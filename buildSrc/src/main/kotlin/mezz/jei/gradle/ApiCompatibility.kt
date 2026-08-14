package mezz.jei.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.attributes.Bundling
import org.gradle.api.attributes.Usage
import org.gradle.api.provider.Provider
import org.gradle.language.base.plugins.LifecycleBasePlugin

private const val CHECKER_VERSION = "0.1.18"
private const val BASELINE_REPOSITORY = "https://maven.blamejared.com"

private val API_MODULES = listOf(
	ApiCompatibilityModule(":CommonApi", "common-api"),
	ApiCompatibilityModule(":FabricApi", "fabric-api"),
	ApiCompatibilityModule(":NeoForgeApi", "neoforge-api"),
)

class ApiCompatibilityPlugin : Plugin<Project> {
	override fun apply(project: Project) {
		project.configureApiCompatibility()
	}
}

private fun Project.configureApiCompatibility() {
	repositories.maven {
		url = uri("https://maven.neoforged.net/releases")
		content {
			includeGroup("net.neoforged")
		}
	}

	val checkerClasspath = createCheckerClasspath()
	val artifactPrefix = "${property("modId")}-${property("minecraftVersion")}"
	val baselineGroup = property("modGroup").toString()
	val baselineVersion = apiCompatibilityBaselineVersion()
	val compatibilityChecks = API_MODULES.map { module ->
		registerApiCompatibilityCheck(
			module = module,
			checkerClasspath = checkerClasspath,
			baselineRepository = BASELINE_REPOSITORY,
			baselineGroup = baselineGroup,
			artifactPrefix = artifactPrefix,
			baselineVersion = baselineVersion,
		)
	}

	val checkApiCompatibility = tasks.register("checkApiCompatibility") {
		group = LifecycleBasePlugin.VERIFICATION_GROUP
		description = "Checks all published JEI API jars for compatibility with the latest published API jars in the same major version."
		dependsOn(compatibilityChecks)
	}

	tasks.named(LifecycleBasePlugin.CHECK_TASK_NAME) {
		dependsOn(checkApiCompatibility)
	}
}

private fun Project.createCheckerClasspath(): Configuration {
	val checkerClasspath = configurations.create("apiCompatibilityChecker") {
		description = "Runtime classpath for JEI's API compatibility checker."
		isCanBeConsumed = false
		isCanBeResolved = true
		attributes {
			attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage::class.java, Usage.JAVA_RUNTIME))
			attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling::class.java, Bundling.EXTERNAL))
		}
	}

	dependencies.add(checkerClasspath.name, "net.neoforged:jarcompatibilitychecker:$CHECKER_VERSION")
	return checkerClasspath
}

private fun Project.apiCompatibilityBaselineVersion(): Provider<String> {
	val specificationVersion = property("specificationVersion").toString()
	val majorVersion = specificationVersion.substringBefore('.').toInt()
	return providers.gradleProperty("apiCompatibilityBaselineVersion")
		.orElse("[$majorVersion.0.0,${majorVersion + 1}.0.0)")
}
