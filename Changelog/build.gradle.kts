import se.bjurr.gitchangelog.plugin.gradle.GitChangelogTask

plugins {
	id("se.bjurr.gitchangelog.git-changelog-gradle-plugin") version("1.77.2")
}

// gradle.properties
val specificationVersion: String by extra

tasks.register<GitChangelogTask>("makeChangelog") {
	fromRepo = projectDir.absolutePath.toString()
	file = file("changelog.html")
	untaggedName = "Current release $specificationVersion"
	fromCommit = "e72e49fa7a072755e7f96cad65388205f6a010dc"
	toRef = "HEAD"
	templateContent = file("changelog.mustache").readText()
}
