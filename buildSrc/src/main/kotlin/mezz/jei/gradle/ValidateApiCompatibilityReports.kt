package mezz.jei.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.UntrackedTask

@UntrackedTask(because = "Always validates reports generated against the latest published API jars.")
abstract class ValidateApiCompatibilityReports : DefaultTask() {
	@get:InputFiles
	@get:PathSensitive(PathSensitivity.NONE)
	abstract val reportFiles: ConfigurableFileCollection

	@TaskAction
	fun validateReports() {
		val incompatibleReports = reportFiles.files.filter { reportFile ->
			reportFile.readText().trim() != "{}"
		}
		if (incompatibleReports.isNotEmpty()) {
			throw GradleException(
				"API incompatibilities found. See ${incompatibleReports.joinToString { it.absolutePath }}"
			)
		}
	}
}
