import se.bjurr.gitchangelog.plugin.gradle.GitChangelogTask

plugins {
	id("se.bjurr.gitchangelog.git-changelog-gradle-plugin") version("3.1.2")
}

// gradle.properties
val specificationVersion: String by extra

tasks.register<GitChangelogTask>("makeChangelog") {
	fromRepo.set(projectDir.absolutePath.toString())
	file.set(file("changelog.html"))
	untaggedName.set("Current release $specificationVersion")
	fromRevision.set("e72e49fa7a072755e7f96cad65388205f6a010dc")
	toRevision.set("HEAD")
	templateContent.set(file("changelog.mustache").readText())
}
