# Jenkins release notifications

The Jenkinsfile uses `jenkins-release-notifier@v0.3.0` for GitHub release
comments and Discord build summaries. Both are queued on
`/mezz/release-notifier-worker`, which owns notification credentials and retries.

Release notifications run only after a successful publish permitted by the
branch's publish guard. The channel is Minecraft `1.12.2`; the shared helper
reads the version and download links from this branch's Gradle metadata and
recognizes its root project as Forge. Discord summaries run after every build
and retain the publish-skipped status.

Worker setup and operation are documented in the
[shared notifier](https://github.com/mezz/jenkins-release-notifier/tree/v0.3.0/docs).
The worker must also use `v0.3.0` to accept its Discord notification format.
