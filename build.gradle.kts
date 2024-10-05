plugins {
    // https://plugins.gradle.org/plugin/com.diffplug.gradle.spotless
	id("com.diffplug.spotless") version("6.25.0")

    // https://plugins.gradle.org/plugin/com.dorongold.task-tree
    id("com.dorongold.task-tree") version("4.0.0")

    // https://maven.fabricmc.net/fabric-loom/fabric-loom.gradle.plugin/maven-metadata.xml
    id("fabric-loom") version("1.8.0-alpha.16") apply(false)

    // https://projects.neoforged.net/neoforged/moddevgradle
    id("net.neoforged.moddev") version("2.0.26-beta") apply(false)

    // https://plugins.gradle.org/plugin/me.modmuss50.mod-publish-plugin
    id("me.modmuss50.mod-publish-plugin") version("0.7.3") apply(false)

    // https://files.minecraftforge.net/net/minecraftforge/gradle/ForgeGradle/index.html
    id("net.minecraftforge.gradle") version("6.0.26") apply(false)

    // https://mvnrepository.com/artifact/org.parchmentmc.librarian.forgegradle/org.parchmentmc.librarian.forgegradle.gradle.plugin
    id("org.parchmentmc.librarian.forgegradle") version("1.2.0") apply(false)
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
val fabricApiVersion: String by extra
val fabricApiVersionRange: String by extra
val fabricLoaderVersion: String by extra
val fabricLoaderVersionRange: String by extra
val forgeVersion: String by extra
val forgeVersionRange: String by extra
val githubUrl: String by extra
val forgeLoaderVersionRange: String by extra
val neoforgeVersionRange: String by extra
val neoforgeLoaderVersionRange: String by extra
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
        indentWithTabs(4)
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
            "curseHomepageUrl" to curseHomepageUrl,
            "fabricApiVersion" to fabricApiVersion,
            "fabricApiVersionRange" to fabricApiVersionRange,
            "fabricLoaderVersion" to fabricLoaderVersion,
            "fabricLoaderVersionRange" to fabricLoaderVersionRange,
            "forgeVersionRange" to forgeVersionRange,
            "githubUrl" to githubUrl,
            "forgeLoaderVersionRange" to forgeLoaderVersionRange,
            "neoforgeVersionRange" to neoforgeVersionRange,
            "neoforgeLoaderVersionRange" to neoforgeLoaderVersionRange,
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
        filesMatching(listOf("META-INF/mods.toml", "META-INF/neoforge.mods.toml", "pack.mcmeta", "fabric.mod.json")) {
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
