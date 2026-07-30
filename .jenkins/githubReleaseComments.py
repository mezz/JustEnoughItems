#!/usr/bin/env python3
"""Post GitHub release comments after a successful Jenkins publish.

This is a small, Jenkins-friendly version of NeoForge Reactionable's
ReleaseMessageHandler:

* find merged pull requests associated with the released commit range;
* comment on those PRs with the published version;
* comment on issues that those PRs or commit messages reference as fixed;
* include CurseForge and Modrinth release links.

The script only uses the Python standard library so Jenkins does not need a
GitHub CLI install or Python packages.
"""

from __future__ import annotations

import argparse
import json
import os
import re
import subprocess
import sys
import urllib.error
import urllib.parse
import urllib.request
from dataclasses import dataclass
from pathlib import Path
from typing import Any


API_VERSION = "2022-11-28"
FIX_REFERENCE = re.compile(
    r"(?im)\b(?:close|closes|closed|fix|fixes|fixed|resolve|resolves|resolved)\s+#(?P<number>\d+)\b"
)
COMMIT_RE = re.compile(r"^[0-9a-fA-F]{7,40}$")
DEFAULT_MARKER_PREFIX = "jenkins-github-release-notifier"
DEFAULT_LOADER_ORDER = ("NeoForge", "Fabric", "Forge")
DEFAULT_DOWNLOAD_PLATFORMS = ("curseforge", "modrinth")
DEFAULT_ENHANCEMENT_LABELS = ("enhancement",)
DEFAULT_REVIEW_DELAY_NOTE = (
    "_Note: CurseForge and Modrinth links may take a little time to work while "
    "new files are reviewed._"
)
DEFAULT_PUBLISH_RESULT_FILES = (
    ("Forge", "Forge/build/publishMods/publishCurseforge.json"),
    ("Forge", "Forge/build/publishMods/publishModrinth.json"),
    ("Fabric", "Fabric/build/publishMods/publishCurseforge.json"),
    ("Fabric", "Fabric/build/publishMods/publishModrinth.json"),
    ("NeoForge", "NeoForge/build/publishMods/publishCurseforge.json"),
    ("NeoForge", "NeoForge/build/publishMods/publishModrinth.json"),
    ("Forge", "build/publishMods/publishCurseforge.json"),
    ("Forge", "build/publishMods/publishModrinth.json"),
)


@dataclass(frozen=True)
class PublishResultFile:
    module: str
    path: Path


@dataclass(frozen=True)
class PullRequest:
    number: int
    title: str
    merged_at: str


@dataclass(frozen=True)
class CommentTarget:
    kind: str
    number: int
    visible_body: str
    marker: str

    @property
    def body(self) -> str:
        return f"{self.visible_body}\n\n<!-- {self.marker} -->"


class GitHubClient:
    def __init__(self, token: str, api_url: str, dry_run: bool):
        self.token = token
        self.api_url = api_url.rstrip("/")
        self.dry_run = dry_run

    def request_json(
        self,
        method: str,
        path_or_url: str,
        payload: dict[str, Any] | None = None,
        *,
        accept: str = "application/vnd.github+json",
        allow_empty: bool = False,
    ) -> tuple[Any, dict[str, str]]:
        if path_or_url.startswith("https://"):
            url = path_or_url
        else:
            url = f"{self.api_url}{path_or_url}"

        data = None
        if payload is not None:
            data = json.dumps(payload).encode("utf-8")

        headers = {
            "Accept": accept,
            "Content-Type": "application/json",
            "User-Agent": "jenkins-github-release-comments",
            "X-GitHub-Api-Version": API_VERSION,
        }
        if self.token:
            headers["Authorization"] = f"Bearer {self.token}"

        request = urllib.request.Request(url, data=data, headers=headers, method=method)
        try:
            with urllib.request.urlopen(request, timeout=30) as response:
                body = response.read().decode("utf-8")
                response_headers = {key: value for key, value in response.headers.items()}
        except urllib.error.HTTPError as error:
            detail = error.read().decode("utf-8", errors="replace")
            raise RuntimeError(
                f"GitHub API {method} {url} failed with HTTP {error.code}: {detail[:1000]}"
            ) from error

        if not body:
            if allow_empty:
                return None, response_headers
            return {}, response_headers

        return json.loads(body), response_headers

    def paginate(self, path: str, *, accept: str = "application/vnd.github+json") -> list[Any]:
        results: list[Any] = []
        next_path_or_url: str | None = path
        while next_path_or_url:
            page, headers = self.request_json("GET", next_path_or_url, accept=accept)
            if not isinstance(page, list):
                raise RuntimeError(f"Expected a JSON array from {next_path_or_url}")
            results.extend(page)
            next_path_or_url = get_next_link(headers.get("Link", ""))
        return results

    def graphql(self, query: str, variables: dict[str, Any]) -> dict[str, Any]:
        response, _ = self.request_json(
            "POST",
            "/graphql",
            {"query": query, "variables": variables},
            accept="application/vnd.github+json",
        )
        errors = response.get("errors")
        if errors:
            raise RuntimeError(f"GitHub GraphQL failed: {json.dumps(errors)}")
        return response["data"]

    def create_issue_comment(self, repo: str, number: int, body: str) -> None:
        if self.dry_run:
            print(f"[dry-run] Would comment on {repo}#{number}: {body.splitlines()[0]}")
            return

        self.request_json(
            "POST",
            f"/repos/{repo}/issues/{number}/comments",
            {"body": body},
            allow_empty=True,
        )
        print(f"Commented on {repo}#{number}")


def get_next_link(link_header: str) -> str | None:
    if not link_header:
        return None

    for part in link_header.split(","):
        section = part.strip()
        if 'rel="next"' not in section:
            continue
        start = section.find("<")
        end = section.find(">", start + 1)
        if 0 <= start < end:
            return section[start + 1 : end]

    return None


def read_gradle_properties(path: Path) -> dict[str, str]:
    properties: dict[str, str] = {}
    if not path.exists():
        return properties

    for line in path.read_text(encoding="utf-8").splitlines():
        stripped = line.strip()
        if not stripped or stripped.startswith("#") or "=" not in stripped:
            continue
        key, value = stripped.split("=", 1)
        properties[key.strip()] = value.strip()
    return properties


def repo_from_github_url(url: str) -> str:
    clean_url = url.strip().removesuffix(".git").rstrip("/")
    ssh_match = re.match(r"git@github\.com:([^/]+)/([^/]+)$", clean_url)
    if ssh_match:
        return f"{ssh_match.group(1)}/{ssh_match.group(2)}"

    parsed = urllib.parse.urlparse(clean_url)
    if parsed.netloc.lower() != "github.com":
        return ""

    parts = [part for part in parsed.path.split("/") if part]
    if len(parts) < 2:
        return ""
    return f"{parts[0]}/{parts[1]}"


def remove_trailing_slashes(url: str) -> str:
    return url.rstrip("/")


def get_last_path_segment(url: str) -> str:
    return remove_trailing_slashes(url).rsplit("/", 1)[-1]


def parse_csv(value: str | None) -> list[str]:
    if not value:
        return []
    return [part.strip() for part in value.split(",") if part.strip()]


def parse_download_platforms(value: str) -> set[str]:
    platforms = {platform.casefold() for platform in parse_csv(value)}
    if platforms == {"none"}:
        return set()

    valid_platforms = set(DEFAULT_DOWNLOAD_PLATFORMS)
    invalid_platforms = platforms - valid_platforms
    if invalid_platforms:
        invalid = ", ".join(sorted(invalid_platforms))
        valid = ", ".join(DEFAULT_DOWNLOAD_PLATFORMS)
        raise RuntimeError(f"Invalid --download-platforms value: {invalid}. Valid values are: {valid}, none.")

    return platforms


def resolve_project_path(project_root: Path, path_text: str) -> Path:
    path = Path(path_text)
    if path.is_absolute():
        return path
    return project_root / path


def parse_publish_result_file(project_root: Path, value: str) -> PublishResultFile:
    if "=" not in value:
        raise RuntimeError(
            f"Invalid --publish-result-file value {value!r}. Expected format: Loader=relative/path.json"
        )

    module, path_text = value.split("=", 1)
    module = module.strip()
    path_text = path_text.strip()
    if not module or not path_text:
        raise RuntimeError(
            f"Invalid --publish-result-file value {value!r}. Expected format: Loader=relative/path.json"
        )

    return PublishResultFile(module, resolve_project_path(project_root, path_text))


def get_publish_result_files(
    project_root: Path,
    extra_publish_result_files: list[str],
    *,
    include_defaults: bool,
) -> list[PublishResultFile]:
    result: list[PublishResultFile] = []
    if include_defaults:
        result.extend(
            PublishResultFile(module, resolve_project_path(project_root, path))
            for module, path in DEFAULT_PUBLISH_RESULT_FILES
        )

    for value in extra_publish_result_files:
        result.append(parse_publish_result_file(project_root, value))

    return result


def get_release_loaders(project_root: Path, loader_order: list[str], explicit_loaders: list[str]) -> list[str]:
    if explicit_loaders:
        return explicit_loaders

    loaders: list[str] = []
    for loader in loader_order:
        if (project_root / loader / "build.gradle").exists() or (project_root / loader / "build.gradle.kts").exists():
            loaders.append(loader)

    return loaders


def ordered_loader_links(links_by_module: dict[str, str], loaders: list[str], loader_order: list[str]) -> list[str]:
    ordered_loaders = [loader for loader in loader_order if loader in loaders]
    ordered_loaders.extend(loader for loader in loaders if loader not in ordered_loaders)
    return [
        links_by_module[loader]
        for loader in ordered_loaders
        if loader in loaders and loader in links_by_module
    ]


def fallback_loader_links(url: str, loaders: list[str]) -> list[str]:
    return [f"[{loader}]({url})" for loader in loaders]


def get_release_link_lines(
    properties: dict[str, str],
    project_root: Path,
    loader_order: list[str],
    explicit_loaders: list[str],
    publish_result_files: list[PublishResultFile],
    curseforge_homepage_url: str,
    modrinth_id: str,
    download_platforms: set[str],
    *,
    include_fallback: bool = True,
) -> list[str]:
    release_loaders = get_release_loaders(project_root, loader_order, explicit_loaders)
    curseforge_links_by_module: dict[str, str] = {}
    modrinth_links_by_module: dict[str, str] = {}
    curse_homepage_url = remove_trailing_slashes(
        curseforge_homepage_url or properties.get("curseHomepageUrl", "")
    )
    curse_project_slug = get_last_path_segment(curse_homepage_url)

    for publish_result_file in publish_result_files:
        if publish_result_file.module not in release_loaders:
            continue

        result_file = publish_result_file.path
        if not result_file.exists():
            continue

        try:
            publish_result = json.loads(result_file.read_text(encoding="utf-8"))
        except json.JSONDecodeError as error:
            print(f"Could not parse {result_file}: {error}", file=sys.stderr)
            continue

        publish_type = publish_result.get("type")
        if publish_type == "curseforge" and "curseforge" in download_platforms:
            file_id = publish_result.get("fileId")
            project_slug = publish_result.get("projectSlug") or curse_project_slug
            if file_id and project_slug and project_slug != "dry-run":
                if curse_homepage_url:
                    curseforge_url = f"{curse_homepage_url}/files/{file_id}"
                else:
                    curseforge_url = f"https://www.curseforge.com/minecraft/mc-mods/{project_slug}/files/{file_id}"
                curseforge_links_by_module[publish_result_file.module] = f"[{publish_result_file.module}]({curseforge_url})"
        elif publish_type == "modrinth" and "modrinth" in download_platforms:
            project_id = publish_result.get("projectId")
            version_id = publish_result.get("id")
            if project_id and project_id != "dry-run" and version_id:
                modrinth_links_by_module[publish_result_file.module] = (
                    f"[{publish_result_file.module}](https://modrinth.com/mod/{project_id}/version/{version_id})"
                )

    release_link_lines: list[str] = []

    curseforge_links = ordered_loader_links(curseforge_links_by_module, release_loaders, loader_order)
    if curseforge_links:
        release_link_lines.append(f"**CurseForge:** {' | '.join(curseforge_links)}")

    modrinth_links = ordered_loader_links(modrinth_links_by_module, release_loaders, loader_order)
    if modrinth_links:
        release_link_lines.append(f"**Modrinth:** {' | '.join(modrinth_links)}")

    if not include_fallback:
        return release_link_lines

    fallback_curseforge_links = fallback_loader_links(f"{curse_homepage_url}/files", release_loaders)
    if "curseforge" in download_platforms and not curseforge_links and curse_homepage_url and fallback_curseforge_links:
        release_link_lines.append(f"**CurseForge:** {' | '.join(fallback_curseforge_links)}")

    if "modrinth" in download_platforms and not modrinth_links and modrinth_id:
        fallback_modrinth_links = fallback_loader_links(
            f"https://modrinth.com/mod/{modrinth_id}/versions",
            release_loaders,
        )
        if fallback_modrinth_links:
            release_link_lines.append(f"**Modrinth:** {' | '.join(fallback_modrinth_links)}")

    return release_link_lines


def git(args: list[str], project_root: Path) -> str:
    return subprocess.check_output(["git", *args], cwd=project_root, text=True).strip()


def is_commit(value: str) -> bool:
    return bool(COMMIT_RE.fullmatch(value or ""))


def resolve_head(explicit_head: str, project_root: Path) -> str:
    candidates = [explicit_head, os.environ.get("GIT_COMMIT", ""), git(["rev-parse", "HEAD"], project_root)]
    for candidate in candidates:
        if is_commit(candidate):
            return candidate
    raise RuntimeError("Could not determine the release HEAD commit")


def resolve_base(explicit_base: str) -> str:
    candidates = [
        explicit_base,
        os.environ.get("GIT_PREVIOUS_SUCCESSFUL_COMMIT", ""),
        os.environ.get("GIT_PREVIOUS_COMMIT", ""),
    ]
    for candidate in candidates:
        if is_commit(candidate):
            return candidate
    return ""


def commit_exists(sha: str, project_root: Path) -> bool:
    if not sha:
        return False
    result = subprocess.run(
        ["git", "cat-file", "-e", f"{sha}^{{commit}}"],
        cwd=project_root,
        stdout=subprocess.DEVNULL,
        stderr=subprocess.DEVNULL,
        check=False,
    )
    return result.returncode == 0


def released_commits(base: str, head: str, project_root: Path) -> list[str]:
    if base and base != head and commit_exists(base, project_root) and commit_exists(head, project_root):
        try:
            commits = git(["rev-list", "--reverse", f"{base}..{head}"], project_root).splitlines()
            if commits:
                return commits
        except subprocess.CalledProcessError as error:
            print(f"Could not inspect commit range {base}..{head}: {error}", file=sys.stderr)

    return [head]


def commit_message(sha: str, project_root: Path) -> str:
    try:
        return git(["show", "-s", "--format=%B", sha], project_root)
    except subprocess.CalledProcessError:
        return ""


def list_pull_requests_for_commit(client: GitHubClient, repo: str, sha: str) -> list[PullRequest]:
    pulls, _ = client.request_json("GET", f"/repos/{repo}/commits/{sha}/pulls")
    result: list[PullRequest] = []
    for pull in pulls:
        merged_at = pull.get("merged_at")
        if not merged_at:
            continue
        result.append(
            PullRequest(
                number=int(pull["number"]),
                title=pull.get("title", ""),
                merged_at=merged_at,
            )
        )
    return result


def fixed_issues_for_pull_request(client: GitHubClient, repo: str, pr_number: int) -> list[int]:
    owner, name = repo.split("/", 1)
    query = """
query($owner: String!, $name: String!, $number: Int!, $cursor: String) {
  repository(owner: $owner, name: $name) {
    pullRequest(number: $number) {
      closingIssuesReferences(first: 100, after: $cursor) {
        nodes {
          number
          repository {
            nameWithOwner
          }
        }
        pageInfo {
          hasNextPage
          endCursor
        }
      }
    }
  }
}
"""

    # GitHub names this field "closingIssuesReferences". The script only reads
    # it to decide who to notify; it never closes, edits, labels, or locks an
    # issue.
    issues: list[int] = []
    cursor: str | None = None
    while True:
        data = client.graphql(
            query,
            {
                "owner": owner,
                "name": name,
                "number": pr_number,
                "cursor": cursor,
            },
        )
        refs = data["repository"]["pullRequest"]["closingIssuesReferences"]
        for node in refs["nodes"]:
            if node["repository"]["nameWithOwner"].lower() == repo.lower():
                issues.append(int(node["number"]))

        page_info = refs["pageInfo"]
        if not page_info["hasNextPage"]:
            return issues
        cursor = page_info["endCursor"]


def issue_numbers_from_text(text: str) -> set[int]:
    return {int(match.group("number")) for match in FIX_REFERENCE.finditer(text or "")}


def get_issue_labels(client: GitHubClient, repo: str, number: int) -> set[str]:
    issue, _ = client.request_json("GET", f"/repos/{repo}/issues/{number}")
    labels = issue.get("labels", [])
    result: set[str] = set()
    for label in labels:
        if isinstance(label, dict):
            name = label.get("name", "")
        else:
            name = str(label)
        if name:
            result.add(name)
    return result


def get_issue_labels_safely(client: GitHubClient, repo: str, number: int) -> set[str]:
    try:
        return get_issue_labels(client, repo, number)
    except Exception as error:
        print(f"Could not read labels for {repo}#{number}: {error}", file=sys.stderr)
        return set()


def existing_release_comment(
    client: GitHubClient,
    repo: str,
    target: CommentTarget,
) -> bool:
    comments = client.paginate(f"/repos/{repo}/issues/{target.number}/comments?per_page=100")
    for comment in comments:
        body = comment.get("body", "")
        if target.marker in body or target.visible_body in body:
            return True
    return False


def marker(marker_prefix: str, kind: str, number: int, version: str, minecraft_version: str) -> str:
    if minecraft_version:
        return f"{marker_prefix}:{kind}:{number}:{version}:minecraft:{minecraft_version}"
    return f"{marker_prefix}:{kind}:{number}:{version}"


def release_description(project_name: str, version: str, minecraft_version: str) -> str:
    description = f"{project_name} version `{version}`"
    if minecraft_version:
        description += f" for Minecraft `{minecraft_version}`"
    return description


def issue_thanks(labels: set[str], enhancement_labels: set[str]) -> str:
    normalized_labels = {label.casefold() for label in labels}
    if normalized_labels.intersection(enhancement_labels):
        return "Thanks for requesting this feature!"
    return "Thanks for reporting this issue!"


def add_release_links(summary: str, release_link_lines: list[str], review_delay_note: str) -> str:
    if not release_link_lines:
        return summary
    body = f"{summary}\n\n" + "\n".join(release_link_lines)
    if review_delay_note:
        body += f"\n\n{review_delay_note}"
    return body


def build_targets(
    project_name: str,
    version: str,
    minecraft_version: str,
    pull_requests: dict[int, PullRequest],
    issue_to_pull_request: dict[int, int | None],
    issue_labels: dict[int, set[str]],
    release_link_lines: list[str],
    marker_prefix: str,
    enhancement_labels: set[str],
    review_delay_note: str,
    *,
    skip_pr_comments: bool,
    skip_issue_comments: bool,
) -> list[CommentTarget]:
    targets: list[CommentTarget] = []
    release = release_description(project_name, version, minecraft_version)

    if not skip_pr_comments:
        for number in sorted(pull_requests):
            visible_body = add_release_links(
                f"🚀 This PR is included in {release}.",
                release_link_lines,
                review_delay_note,
            )
            targets.append(
                CommentTarget("pr", number, visible_body, marker(marker_prefix, "pr", number, version, minecraft_version))
            )

    if not skip_issue_comments:
        for number in sorted(issue_to_pull_request):
            pr_number = issue_to_pull_request[number]
            thanks = issue_thanks(issue_labels.get(number, set()), enhancement_labels)
            if pr_number is None:
                summary = f"🚀 A fix for this issue is available in {release}.\n{thanks}"
            else:
                summary = (
                    f"🚀 A fix for this issue is available in {release}, "
                    f"via PR #{pr_number}.\n{thanks}"
                )
            visible_body = add_release_links(summary, release_link_lines, review_delay_note)
            targets.append(
                CommentTarget("issue", number, visible_body, marker(marker_prefix, "issue", number, version, minecraft_version))
            )

    return targets


def infer_version(explicit_version: str, properties: dict[str, str]) -> str:
    if explicit_version:
        return explicit_version

    specification_version = properties.get("specificationVersion", "")
    build_number = os.environ.get("BUILD_NUMBER", "")
    if specification_version and build_number:
        return f"{specification_version}.{build_number}"

    raise RuntimeError(
        "Could not determine the release version. Pass --version or run from Jenkins with BUILD_NUMBER set."
    )


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--project-root", default=".", help="Project checkout root. Defaults to the current directory.")
    parser.add_argument("--gradle-properties", help="Path to gradle.properties. Defaults to <project-root>/gradle.properties.")
    parser.add_argument("--repo", help="GitHub repository, for example mezz/JustEnoughItems")
    parser.add_argument("--version", help="Released version. Defaults to specificationVersion.BUILD_NUMBER.")
    parser.add_argument("--minecraft-version", help="Minecraft version. Defaults to minecraftVersion in gradle.properties.")
    parser.add_argument("--project-name", help="Name to use in release comments. Defaults to modName.")
    parser.add_argument("--base", default="", help="Previous released commit. Defaults to Jenkins env.")
    parser.add_argument("--head", default="", help="Released commit. Defaults to GIT_COMMIT or HEAD.")
    parser.add_argument("--api-url", default="https://api.github.com", help="GitHub API base URL.")
    parser.add_argument("--token-env", default="GITHUB_TOKEN", help="Environment variable containing the token.")
    parser.add_argument("--marker-prefix", default=DEFAULT_MARKER_PREFIX, help="Hidden marker prefix used to avoid duplicates.")
    parser.add_argument(
        "--loader-order",
        default=",".join(DEFAULT_LOADER_ORDER),
        help="Comma-separated loader order for link display. Defaults to NeoForge,Fabric,Forge.",
    )
    parser.add_argument("--loaders", help="Comma-separated loader list. Defaults to detecting module directories.")
    parser.add_argument(
        "--publish-result-file",
        action="append",
        default=[],
        metavar="LOADER=PATH",
        help="Additional publishMods result JSON file. Can be repeated.",
    )
    parser.add_argument(
        "--no-default-publish-result-files",
        action="store_true",
        help="Only use --publish-result-file entries for CurseForge/Modrinth link detection.",
    )
    parser.add_argument(
        "--download-platforms",
        default=",".join(DEFAULT_DOWNLOAD_PLATFORMS),
        help="Comma-separated download platforms to link. Valid values: curseforge,modrinth,none.",
    )
    parser.add_argument("--curseforge-homepage-url", help="Fallback CurseForge project URL. Defaults to curseHomepageUrl.")
    parser.add_argument("--modrinth-id", help="Fallback Modrinth project ID or slug. Defaults to modrinthId.")
    parser.add_argument(
        "--enhancement-label",
        action="append",
        default=list(DEFAULT_ENHANCEMENT_LABELS),
        help="Label treated as a feature request. Can be repeated. Defaults to enhancement.",
    )
    parser.add_argument(
        "--review-delay-note",
        default=DEFAULT_REVIEW_DELAY_NOTE,
        help="Note appended after CurseForge/Modrinth links. Pass an empty value to disable.",
    )
    parser.add_argument(
        "--no-download-link-fallback",
        action="store_true",
        help="Do not add generic CurseForge/Modrinth fallback links when exact publish result files are absent.",
    )
    parser.add_argument("--dry-run", action="store_true", help="Print intended comments without posting them.")
    parser.add_argument("--skip-pr-comments", action="store_true", help="Do not comment on released PRs.")
    parser.add_argument("--skip-issue-comments", action="store_true", help="Do not comment on fixed issues.")
    return parser.parse_args()


def main() -> int:
    args = parse_args()
    project_root = Path(args.project_root).resolve()
    gradle_properties_path = (
        resolve_project_path(project_root, args.gradle_properties)
        if args.gradle_properties
        else project_root / "gradle.properties"
    )
    properties = read_gradle_properties(gradle_properties_path)
    loader_order = parse_csv(args.loader_order) or list(DEFAULT_LOADER_ORDER)
    explicit_loaders = parse_csv(args.loaders)
    publish_result_files = get_publish_result_files(
        project_root,
        args.publish_result_file,
        include_defaults=not args.no_default_publish_result_files,
    )
    curseforge_homepage_url = args.curseforge_homepage_url or properties.get("curseHomepageUrl", "")
    modrinth_id = args.modrinth_id or properties.get("modrinthId", "")
    download_platforms = parse_download_platforms(args.download_platforms)
    enhancement_labels = {
        label.casefold()
        for value in args.enhancement_label
        for label in parse_csv(value)
    }

    repo = args.repo or os.environ.get("GITHUB_REPOSITORY", "") or repo_from_github_url(properties.get("githubUrl", ""))
    if not repo:
        raise RuntimeError("Could not determine the GitHub repository. Pass --repo owner/name.")

    version = infer_version(args.version, properties)
    minecraft_version = args.minecraft_version or properties.get("minecraftVersion", "")
    project_name = args.project_name or properties.get("modName") or repo.split("/", 1)[1]
    if args.skip_pr_comments and args.skip_issue_comments:
        print("Skipping release comment discovery because both PR and issue comments are disabled.")
        return 0

    head = resolve_head(args.head, project_root)
    base = resolve_base(args.base)
    commits = released_commits(base, head, project_root)

    token = os.environ.get(args.token_env, "")
    if not token and not args.dry_run:
        raise RuntimeError(f"{args.token_env} is required unless --dry-run is used.")

    client = GitHubClient(token, args.api_url, args.dry_run)

    pull_requests: dict[int, PullRequest] = {}
    issue_to_pull_request: dict[int, int | None] = {}
    issue_labels: dict[int, set[str]] = {}
    release_link_lines = get_release_link_lines(
        properties,
        project_root,
        loader_order,
        explicit_loaders,
        publish_result_files,
        curseforge_homepage_url,
        modrinth_id,
        download_platforms,
        include_fallback=not args.no_download_link_fallback,
    )

    print(f"Inspecting {len(commits)} released commit(s) for {repo} version {version}.")
    for sha in commits:
        for number in issue_numbers_from_text(commit_message(sha, project_root)):
            issue_to_pull_request.setdefault(number, None)

        for pull_request in list_pull_requests_for_commit(client, repo, sha):
            pull_requests.setdefault(pull_request.number, pull_request)

    if not args.skip_issue_comments:
        for pr_number in sorted(pull_requests):
            for issue_number in fixed_issues_for_pull_request(client, repo, pr_number):
                if issue_to_pull_request.get(issue_number) is None:
                    issue_to_pull_request[issue_number] = pr_number

        for issue_number in sorted(issue_to_pull_request):
            issue_labels[issue_number] = get_issue_labels_safely(client, repo, issue_number)

    targets = build_targets(
        project_name,
        version,
        minecraft_version,
        pull_requests,
        issue_to_pull_request,
        issue_labels,
        release_link_lines,
        args.marker_prefix,
        enhancement_labels,
        args.review_delay_note,
        skip_pr_comments=args.skip_pr_comments,
        skip_issue_comments=args.skip_issue_comments,
    )
    if not targets:
        print("No merged PRs or fixed issues found for this release.")
        return 0

    for target in targets:
        if existing_release_comment(client, repo, target):
            print(f"Skipping {repo}#{target.number}; release comment already exists.")
            continue
        client.create_issue_comment(repo, target.number, target.body)

    return 0


if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except Exception as exception:
        print(f"GitHub release comments failed: {exception}", file=sys.stderr)
        raise SystemExit(1)
