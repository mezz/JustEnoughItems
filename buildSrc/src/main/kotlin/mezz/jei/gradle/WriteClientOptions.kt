package mezz.jei.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

/** Reapplies required launch options while preserving other client preferences. */
@DisableCachingByDefault(because = "Minecraft rewrites its options during client runs.")
abstract class WriteClientOptions : DefaultTask() {
	@get:InputFile
	@get:PathSensitive(PathSensitivity.NONE)
	abstract val templateFile: RegularFileProperty

	@get:OutputFile
	abstract val optionsFile: RegularFileProperty

	init {
		outputs.upToDateWhen { false }
	}

	@TaskAction
	fun writeOptions() {
		val required = templateFile.get().asFile.readLines()
			.filter { it.contains(':') }
			.associateBy { it.substringBefore(':') }
		val output = optionsFile.get().asFile
		val seen = mutableSetOf<String>()
		val lines = if (output.exists()) output.readLines().map { line ->
			val key = line.substringBefore(':')
			seen.add(key)
			required[key] ?: line
		}.toMutableList() else mutableListOf()
		lines.addAll(required.filterKeys { it !in seen }.values)
		output.parentFile.mkdirs()
		output.writeText(lines.joinToString("\n", postfix = "\n"))
	}
}
