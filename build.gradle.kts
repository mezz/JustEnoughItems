import se.bjurr.gitchangelog.plugin.gradle.GitChangelogTask

plugins {
	idea
	eclipse
	id("se.bjurr.gitchangelog.git-changelog-gradle-plugin") version("1.71.4")
	id("com.diffplug.spotless") version("5.14.3")
}
apply {
	from("buildtools/ColoredOutput.gradle")
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

idea {
	module {
		for (fileName in listOf("run", "out", "logs")) {
			excludeDirs.add(file(fileName))
		}
	}
}

spotless {
	java {
		target("*/src/*/java/mezz/jei/**/*.java")

		endWithNewline()
		trimTrailingWhitespace()
		removeUnusedImports()
	}
}
