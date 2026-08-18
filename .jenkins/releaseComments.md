# GitHub release comments from Jenkins

JEI delegates released-issue and pull-request comments to the shared
[`jenkins-release-notifier`](https://github.com/mezz/jenkins-release-notifier)
worker. The project job only submits release metadata after all artifact
publishing succeeds. The worker owns the GitHub credential, durable retry
queue, target discovery, idempotency markers, and comment delivery.

The integration is pinned to the notifier's `v0.1.1` SCM tag and schedules the
Jenkins job at `/mezz/release-notifier-worker`. Each Minecraft version uses an
independent notifier channel. The project records the last submitted release
commit in its build description so successful non-publishing builds do not
break the next release range. The first submission falls back to Jenkins'
previous successful commit.

Release metadata comes from `gradle.properties`. Exact CurseForge and Modrinth
links come from the `publishMods` result JSON files shared with the Discord
notification; generic project links are used when exact results are absent.

Worker setup, parameter details, retry behavior, and troubleshooting are
documented in the notifier repository:

- [Setup](https://github.com/mezz/jenkins-release-notifier/blob/v0.1.1/docs/setup.md)
- [Troubleshooting](https://github.com/mezz/jenkins-release-notifier/blob/v0.1.1/docs/operations.md)
