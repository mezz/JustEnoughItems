import se.bjurr.gitchangelog.plugin.gradle.GitChangelogTask

plugins {
	// https://plugins.gradle.org/plugin/se.bjurr.gitchangelog.git-changelog-gradle-plugin
	id("se.bjurr.gitchangelog.git-changelog-gradle-plugin") version("3.0.6")
}

// gradle.properties
val specificationVersion: String by extra
val changelogUntaggedName = "Current release $specificationVersion"

val makeHtmlChangelog = tasks.register<GitChangelogTask>("makeHtmlChangelog") {
	val output = layout.buildDirectory.file("changelog.html")

	fromRepo.set(project.rootProject.rootDir.absolutePath)
	file.set(output.get().asFile)
	untaggedName.set(changelogUntaggedName)
	fromRevision.set("HEAD~30")
	toRevision.set("HEAD")
	templateContent.set(file("changelog.mustache").readText())

	// Register output for configuration cache
	outputs.file(output)
}

val makeMarkdownChangelog = tasks.register<GitChangelogTask>("makeMarkdownChangelog") {
	val output = layout.buildDirectory.file("changelog.md")

	fromRepo.set(project.rootProject.rootDir.absolutePath)
	file.set(output.get().asFile)
	untaggedName.set(changelogUntaggedName)
	fromRevision.set(System.getenv("GIT_PREVIOUS_SUCCESSFUL_COMMIT") ?: "HEAD~10")
	toRevision.set("HEAD")
	templateContent.set(file("changelog-markdown.mustache").readText())

	// Register output for configuration cache
	outputs.file(output)
}

tasks.withType<GitChangelogTask> {
	outputs.upToDateWhen { false } // Always run
}

val changelogHtml: Configuration by configurations.creating {
	isCanBeConsumed = true
	isCanBeResolved = false
	attributes {
		attribute(Usage.USAGE_ATTRIBUTE, objects.named<Usage>("changelogHtml"))
	}
	outgoing.artifact(makeHtmlChangelog.map { it.outputs.files.singleFile }) {
		type = "html"
	}
}

val changelogMarkdown: Configuration by configurations.creating {
	isCanBeConsumed = true
	isCanBeResolved = false
	attributes {
		attribute(Usage.USAGE_ATTRIBUTE, objects.named<Usage>("changelogMarkdown"))
	}
	outgoing.artifact(makeMarkdownChangelog.map { it.outputs.files.singleFile }) {
		type = "markdown"
	}
}
