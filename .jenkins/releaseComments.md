# GitHub release comments from Jenkins

NeoForge's `neoforged-releases` comments come from Reactionable's
`ReleaseMessageHandler`. This setup uses the same detection idea but keeps the
action strictly notification-only: Jenkins runs
`.jenkins/githubReleaseComments.py` after publishing succeeds, and the script
only posts comments.

The script finds merged PRs associated with the released commit range, follows
the PR references GitHub exposes through `closingIssuesReferences`, scans commit
messages for references such as `Fix #123`, and comments to tell people where
the released fix can be downloaded. It never closes, edits, labels, or locks
issues or PRs.

Issue comments thank the reporter by default. If the issue has the
`enhancement` label, the comment thanks them for requesting the feature instead.

## Setup

1. Create a GitHub fine-grained PAT from
   <https://github.com/settings/personal-access-tokens/new>. For a repository
   such as `https://github.com/<owner>/<repo>`, choose `<owner>` as the resource
   owner and restrict repository access to `<repo>`. If you prefer a GitHub App
   installation token, start from <https://github.com/settings/apps/new> and
   install the app on the repository.
   - Required for this direct Jenkins script:
     - repository metadata: read
     - contents/commits: read
     - pull requests: read
     - issues and/or pull requests: write comments
2. Store the token in Jenkins as a secret text credential.
3. Expose it to this job as `GITHUB_RELEASE_COMMENT_TOKEN`, or add this to the
   Jenkinsfile `environment` block once the credential exists:

   ```groovy
   GITHUB_RELEASE_COMMENT_TOKEN = credentials("github-release-comment-token")
   ```

4. Ensure the Jenkins agent has Python 3.10 or newer available as `python3`.

The Jenkinsfile stage is opt-in and skipped unless
`GITHUB_RELEASE_COMMENT_TOKEN` is set:

```groovy
stage('Comment GitHub Release') {
    when {
        expression { env.SHOULD_PUBLISH != 'false' && env.GITHUB_RELEASE_COMMENT_TOKEN }
    }
    steps {
        catchError(buildResult: 'SUCCESS', stageResult: 'UNSTABLE') {
            withEnv(["GITHUB_TOKEN=${env.GITHUB_RELEASE_COMMENT_TOKEN}"]) {
                sh "python3 .jenkins/githubReleaseComments.py"
            }
        }
    }
}
```

## Local dry run

Use a token even for dry runs if you want GraphQL closing-issue detection:

```sh
GITHUB_TOKEN=... BUILD_NUMBER=1234 python3 .jenkins/githubReleaseComments.py --dry-run
```

Useful overrides:

```sh
python3 .jenkins/githubReleaseComments.py \
  --dry-run \
  --version 19.43.0.1234 \
  --minecraft-version 1.21.1 \
  --base <previous-release-commit> \
  --head <released-commit>
```

The default version is `specificationVersion.BUILD_NUMBER`, matching the Gradle
version in `build.gradle.kts`. The default Minecraft version is
`minecraftVersion` from `gradle.properties`, so backport release comments say
which Minecraft line contains the fix.

CurseForge and Modrinth links are controlled by `--download-platforms`, which
defaults to `curseforge,modrinth`. Exact links are read from the same
`publishMods` JSON result files used by `.jenkins/discordNotification.groovy`;
if those files are not present, the script falls back to the configured
CurseForge files page and Modrinth versions page. Comments include a note that
CurseForge and Modrinth may take time to review new files before those links
work.

## Copying to another project

The script is intentionally self-contained and uses only Python's standard
library. For another Jenkins-built project, copy `.jenkins/githubReleaseComments.py`
and adjust the Jenkins token credential.

Useful configuration options:

- `--repo owner/name`
- `--project-name "Project Name"`
- `--version 1.2.3`
- `--minecraft-version 1.21.1`
- `--marker-prefix my-project-release-comment`
- `--loaders NeoForge,Fabric` to bypass module-directory detection
- `--loader-order NeoForge,Fabric,Forge` to control link order
- `--download-platforms curseforge,modrinth` to control which download links are shown; use `none` to disable them
- `--publish-result-file NeoForge=NeoForge/build/publishMods/publishCurseforge.json`
- `--curseforge-homepage-url https://www.curseforge.com/minecraft/mc-mods/<slug>`
- `--modrinth-id <project-id-or-slug>`
- `--enhancement-label enhancement`, repeated for other feature-request labels
- `--review-delay-note ""` to suppress the review-delay note
- `--no-download-link-fallback` to only include exact publish result links

If the copied project also uses these Gradle properties, no extra arguments are
usually required: `githubUrl`, `modName`, `specificationVersion`,
`minecraftVersion`, `curseHomepageUrl`, and `modrinthId`.

Sources:

- NeoForge Reactionable release handler:
  https://github.com/neoforged/Reactionable/blob/main/src/main/java/net/neoforged/automation/webhook/handler/ReleaseMessageHandler.java
- GitHub REST: list pull requests associated with a commit:
  https://docs.github.com/en/rest/commits/commits?apiVersion=2022-11-28#list-pull-requests-associated-with-a-commit
- GitHub REST: create an issue comment:
  https://docs.github.com/en/rest/issues/comments?apiVersion=2022-11-28#create-an-issue-comment
- GitHub GraphQL `PullRequest.closingIssuesReferences`:
  https://docs.github.com/en/graphql/reference/objects#pullrequest
- Jenkins Credentials Binding:
  https://plugins.jenkins.io/credentials-binding/
