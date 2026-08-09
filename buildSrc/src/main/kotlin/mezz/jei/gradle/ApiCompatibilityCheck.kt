package mezz.jei.gradle

import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ModuleDependency
import org.gradle.api.attributes.Bundling
import org.gradle.api.attributes.Usage
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskProvider
import org.gradle.language.base.plugins.LifecycleBasePlugin
import org.gradle.process.CommandLineArgumentProvider

internal data class ApiCompatibilityModule(
	val projectPath: String,
	val artifactSuffix: String,
) {
	val taskName = "check${projectPath.removePrefix(":")}Compatibility"
	val baselineTaskName = "resolve${taskName.removePrefix("check")}Baseline"
}

internal fun Project.registerApiCompatibilityCheck(
	module: ApiCompatibilityModule,
	checkerClasspath: Configuration,
	baselineRepository: String,
	baselineGroup: String,
	artifactPrefix: String,
	baselineVersion: Provider<String>,
): TaskProvider<JavaExec> {
	val artifactId = "$artifactPrefix-${module.artifactSuffix}"
	val inputConfiguration = createApiInputConfiguration(module)
	val resolveBaselineTask = tasks.register(
		module.baselineTaskName,
		ResolveApiCompatibilityBaseline::class.java,
	) {
		description = "Resolves the latest published $artifactId API jar in the same major version."
		repositoryUrl.set(baselineRepository)
		groupId.set(baselineGroup)
		this.artifactId.set(artifactId)
		versionSelector.set(baselineVersion)
		offline.set(gradle.startParameter.isOffline)
		baselineJar.set(layout.buildDirectory.file("apiCompatibility/baselines/${module.artifactSuffix}.jar"))
		resolvedVersionFile.set(layout.buildDirectory.file("apiCompatibility/baselines/${module.artifactSuffix}.version"))
	}

	val inputJar = inputConfiguration.incoming.files.elements.map { it.single().asFile }
	val outputFile = layout.buildDirectory.file("reports/apiCompatibility/${module.artifactSuffix}.json")

	return tasks.register(module.taskName, JavaExec::class.java) {
		group = LifecycleBasePlugin.VERIFICATION_GROUP
		description = "Checks ${module.projectPath} against the latest published $artifactId API jar in the same major version."

		classpath = checkerClasspath
		mainClass.set("net.neoforged.jarcompatibilitychecker.ConsoleTool")
		inputs.property("apiCompatibilityBaselineVersion", baselineVersion)

		val checkerArguments = objects.newInstance(JarCompatibilityCheckerArgumentProvider::class.java)
		checkerArguments.baselineJar.from(resolveBaselineTask.flatMap { it.baselineJar })
		checkerArguments.resolvedBaselineVersion.set(resolveBaselineTask.flatMap { it.resolvedVersionFile })
		checkerArguments.inputJar.fileProvider(inputJar)
		checkerArguments.outputFile.set(outputFile)
		argumentProviders.add(checkerArguments)
	}
}

private fun Project.createApiInputConfiguration(module: ApiCompatibilityModule): Configuration {
	val inputConfiguration = configurations.create("${module.taskName}Input") {
		description = "Current ${module.projectPath} API jar for compatibility checks."
		isCanBeConsumed = false
		isCanBeResolved = true
		attributes {
			attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage::class.java, Usage.JAVA_RUNTIME))
			attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling::class.java, Bundling.EXTERNAL))
		}
	}

	val inputDependency = dependencies.add(inputConfiguration.name, dependencies.project(module.projectPath))
	if (inputDependency is ModuleDependency) {
		inputDependency.isTransitive = false
	}
	return inputConfiguration
}

abstract class JarCompatibilityCheckerArgumentProvider : CommandLineArgumentProvider {
	@get:Classpath
	abstract val baselineJar: ConfigurableFileCollection

	@get:InputFile
	@get:PathSensitive(PathSensitivity.NONE)
	abstract val resolvedBaselineVersion: RegularFileProperty

	@get:InputFile
	@get:PathSensitive(PathSensitivity.RELATIVE)
	abstract val inputJar: RegularFileProperty

	@get:OutputFile
	abstract val outputFile: RegularFileProperty

	override fun asArguments(): Iterable<String> = listOf(
		"--api",
		"--non-extendable-api-check-mode",
		"SKIP",
		"--fail",
		"--base-jar",
		baselineJar.singleFile.absolutePath,
		"--input-jar",
		inputJar.get().asFile.absolutePath,
		"--output",
		outputFile.get().asFile.absolutePath,
	)
}
