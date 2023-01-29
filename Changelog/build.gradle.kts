import se.bjurr.gitchangelog.plugin.gradle.GitChangelogTask

plugins {
	id("se.bjurr.gitchangelog.git-changelog-gradle-plugin") version("1.77.2")
}

// gradle.properties
val specificationVersion: String by extra
val changelogUntaggedName = "Current release $specificationVersion"

tasks.register<GitChangelogTask>("makeChangelog") {
	fromRepo = projectDir.absolutePath.toString()
	file = file("changelog.html")
	untaggedName = changelogUntaggedName
	fromCommit = "e72e49fa7a072755e7f96cad65388205f6a010dc"
	toRef = "HEAD"
	templateContent = file("changelog.mustache").readText()
}

tasks.register<GitChangelogTask>("makeMarkdownChangelog") {
	fromRepo = projectDir.absolutePath.toString()
	file = file("changelog.md")
	untaggedName = changelogUntaggedName
	fromCommit = System.getenv("GIT_PREVIOUS_SUCCESSFUL_COMMIT") ?: "HEAD~10"
	toRef = "HEAD"
	templateContent = file("changelog-markdown.mustache").readText()
}
