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
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.UntrackedTask
import org.gradle.language.base.plugins.LifecycleBasePlugin
import org.gradle.process.CommandLineArgumentProvider
import java.io.File
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.security.MessageDigest
import java.time.Duration

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
	val minecraftVersion = property("minecraftVersion").toString()
	val apiCompatibilityMinecraftVersion = providers.gradleProperty("apiCompatibilityMinecraftVersion")
		.orElse(minecraftVersion)
	val modGroup = property("modGroup").toString()
	val modId = property("modId").toString()
	val specificationVersion = property("specificationVersion").toString()

	val apiCompatibilityCheckerVersion = "0.1.15"
	val apiCompatibilityAsmVersion = "9.10.1"
	val apiCompatibilityBaselineRepository = "https://maven.blamejared.com"
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
	)

	val apiCompatibilityCheckTasks = apiCompatibilityModules.map { module ->
		val artifactId = "$modId-${apiCompatibilityMinecraftVersion.get()}-${module.artifactSuffix}"
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

		val resolveBaselineTask = tasks.register(
			"resolve${module.taskName.removePrefix("check")}Baseline",
			ResolveApiCompatibilityBaseline::class.java,
		) {
			description = "Resolves the latest published $artifactId API jar in the same major version."
			repositoryUrl.set(apiCompatibilityBaselineRepository)
			groupId.set(modGroup)
			this.artifactId.set(artifactId)
			versionSelector.set(apiCompatibilityBaselineVersion)
			offline.set(gradle.startParameter.isOffline)
			baselineJar.set(layout.buildDirectory.file("apiCompatibility/baselines/${module.artifactSuffix}.jar"))
			resolvedVersionFile.set(layout.buildDirectory.file("apiCompatibility/baselines/${module.artifactSuffix}.version"))
		}

		val apiInputJar = inputConfiguration.incoming.files.elements.map { it.single().asFile }
		val outputFile = layout.buildDirectory.file("reports/apiCompatibility/${module.artifactSuffix}.json")

		val checkerTask = tasks.register("${module.taskName}WithJarCompatibilityChecker", JavaExec::class.java) {
			group = LifecycleBasePlugin.VERIFICATION_GROUP
			description = "Runs JarCompatibilityChecker for ${module.projectPath} against the latest published $artifactId API jar in the same major version."

			classpath = apiCompatibilityChecker
			mainClass.set("net.neoforged.jarcompatibilitychecker.ConsoleTool")

			inputs.property("apiCompatibilityBaselineVersion", apiCompatibilityBaselineVersion)

			val checkerArguments = objects.newInstance(JarCompatibilityCheckerArgumentProvider::class.java)
			checkerArguments.baselineJar.from(resolveBaselineTask.flatMap { it.baselineJar })
			checkerArguments.resolvedBaselineVersion.set(resolveBaselineTask.flatMap { it.resolvedVersionFile })
			checkerArguments.inputJar.fileProvider(apiInputJar)
			checkerArguments.outputFile.set(outputFile)
			argumentProviders.add(checkerArguments)
		}

		tasks.register(module.taskName, ValidateApiCompatibilityReport::class.java) {
			group = LifecycleBasePlugin.VERIFICATION_GROUP
			description = "Checks ${module.projectPath} against the latest published $artifactId API jar in the same major version."

			dependsOn(checkerTask)
			reportFile.set(outputFile)
			val apiProjectDirectory = isolatedProjectDirectory(module.projectPath)
			sourceFiles.from(apiProjectDirectory.dir("src/main/java").asFileTree.matching {
				include("**/*.java")
			})
		}
	}

	tasks.register("checkApiCompatibility") {
		group = LifecycleBasePlugin.VERIFICATION_GROUP
		description = "Checks all published JEI API jars for compatibility with the latest published API jars in the same major version."
		dependsOn(apiCompatibilityCheckTasks)
	}

	tasks.named(LifecycleBasePlugin.CHECK_TASK_NAME) {
		dependsOn("checkApiCompatibility")
	}
}

@UntrackedTask(because = "Always checks Maven metadata for the latest published API baseline.")
abstract class ResolveApiCompatibilityBaseline : DefaultTask() {
	@get:Input
	abstract val repositoryUrl: Property<String>

	@get:Input
	abstract val groupId: Property<String>

	@get:Input
	abstract val artifactId: Property<String>

	@get:Input
	abstract val versionSelector: Property<String>

	@get:Input
	abstract val offline: Property<Boolean>

	@get:OutputFile
	abstract val baselineJar: RegularFileProperty

	@get:OutputFile
	abstract val resolvedVersionFile: RegularFileProperty

	@TaskAction
	fun resolveBaseline() {
		val artifactId = artifactId.get()
		val versionSelector = versionSelector.get()
		val baselineJar = baselineJar.get().asFile
		val resolvedVersionFile = resolvedVersionFile.get().asFile
		val previousVersion = resolvedVersionFile.takeIf(File::isFile)?.readText()?.trim()
		if (offline.get()) {
			if (previousVersion != null && baselineJar.isFile && matchesVersionSelector(previousVersion, versionSelector)) {
				logger.lifecycle("Using offline API compatibility baseline {}:{}:{}", groupId.get(), artifactId, previousVersion)
				return
			}
			throw GradleException("No cached API compatibility baseline for $artifactId matches $versionSelector")
		}

		val client = HttpClient.newBuilder()
			.connectTimeout(Duration.ofSeconds(30))
			.followRedirects(HttpClient.Redirect.NORMAL)
			.build()
		val repositoryUrl = repositoryUrl.get().trimEnd('/')
		val groupPath = groupId.get().replace('.', '/')
		val artifactBaseUrl = "$repositoryUrl/$groupPath/$artifactId"
		val resolvedVersion = if (versionSelector.startsWith('[') || versionSelector.startsWith('(')) {
			val metadata = download(client, URI.create("$artifactBaseUrl/maven-metadata.xml")).toString(Charsets.UTF_8)
			selectLatestVersion(metadata, versionSelector)
		} else {
			versionSelector
		}

		if (previousVersion == resolvedVersion && baselineJar.isFile) {
			logger.info("Using cached API compatibility baseline {}:{}:{}", groupId.get(), artifactId, resolvedVersion)
			return
		}

		val artifactFileName = "$artifactId-$resolvedVersion.jar"
		val artifactUrl = "$artifactBaseUrl/$resolvedVersion/$artifactFileName"
		val expectedChecksum = download(client, URI.create("$artifactUrl.sha256"))
			.toString(Charsets.UTF_8)
			.trim()
			.substringBefore(' ')
		val artifact = download(client, URI.create(artifactUrl))
		val actualChecksum = MessageDigest.getInstance("SHA-256")
			.digest(artifact)
			.joinToString("") { "%02x".format(it.toInt() and 0xff) }
		if (!actualChecksum.equals(expectedChecksum, ignoreCase = true)) {
			throw GradleException(
				"Checksum verification failed for $artifactUrl: expected $expectedChecksum but got $actualChecksum"
			)
		}

		baselineJar.parentFile.mkdirs()
		baselineJar.writeBytes(artifact)
		resolvedVersionFile.writeText("$resolvedVersion\n")
		logger.lifecycle("Resolved API compatibility baseline {}:{}:{}", groupId.get(), artifactId, resolvedVersion)
	}

	private fun download(client: HttpClient, uri: URI): ByteArray {
		val request = HttpRequest.newBuilder(uri)
			.timeout(Duration.ofSeconds(60))
			.header("Cache-Control", "no-cache")
			.GET()
			.build()
		val response = try {
			client.send(request, HttpResponse.BodyHandlers.ofByteArray())
		} catch (e: InterruptedException) {
			Thread.currentThread().interrupt()
			throw GradleException("Interrupted while downloading $uri", e)
		} catch (e: Exception) {
			throw GradleException("Failed to download $uri", e)
		}
		if (response.statusCode() !in 200..299) {
			throw GradleException("Failed to download $uri: HTTP ${response.statusCode()}")
		}
		return response.body()
	}

	private fun selectLatestVersion(metadata: String, selector: String): String {
		if (selector.length < 3 || selector.last() !in listOf(']', ')')) {
			throw GradleException("Unsupported API compatibility baseline version range: $selector")
		}
		val bounds = selector.substring(1, selector.lastIndex).split(',', limit = 2)
		if (bounds.size != 2) {
			throw GradleException("Unsupported API compatibility baseline version range: $selector")
		}

		val lowerBound = bounds[0].takeIf(String::isNotBlank)?.let(NumericVersion::parse)
		val upperBound = bounds[1].takeIf(String::isNotBlank)?.let(NumericVersion::parse)
		val lowerInclusive = selector.first() == '['
		val upperInclusive = selector.last() == ']'
		return VERSION_PATTERN.findAll(metadata)
			.map { it.groupValues[1] }
			.mapNotNull { version -> NumericVersion.parseOrNull(version)?.let { version to it } }
			.filter { (_, version) ->
				(lowerBound == null || version > lowerBound || lowerInclusive && version == lowerBound) &&
					(upperBound == null || version < upperBound || upperInclusive && version == upperBound)
			}
			.maxByOrNull { it.second }
			?.first
			?: throw GradleException("No published API compatibility baseline matches $selector")
	}

	private fun matchesVersionSelector(version: String, selector: String): Boolean =
		if (selector.startsWith('[') || selector.startsWith('(')) {
			runCatching { selectLatestVersion("<version>$version</version>", selector) }
				.getOrNull() == version
		} else {
			version == selector
		}

	private data class NumericVersion(val components: List<Int>) : Comparable<NumericVersion> {
		override fun compareTo(other: NumericVersion): Int {
			val componentCount = maxOf(components.size, other.components.size)
			for (index in 0 until componentCount) {
				val comparison = components.getOrElse(index) { 0 }.compareTo(other.components.getOrElse(index) { 0 })
				if (comparison != 0) {
					return comparison
				}
			}
			return 0
		}

		companion object {
			fun parse(version: String): NumericVersion =
				parseOrNull(version) ?: throw GradleException("Unsupported non-numeric JEI API version: $version")

			fun parseOrNull(version: String): NumericVersion? {
				val components = version.split('.').map { it.toIntOrNull() ?: return null }
				return NumericVersion(components)
			}
		}
	}

	companion object {
		private val VERSION_PATTERN = Regex("""<version>\s*([^<\s]+)\s*</version>""")
	}
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

private data class ApiCompatibilityError(
	val className: String,
	val memberName: String?,
	val message: String,
) {
	fun format(): String =
		if (memberName == null) {
			"$className: $message"
		} else {
			"$className: $memberName - $message"
		}
}

abstract class ValidateApiCompatibilityReport : DefaultTask() {
	companion object {
		private const val ABSTRACT_METHOD_ERROR = "Method was made abstract"
	}

	private val nonExtendableAnnotation = Regex("""@\s*(?:ApiStatus\.)?NonExtendable\b""")

	@get:InputFile
	@get:PathSensitive(PathSensitivity.RELATIVE)
	abstract val reportFile: RegularFileProperty

	@get:InputFiles
	@get:PathSensitive(PathSensitivity.RELATIVE)
	abstract val sourceFiles: ConfigurableFileCollection

	@TaskAction
	fun validateReport() {
		val errors = collectApiCompatibilityErrors(reportFile.get().asFile)
		val nonExtendableInterfaces = findNonExtendableInterfaces(errors)
		val ignoredErrors = errors.filter { isIgnoredNonExtendableInterfaceError(it, nonExtendableInterfaces) }
		val unexpectedErrors = errors.filterNot { isIgnoredNonExtendableInterfaceError(it, nonExtendableInterfaces) }

		if (ignoredErrors.isNotEmpty()) {
			logger.lifecycle("Ignored known API compatibility false positives:\n{}", ignoredErrors.joinToString("\n") { it.format() })
		}

		if (unexpectedErrors.isNotEmpty()) {
			throw GradleException("API compatibility check failed:\n${unexpectedErrors.joinToString("\n") { it.format() }}")
		}
	}

	private fun collectApiCompatibilityErrors(reportFile: File): List<ApiCompatibilityError> {
		val report = JsonSlurper().parse(reportFile) as? Map<*, *> ?: return emptyList()
		val errors = mutableListOf<ApiCompatibilityError>()

		for ((classNameValue, classReportValue) in report) {
			val className = classNameValue as? String ?: continue
			val classReport = classReportValue as? Map<*, *> ?: continue

			fun collectErrors(memberName: String?, incompatibilitiesValue: Any?) {
				val incompatibilities = incompatibilitiesValue as? Iterable<*> ?: return
				for (incompatibilityValue in incompatibilities) {
					val incompatibility = incompatibilityValue as? Map<*, *> ?: continue
					if (incompatibility["isError"] == true) {
						val message = incompatibility["message"] as? String ?: incompatibility.toString()
						errors.add(ApiCompatibilityError(className, memberName, message))
					}
				}
			}

			collectErrors(null, classReport["classIncompatibilities"])

			val methodIncompatibilities = classReport["methodIncompatibilities"] as? Map<*, *>
			methodIncompatibilities?.forEach { (methodNameValue, incompatibilitiesValue) ->
				collectErrors(methodNameValue as? String, incompatibilitiesValue)
			}

			val fieldIncompatibilities = classReport["fieldIncompatibilities"] as? Map<*, *>
			fieldIncompatibilities?.forEach { (fieldNameValue, incompatibilitiesValue) ->
				collectErrors(fieldNameValue as? String, incompatibilitiesValue)
			}
		}

		return errors
	}

	// TODO Remove this once JarCompatibilityChecker accounts for @ApiStatus.NonExtendable:
	// https://github.com/neoforged/JarCompatibilityChecker/pull/5
	// Adding abstract methods to non-extendable JEI API interfaces is safe because mods should not implement them.
	private fun isIgnoredNonExtendableInterfaceError(
		error: ApiCompatibilityError,
		nonExtendableInterfaces: Set<String>,
	): Boolean =
		error.message == ABSTRACT_METHOD_ERROR &&
			error.memberName != null &&
			nonExtendableInterfaces.contains(error.className)

	private fun findNonExtendableInterfaces(errors: List<ApiCompatibilityError>): Set<String> =
		errors.asSequence()
			.map(ApiCompatibilityError::className)
			.distinct()
			.filter(::isNonExtendableInterface)
			.toSet()

	private fun isNonExtendableInterface(className: String): Boolean {
		val sourceFile = findSourceFile(className) ?: return false
		val source = sourceFile.readText()
		return nonExtendableAnnotation.containsMatchIn(source) &&
			Regex("\\binterface\\s+${Regex.escape(sourceFile.nameWithoutExtension)}\\b").containsMatchIn(source)
	}

	private fun findSourceFile(className: String): File? {
		val pathSuffix = className.replace('/', File.separatorChar) + ".java"
		return sourceFiles.files.firstOrNull { it.path.endsWith(pathSuffix) }
	}
}
