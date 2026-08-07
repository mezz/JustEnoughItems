package mezz.jei.gradle

import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.api.tasks.bundling.AbstractArchiveTask
import org.gradle.api.tasks.bundling.Jar
import org.gradle.external.javadoc.StandardJavadocDocletOptions
import org.gradle.language.jvm.tasks.ProcessResources

class JeiProjectPlugin : Plugin<Project> {
	override fun apply(project: Project) {
		project.configureJeiProject()
	}
}

private fun Project.configureJeiProject() {
	val buildNumber = findProperty("BUILD_NUMBER") ?: "9999"
	val specificationVersion = property("specificationVersion").toString()
	val modGroup = property("modGroup").toString()
	val modJavaVersion = property("modJavaVersion").toString()
	val modName = property("modName").toString()
	val modAuthor = property("modAuthor").toString()
	val curseHomepageUrl = property("curseHomepageUrl")
	val fabricApiVersion = property("fabricApiVersion")
	val fabricApiVersionRange = property("fabricApiVersionRange")
	val fabricLoaderVersion = property("fabricLoaderVersion")
	val fabricLoaderVersionRange = property("fabricLoaderVersionRange")
	val githubUrl = property("githubUrl")
	val neoforgeVersionRange = findProperty("neoforgeVersionRange")?.toString().orEmpty()
	val neoforgeLoaderVersionRange = findProperty("neoforgeLoaderVersionRange")?.toString().orEmpty()
	val minecraftVersion = property("minecraftVersion")
	val minecraftVersionRange = property("minecraftVersionRange")
	val modDescription = property("modDescription")
	val modId = property("modId")

	version = "$specificationVersion.$buildNumber"
	group = modGroup

	tasks.withType(Javadoc::class.java).configureEach {
		// workaround cast for https://github.com/gradle/gradle/issues/7038
		val standardJavadocDocletOptions = options as StandardJavadocDocletOptions
		// prevent java 8's strict doclint for javadocs from failing builds
		standardJavadocDocletOptions.addStringOption("Xdoclint:none", "-quiet")
	}

	tasks.withType(JavaCompile::class.java).configureEach {
		options.encoding = "UTF-8"
		options.release.set(JavaVersion.toVersion(modJavaVersion).majorVersion.toInt())
		options.isDeprecation = true
		options.compilerArgs.add("-Xlint:unchecked")
	}

	tasks.withType(Jar::class.java).configureEach {
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

	tasks.withType(ProcessResources::class.java).configureEach {
		val resourceProperties = mapOf(
			"curseHomepageUrl" to curseHomepageUrl,
			"fabricApiVersion" to fabricApiVersion,
			"fabricApiVersionRange" to fabricApiVersionRange,
			"fabricLoaderVersion" to fabricLoaderVersion,
			"fabricLoaderVersionRange" to fabricLoaderVersionRange,
			"githubUrl" to githubUrl,
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
		inputs.properties(resourceProperties)
		filesMatching(listOf("META-INF/neoforge.mods.toml", "pack.mcmeta", "fabric.mod.json")) {
			expand(resourceProperties)
		}
	}

	// Activate reproducible builds
	// https://docs.gradle.org/current/userguide/working_with_files.html#sec:reproducible_archives
	tasks.withType(AbstractArchiveTask::class.java).configureEach {
		isPreserveFileTimestamps = false
		isReproducibleFileOrder = true
	}
}
