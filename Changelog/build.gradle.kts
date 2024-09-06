import se.bjurr.gitchangelog.plugin.gradle.GitChangelogTask

plugins {
	id("se.bjurr.gitchangelog.git-changelog-gradle-plugin") version("3.1.2")
}

// gradle.properties
val specificationVersion: String by extra
val changelogUntaggedName = "Current release $specificationVersion"

tasks.register<GitChangelogTask>("makeChangelog") {
	fromRepo.set(projectDir.absolutePath.toString())
	file.set(file("changelog.html"))
	untaggedName.set(changelogUntaggedName)
	fromRevision.set("e72e49fa7a072755e7f96cad65388205f6a010dc")
	toRevision.set("HEAD")
	templateContent.set(file("changelog.mustache").readText())
}

tasks.register<GitChangelogTask>("makeMarkdownChangelog") {
	fromRepo.set(projectDir.absolutePath.toString())
	file.set(file("changelog.md"))
	untaggedName.set(changelogUntaggedName)
	fromRevision.set(System.getenv("GIT_PREVIOUS_SUCCESSFUL_COMMIT") ?: "HEAD~10")
	toRevision.set("HEAD")
	templateContent.set(file("changelog-markdown.mustache").readText())
}

tasks.withType<GitChangelogTask> {
	notCompatibleWithConfigurationCache("invocation of 'Task.project' at execution time is unsupported")
}
