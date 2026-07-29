package mezz.jei.gradle

import groovy.json.JsonSlurper
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ModuleDependency
import org.gradle.api.attributes.Bundling
import org.gradle.api.attributes.Usage
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.jvm.tasks.Jar
import org.gradle.language.base.plugins.LifecycleBasePlugin
import org.gradle.process.CommandLineArgumentProvider
import java.io.File
import java.util.concurrent.TimeUnit

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
	repositories.maven {
		url = uri("https://maven.blamejared.com")
		content {
			includeGroup("mezz.jei")
		}
	}

	val minecraftVersion = property("minecraftVersion").toString()
	val modGroup = property("modGroup").toString()
	val modId = property("modId").toString()
	val specificationVersion = property("specificationVersion").toString()

	val apiCompatibilityCheckerVersion = "0.1.15"
	val apiCompatibilityAsmVersion = "9.10.1"
	val apiCompatibilityMajorVersion = specificationVersion.substringBefore('.').toInt()
	val apiCompatibilityBaselineVersion = providers.gradleProperty("apiCompatibilityBaselineVersion")
		.orElse("[$apiCompatibilityMajorVersion.0.0,${apiCompatibilityMajorVersion + 1}.0.0)")

	val apiCompatibilityChecker = configurations.create("apiCompatibilityChecker") {
		description = "Runtime classpath for JEI's API compatibility checker."
		isCanBeConsumed = false
		isCanBeResolved = true
		attributes {
			attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage::class.java, Usage.JAVA_RUNTIME))
			attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling::class.java, Bundling.EXTERNAL))
		}
		resolutionStrategy.eachDependency {
			if (requested.group == "org.ow2.asm") {
				useVersion(apiCompatibilityAsmVersion)
				because("ASM 9.7 cannot read Java 25 class files.")
			}
		}
	}

	dependencies.add(apiCompatibilityChecker.name, "net.neoforged:jarcompatibilitychecker:$apiCompatibilityCheckerVersion")
	dependencies.add(apiCompatibilityChecker.name, "org.ow2.asm:asm:$apiCompatibilityAsmVersion")
	dependencies.add(apiCompatibilityChecker.name, "org.ow2.asm:asm-analysis:$apiCompatibilityAsmVersion")
	dependencies.add(apiCompatibilityChecker.name, "org.ow2.asm:asm-commons:$apiCompatibilityAsmVersion")
	dependencies.add(apiCompatibilityChecker.name, "org.ow2.asm:asm-tree:$apiCompatibilityAsmVersion")

	val apiCompatibilityModules = listOf(
		ApiCompatibilityModule(":CommonApi", "checkCommonApiCompatibility", "common-api"),
		ApiCompatibilityModule(":FabricApi", "checkFabricApiCompatibility", "fabric-api"),
		ApiCompatibilityModule(":NeoForgeApi", "checkNeoForgeApiCompatibility", "neoforge-api"),
	)

	val apiCompatibilityCheckTasks = apiCompatibilityModules.map { module ->
		val apiProject = evaluationDependsOn(module.projectPath)
		val artifactId = "$modId-$minecraftVersion-${module.artifactSuffix}"
		val baselineConfiguration = configurations.create("${module.taskName}Baseline") {
			description = "Published baseline artifact for ${apiProject.path} API compatibility checks."
			isCanBeConsumed = false
			isCanBeResolved = true
			resolutionStrategy.cacheDynamicVersionsFor(0, TimeUnit.SECONDS)
		}
		val baselineDependency = dependencies.add(baselineConfiguration.name, "$modGroup:$artifactId:${apiCompatibilityBaselineVersion.get()}")
		if (baselineDependency is ModuleDependency) {
			baselineDependency.isTransitive = false
		}

		val jarTask = apiProject.tasks.named("jar", Jar::class.java)
		val inputJar = jarTask.flatMap { it.archiveFile }
		val outputFile = layout.buildDirectory.file("reports/apiCompatibility/${module.artifactSuffix}.json")

		val checkerTask = tasks.register("run${module.taskName.replaceFirstChar { it.uppercase() }}", JavaExec::class.java) {
			dependsOn(jarTask)
			classpath = apiCompatibilityChecker
			mainClass.set("net.neoforged.jarcompatibilitychecker.ConsoleTool")

			inputs.property("apiCompatibilityBaselineVersion", apiCompatibilityBaselineVersion)
			outputs.upToDateWhen { false }

			val checkerArguments = objects.newInstance(JarCompatibilityCheckerArgumentProvider::class.java)
			checkerArguments.baselineJar.from(baselineConfiguration)
			checkerArguments.inputJar.set(inputJar)
			checkerArguments.outputFile.set(outputFile)
			argumentProviders.add(checkerArguments)
		}

		tasks.register(module.taskName, ApiCompatibilityReportCheck::class.java) {
			group = LifecycleBasePlugin.VERIFICATION_GROUP
			description = "Checks ${apiProject.path} against the latest published $artifactId API jar in the same major version."

			dependsOn(checkerTask)
			reportFile.set(outputFile)
			apiSourceFiles.from(apiProject.layout.projectDirectory.dir("src/main/java").asFileTree.matching {
				include("**/*.java")
			})
		}
	}

	tasks.register("checkApiCompatibility") {
		group = LifecycleBasePlugin.VERIFICATION_GROUP
		description = "Checks all published JEI API jars for compatibility with the latest published API jars in the same major version."
		dependsOn(apiCompatibilityCheckTasks)
	}
}

abstract class JarCompatibilityCheckerArgumentProvider : CommandLineArgumentProvider {
	@get:Classpath
	abstract val baselineJar: ConfigurableFileCollection

	@get:InputFile
	@get:PathSensitive(PathSensitivity.RELATIVE)
	abstract val inputJar: RegularFileProperty

	@get:OutputFile
	abstract val outputFile: RegularFileProperty

	override fun asArguments(): Iterable<String> = listOf(
		"--api",
		"--base-jar",
		baselineJar.singleFile.absolutePath,
		"--input-jar",
		inputJar.get().asFile.absolutePath,
		"--output",
		outputFile.get().asFile.absolutePath,
	)
}

private data class ApiCompatibilityModule(
	val projectPath: String,
	val taskName: String,
	val artifactSuffix: String,
)

abstract class ApiCompatibilityReportCheck : DefaultTask() {
	private val nonExtendableAnnotation = Regex("""@\s*(?:ApiStatus\.)?NonExtendable\b""")

	@get:InputFile
	@get:PathSensitive(PathSensitivity.NONE)
	abstract val reportFile: RegularFileProperty

	@get:InputFiles
	@get:PathSensitive(PathSensitivity.RELATIVE)
	abstract val apiSourceFiles: ConfigurableFileCollection

	@TaskAction
	fun checkReport() {
		val report = reportFile.get().asFile
		if (!report.isFile) {
			throw GradleException("API compatibility report was not created: ${report.absolutePath}")
		}

		val reportText = report.readText().trim()
		if (reportText.isEmpty() || reportText == "{}") {
			return
		}

		val reportData = JsonSlurper().parseText(reportText) as? Map<*, *>
			?: throw GradleException("API compatibility report has an unexpected format: expected a JSON object.")

		val failures = mutableListOf<String>()
		val allowedExceptions = mutableListOf<String>()

		for ((classNameValue, incompatibilitiesValue) in reportData) {
			val className = classNameValue.toString()
			val incompatibilities = incompatibilitiesValue as? Map<*, *> ?: continue
			val nonExtendableApi = isNonExtendableApi(className)

			collectIncompatibilities(
				failures,
				allowedExceptions,
				className,
				"class",
				null,
				incompatibilities["classIncompatibilities"],
				nonExtendableApi
			)
			collectMemberIncompatibilities(
				failures,
				allowedExceptions,
				className,
				"method",
				incompatibilities["methodIncompatibilities"],
				nonExtendableApi
			)
			collectMemberIncompatibilities(
				failures,
				allowedExceptions,
				className,
				"field",
				incompatibilities["fieldIncompatibilities"],
				nonExtendableApi
			)
		}

		if (allowedExceptions.isNotEmpty()) {
			logger.lifecycle("Allowed ${allowedExceptions.size} non-extensible API compatibility exception(s):")
			allowedExceptions.forEach {
				logger.lifecycle("  - $it")
			}
		}

		if (failures.isNotEmpty()) {
			throw GradleException(buildString {
				appendLine("API compatibility check failed with ${failures.size} error(s):")
				failures.forEach {
					appendLine("  - $it")
				}
			})
		}
	}

	private fun collectMemberIncompatibilities(
		failures: MutableList<String>,
		allowedExceptions: MutableList<String>,
		className: String,
		kind: String,
		memberIncompatibilities: Any?,
		nonExtendableApi: Boolean
	) {
		val members = memberIncompatibilities as? Map<*, *> ?: return
		for ((memberNameValue, incompatibilitiesValue) in members) {
			collectIncompatibilities(
				failures,
				allowedExceptions,
				className,
				kind,
				memberNameValue?.toString(),
				incompatibilitiesValue,
				nonExtendableApi
			)
		}
	}

	private fun collectIncompatibilities(
		failures: MutableList<String>,
		allowedExceptions: MutableList<String>,
		className: String,
		kind: String,
		memberName: String?,
		incompatibilitiesValue: Any?,
		nonExtendableApi: Boolean
	) {
		val incompatibilities = incompatibilitiesValue as? Iterable<*> ?: return
		for (incompatibilityValue in incompatibilities) {
			val incompatibility = incompatibilityValue as? Map<*, *> ?: continue
			val message = incompatibility["message"]?.toString() ?: continue
			val isError = incompatibility["isError"] as? Boolean ?: true
			if (!isError) {
				continue
			}

			val formattedMessage = formatIncompatibility(className, kind, memberName, message)
			if (isAllowedNonExtensibleChange(kind, message, nonExtendableApi)) {
				allowedExceptions.add(formattedMessage)
			} else {
				failures.add(formattedMessage)
			}
		}
	}

	private fun isAllowedNonExtensibleChange(
		kind: String,
		message: String,
		nonExtendableApi: Boolean
	): Boolean {
		if (!nonExtendableApi) {
			return false
		}

		return when (kind) {
			"class" -> message == "Class was made final"
			"method" -> message == "Method was made abstract" || message == "Method was made final"
			else -> false
		}
	}

	private fun isNonExtendableApi(className: String): Boolean {
		if ('$' in className) {
			// Do not inherit @NonExtendable from a top-level type to nested types.
			// Some nested API types, like listeners, are implemented by addons.
			return false
		}

		val sourcePathSuffix = "$className.java"
		return apiSourceFiles.files.any { sourceFile ->
			val sourcePath = sourceFile.toPath().toString().replace(File.separatorChar, '/')
			if (!sourcePath.endsWith(sourcePathSuffix)) {
				return@any false
			}

			val sourceText = sourceFile.readText()
			nonExtendableAnnotation.containsMatchIn(sourceText)
		}
	}

	private fun formatIncompatibility(
		className: String,
		kind: String,
		memberName: String?,
		message: String
	): String {
		val dottedClassName = className.replace('/', '.')
		return if (memberName == null) {
			"$dottedClassName: $message"
		} else {
			"$dottedClassName $kind $memberName: $message"
		}
	}
}
