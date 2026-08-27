import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
	id("com.diffplug.spotless") version("8.10.0")
    id("com.dorongold.task-tree") version("4.0.2")
    id("org.spongepowered.gradle.vanilla") version("0.2.2") apply(false)
    id("net.neoforged.moddev.legacyforge") version("2.0.144") apply(false)
    id("net.mezzdev.modshade") version("0.6.0") apply(false)
    id("me.modmuss50.mod-publish-plugin") version("2.2.0") apply(false)
    id("fabric-loom") version("1.17.20") apply(false)
}
apply {
	from("buildtools/ColoredOutput.gradle")
}

repositories {
    mavenCentral()
}

// gradle.properties
val curseHomepageUrl: String by extra
val curseProjectId: String by extra
val amecsKeyModifiersVersionFabric: String by extra
val amecsVersionFabric: String by extra
val fabricApiVersion: String by extra
val fabricLoaderVersion: String by extra
val forgeVersion: String by extra
val forgeVersionRange: String by extra
val forgeLoaderVersionRange: String by extra
val githubUrl: String by extra
val parchmentVersionForge: String by extra
val minecraftVersion: String by extra
val minecraftVersionRange: String by extra
val modAuthor: String by extra
val modDescription: String by extra
val modGroup: String by extra
val modId: String by extra
val modJavaVersion: String by extra
val modName: String by extra
val specificationVersion: String by extra

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

    tasks.withType<Test>().configureEach {
        testLogging {
            events = setOf(TestLogEvent.FAILED)
            exceptionFormat = TestExceptionFormat.FULL
        }
    }

    tasks.withType<Jar> {
        manifest {
            attributes(mapOf(
                "Specification-Title" to modName,
                "Specification-Vendor" to modAuthor,
                "Specification-Version" to specificationVersion,
                "Implementation-Title" to name,
                "Implementation-Version" to archiveVersion,
                "Implementation-Vendor" to modAuthor
            ))
        }
    }

    tasks.withType<ProcessResources> {
        val properties = mapOf(
            "amecsKeyModifiersVersionFabric" to amecsKeyModifiersVersionFabric,
            "amecsVersionFabric" to amecsVersionFabric,
            "curseHomepageUrl" to curseHomepageUrl,
            "fabricApiVersion" to fabricApiVersion,
            "fabricLoaderVersion" to fabricLoaderVersion,
            "forgeVersionRange" to forgeVersionRange,
            "githubUrl" to githubUrl,
            "forgeLoaderVersionRange" to forgeLoaderVersionRange,
            "minecraftVersion" to minecraftVersion,
            "minecraftVersionRange" to minecraftVersionRange,
            "modAuthor" to modAuthor,
            "modDescription" to modDescription,
            "modId" to modId,
            "modJavaVersion" to modJavaVersion,
            "modName" to modName,
            "version" to version,
        )
        inputs.properties(properties)
        filesMatching(listOf("META-INF/mods.toml", "pack.mcmeta", "fabric.mod.json")) {
            expand(properties)
        }
    }

    // Activate reproducible builds
    // https://docs.gradle.org/current/userguide/working_with_files.html#sec:reproducible_archives
    tasks.withType<AbstractArchiveTask>().configureEach {
        isPreserveFileTimestamps = false
        isReproducibleFileOrder = true
    }
}

apply(from = "gradle/api-compatibility.gradle.kts")
