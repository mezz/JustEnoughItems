package mezz.jei.gradle

import groovy.json.JsonSlurper
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.UntrackedTask
import java.io.File

@UntrackedTask(because = "Always validates reports generated against the latest published API jars.")
abstract class ValidateApiCompatibilityReports : DefaultTask() {
	private val topLevelNonExtendableAnnotation = Regex(
		"""(?m)^@ApiStatus\.NonExtendable\s*\Rpublic\s+(?:sealed\s+)?(?:class|interface|enum|record)\b"""
	)

	@get:InputFiles
	@get:PathSensitive(PathSensitivity.NONE)
	abstract val reportFiles: ConfigurableFileCollection

	@get:InputFiles
	@get:PathSensitive(PathSensitivity.RELATIVE)
	abstract val apiSourceFiles: ConfigurableFileCollection

	@TaskAction
	fun validateReports() {
		val failures = mutableListOf<String>()
		val allowedExceptions = mutableListOf<String>()

		for (reportFile in reportFiles.files) {
			val reportText = reportFile.readText().trim()
			if (reportText.isEmpty() || reportText == "{}") {
				continue
			}

			val reportData = JsonSlurper().parseText(reportText) as? Map<*, *>
				?: throw GradleException("API compatibility report has an unexpected format: ${reportFile.absolutePath}")

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
		}

		if (allowedExceptions.isNotEmpty()) {
			logger.lifecycle("Allowed ${allowedExceptions.size} non-extendable API compatibility exception(s):")
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
			if (isAllowedNonExtendableChange(kind, message, nonExtendableApi)) {
				allowedExceptions.add(formattedMessage)
			} else {
				failures.add(formattedMessage)
			}
		}
	}

	private fun isAllowedNonExtendableChange(
		kind: String,
		message: String,
		nonExtendableApi: Boolean
	): Boolean {
		if (!nonExtendableApi) {
			return false
		}

		return when (kind) {
			"class" ->
				message == "@org.jetbrains.annotations.ApiStatus\$NonExtendable() - Annotation was added" ||
					message == "Class was made final"
			"method" -> message == "Method was made abstract" || message == "Method was made final"
			else -> false
		}
	}

	private fun isNonExtendableApi(className: String): Boolean {
		if ('$' in className) {
			return false
		}

		val sourcePathSuffix = "$className.java"
		return apiSourceFiles.files.any { sourceFile ->
			val sourcePath = sourceFile.toPath().toString().replace(File.separatorChar, '/')
			if (!sourcePath.endsWith(sourcePathSuffix)) {
				return@any false
			}

			topLevelNonExtendableAnnotation.containsMatchIn(sourceFile.readText())
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
