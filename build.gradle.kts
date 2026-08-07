plugins {
    // https://plugins.gradle.org/plugin/com.diffplug.gradle.spotless
	id("com.diffplug.spotless") version("8.8.0")

	// https://maven.fabricmc.net/fabric-loom/fabric-loom.gradle.plugin/maven-metadata.xml
	id("fabric-loom") version("1.17.18") apply(false)

    // https://maven.fabricmc.net/net/fabricmc/fabric-loom-companion/net.fabricmc.fabric-loom-companion.gradle.plugin/maven-metadata.xml
    // applying this to all projects allows loom projects to access the required data in a manner that follows Gradle's best practices.
    id("net.fabricmc.fabric-loom-companion") version("1.17.18")

    // https://projects.neoforged.net/neoforged/moddevgradle
    id("net.neoforged.moddev") version("2.0.143") apply(false)

    id("net.mezzdev.modshade") version("0.3.0") apply(false)

    // https://plugins.gradle.org/plugin/me.modmuss50.mod-publish-plugin
    id("me.modmuss50.mod-publish-plugin") version("2.0.1") apply(false)

    id("mezz.jei.api-compatibility")
}
apply {
	from("buildtools/ColoredOutput.gradle")
}

repositories {
    mavenCentral()
}

spotless {
	java {
		target("*/src/*/java/mezz/jei/**/*.java")

		endWithNewline()
		trimTrailingWhitespace()
		removeUnusedImports()
		forbidWildcardImports()
		replaceRegex(
			"single-line if block formatting",
			"""(?m)^([ \t]*)if[ \t]*(\([^{}\r\n]+\))[ \t]*\{[ \t]*([^{}\r\n]+?)[ \t]*}${'$'}""",
			"${'$'}1if ${'$'}2 {\n${'$'}1\t${'$'}3\n${'$'}1}"
		)
		leadingSpacesToTabs(4)
		replaceRegex("class-level javadoc indentation fix", "^\\*", " *")
		replaceRegex("method-level javadoc indentation fix", "\t\\*", "\t *")
	}
}
