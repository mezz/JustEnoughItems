#!/usr/bin/env python3
import json
import os
import re
import subprocess
import sys
from pathlib import Path


NO_TESTS_LABEL = os.environ.get("NO_TESTS_LABEL", "no-tests-needed")

PRODUCTION_SOURCE_PATTERN = re.compile(
    r"^(Common|CommonApi|Core|Fabric|FabricApi|Forge|ForgeApi|Gui|Library)/src/main/java/.+\.java$"
)
TEST_SOURCE_PATTERN = re.compile(
    r"^(Common|CommonApi|Core|Fabric|FabricApi|Forge|ForgeApi|Gui|Library)/src/"
    r"(test|testFixtures|clientTestFixtures|gametest|keyMappingGametest|gameTest|clientGameTest)/.+"
)


def run_git(*args: str, check: bool = True) -> subprocess.CompletedProcess[str]:
    return subprocess.run(
        ["git", *args],
        check=check,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        text=True,
    )


def has_ref(ref: str) -> bool:
    return run_git("rev-parse", "--verify", "--quiet", ref, check=False).returncode == 0


def has_no_tests_label() -> bool:
    event_path = os.environ.get("GITHUB_EVENT_PATH")
    if not event_path:
        return False

    path = Path(event_path)
    if not path.is_file():
        return False

    with path.open(encoding="utf-8") as event_file:
        event = json.load(event_file)

    labels = {
        label.get("name")
        for label in event.get("pull_request", {}).get("labels", [])
    }
    return NO_TESTS_LABEL in labels


def find_diff_base() -> str:
    if has_ref("HEAD^1"):
        return "HEAD^1"

    base_ref = os.environ.get("GITHUB_BASE_REF")
    if base_ref and has_ref(f"origin/{base_ref}"):
        return run_git("merge-base", "HEAD", f"origin/{base_ref}").stdout.strip()

    print(
        "::error title=Unable to check test changes::Could not find the pull request base commit. "
        "Ensure actions/checkout uses fetch-depth: 2 or greater.",
        file=sys.stderr,
    )
    sys.exit(1)


def changed_files(diff_base: str) -> list[str]:
    diff = run_git("diff", "--name-only", "--diff-filter=ACMRT", diff_base, "HEAD")
    return [
        line
        for line in diff.stdout.splitlines()
        if line
    ]


def main() -> int:
    if os.environ.get("GITHUB_EVENT_NAME") != "pull_request":
        return 0

    if has_no_tests_label():
        print(f"Skipping test-change check because the pull request has the '{NO_TESTS_LABEL}' label.")
        return 0

    files = changed_files(find_diff_base())
    production_files = [
        file
        for file in files
        if PRODUCTION_SOURCE_PATTERN.match(file)
    ]
    test_files = [
        file
        for file in files
        if TEST_SOURCE_PATTERN.match(file)
    ]

    if not production_files:
        print("No production Java changes detected.")
        return 0

    if test_files:
        print("Production Java changes include test changes.")
        return 0

    print(
        "::error title=Missing test changes::Production Java changed without test or game-test changes. "
        f"Add tests, or have a maintainer apply the '{NO_TESTS_LABEL}' label.",
        file=sys.stderr,
    )
    print("Production Java files:", file=sys.stderr)
    for file in production_files:
        print(f"  {file}", file=sys.stderr)
    return 1


if __name__ == "__main__":
    sys.exit(main())
