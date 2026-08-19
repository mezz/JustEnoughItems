# GitHub release comments from Jenkins

JEI delegates released-issue, pull-request, and Discord build notifications to
the shared [`jenkins-release-notifier`](https://github.com/mezz/jenkins-release-notifier)
worker. The project job submits release metadata after artifact publishing and
queues a Discord build summary from its `post` block. The worker owns the
GitHub and Discord credentials, durable retry queues, target discovery,
idempotency markers, and delivery.

The integration loads the notifier's `v0.2.0` SCM tag before the Pipeline and
schedules the Jenkins job at `/mezz/release-notifier-worker`. Each Minecraft
version uses an independent notifier channel. The project records the last
submitted release commit in its build description so successful
non-publishing builds do not break the next release range. The first submission
falls back to Jenkins' previous successful commit.

The notifier library reads release metadata from `gradle.properties`. Exact
CurseForge and Modrinth links come from the Mod Publish Plugin result JSON
files; generic project links are used when exact results are absent. Discord
notifications include the result, branch, build number, version, commits, and
available release links.

Worker setup, parameter details, retry behavior, and troubleshooting are
documented in the notifier repository:

- [Setup](https://github.com/mezz/jenkins-release-notifier/blob/v0.2.0/docs/setup.md)
- [Discord notifications](https://github.com/mezz/jenkins-release-notifier/blob/v0.2.0/docs/discord.md)
- [Troubleshooting](https://github.com/mezz/jenkins-release-notifier/blob/v0.2.0/docs/operations.md)
