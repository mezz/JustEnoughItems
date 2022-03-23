//import se.bjurr.gitchangelog.plugin.gradle.GitChangelogTask
//
//plugins {
//	idea
//	eclipse
//	id("se.bjurr.gitchangelog.git-changelog-gradle-plugin") version("1.71.4")
//	id("com.diffplug.spotless") version("5.14.3")
//}
//apply {
//	from("buildtools/ColoredOutput.gradle")
//}
//
//// gradle.properties
//val specificationVersion: String by extra
//
//tasks.register<GitChangelogTask>("makeChangelog") {
//	fromRepo = projectDir.absolutePath.toString()
//	file = file("changelog.html")
//	untaggedName = "Current release $specificationVersion"
//	fromCommit = "e72e49fa7a072755e7f96cad65388205f6a010dc"
//	toRef = "HEAD"
//	templateContent = file("changelog.mustache").readText()
//}
//
//idea {
//	module {
//		for (fileName in listOf("run", "out", "logs")) {
//			excludeDirs.add(file(fileName))
//		}
//	}
//}
//
//spotless {
//	java {
//		target("*/src/*/java/mezz/jei/**/*.java")
//
//		endWithNewline()
//		trimTrailingWhitespace()
//		removeUnusedImports()
//	}
//}

// gradle.properties
val modGroup: String by extra
val modName: String by extra
val modAuthor: String by extra
val specificationVersion: String by extra
val modJavaVersion: String by extra

subprojects {
    //adds the build number to the end of the version string if on a build server
    var buildNumber = project.findProperty("BUILD_NUMBER")
    if (buildNumber == null) {
        buildNumber = "9999"
    }

    version = "${specificationVersion}.${buildNumber}"
    group = modGroup

    tasks.withType<Javadoc> {
        // workaround cast for https://github.com/gradle/gradle/issues/7038
        val standardJavadocDocletOptions = options as StandardJavadocDocletOptions
        // prevent java 8's strict doclint for javadocs from failing builds
        standardJavadocDocletOptions.addStringOption("Xdoclint:none", "-quiet")
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release.set(JavaLanguageVersion.of(modJavaVersion).asInt())
    }

    tasks.withType<Jar> {
        val now = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(java.util.Date())
        manifest {
            attributes(mapOf(
                "Specification-Title" to modName,
                "Specification-Vendor" to modAuthor,
                "Specification-Version" to specificationVersion,
                "Implementation-Title" to name,
                "Implementation-Version" to archiveVersion,
                "Implementation-Vendor" to modAuthor,
                "Implementation-Timestamp" to now,
            ))
        }
    }
}
