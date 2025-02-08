import se.bjurr.gitchangelog.plugin.gradle.GitChangelogTask

plugins {
	// https://plugins.gradle.org/plugin/se.bjurr.gitchangelog.git-changelog-gradle-plugin
	id("se.bjurr.gitchangelog.git-changelog-gradle-plugin") version("2.1.2")
}

// gradle.properties
val specificationVersion: String by extra
val changelogUntaggedName = "Current release $specificationVersion"

tasks.register<GitChangelogTask>("makeChangelog") {
	fromRepo = projectDir.absolutePath.toString()
	file = file("changelog.html")
	untaggedName = changelogUntaggedName
	fromRevision = "HEAD~30"
	toRevision = "HEAD"
	templateContent = file("changelog.mustache").readText()
}

tasks.register<GitChangelogTask>("makeMarkdownChangelog") {
	fromRepo = projectDir.absolutePath.toString()
	file = file("changelog.md")
	untaggedName = changelogUntaggedName
	fromRevision = System.getenv("GIT_PREVIOUS_SUCCESSFUL_COMMIT") ?: "HEAD~10"
	toRevision = "HEAD"
	templateContent = file("changelog-markdown.mustache").readText()
}

tasks.withType<GitChangelogTask> {
	notCompatibleWithConfigurationCache("invocation of 'Task.project' at execution time is unsupported")
}
