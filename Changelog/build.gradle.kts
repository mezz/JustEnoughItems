import se.bjurr.gitchangelog.plugin.gradle.GitChangelogTask

plugins {
	id("se.bjurr.gitchangelog.git-changelog-gradle-plugin") version("3.1.2")
}

// gradle.properties
val specificationVersion: String by extra

val makeHtmlChangelog = tasks.register<GitChangelogTask>("makeHtmlChangelog") {
	val output = layout.buildDirectory.file("changelog.html")

	fromRepo.set(project.rootProject.rootDir.absolutePath)
	file.set(output.get().asFile)
	untaggedName.set("Current release $specificationVersion")
	fromRevision.set("e72e49fa7a072755e7f96cad65388205f6a010dc")
	toRevision.set("HEAD")
	templateContent.set(file("changelog.mustache").readText())

	outputs.file(output)
}

tasks.register("makeChangelog") {
	dependsOn(makeHtmlChangelog)
}

tasks.withType<GitChangelogTask> {
	outputs.upToDateWhen { false }
}

val changelogHtml = configurations.create("changelogHtml") {
	isCanBeConsumed = true
	isCanBeResolved = false
	isVisible = false
	attributes {
		attribute(Usage.USAGE_ATTRIBUTE, objects.named<Usage>("changelogHtml"))
	}
	outgoing.artifact(makeHtmlChangelog.map { it.outputs.files.singleFile }) {
		type = "html"
	}
}
