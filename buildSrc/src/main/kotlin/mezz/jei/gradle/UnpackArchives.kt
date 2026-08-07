package mezz.jei.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.file.ArchiveOperations
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject

abstract class UnpackArchives : DefaultTask() {
	@get:Input
	abstract val excludedPatterns: SetProperty<String>

	@get:InputFiles
	@get:PathSensitive(PathSensitivity.RELATIVE)
	abstract val archives: ConfigurableFileCollection

	@get:OutputDirectory
	abstract val outputDirectory: DirectoryProperty

	@get:Inject
	abstract val archiveOperations: ArchiveOperations

	@get:Inject
	abstract val fileSystemOperations: FileSystemOperations

	init {
		excludedPatterns.convention(emptySet())
	}

	@TaskAction
	fun unpack() {
		fileSystemOperations.sync {
			from(archives.map(archiveOperations::zipTree))
			into(outputDirectory)
			exclude(excludedPatterns.get())
			duplicatesStrategy = DuplicatesStrategy.EXCLUDE
		}
	}
}
