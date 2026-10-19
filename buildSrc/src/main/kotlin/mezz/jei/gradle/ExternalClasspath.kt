package mezz.jei.gradle

import org.gradle.api.Project
import org.gradle.api.artifacts.component.ProjectComponentIdentifier
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.SourceSet

/**
 * Returns the compile and runtime dependencies without JEI's own classes and jars.
 * ModShade passes the combined JEI and dependency jar to R8, with keep rules for JEI.
 * R8 still analyzes JEI's code, so it needs Minecraft and JEI's other dependencies
 * to resolve references. Exclude JEI's classes here because R8 already receives
 * them in that combined jar.
 */
fun Project.externalSourceSetClasspath(sourceSets: Iterable<SourceSet>): FileCollection {
	val libraries = objects.fileCollection()
	val projectOutputs = objects.fileCollection()
	for (sourceSet in sourceSets) {
		libraries.from(sourceSet.compileClasspath, sourceSet.runtimeClasspath)
		projectOutputs.from(sourceSet.output)
		for (name in listOf(sourceSet.compileClasspathConfigurationName, sourceSet.runtimeClasspathConfigurationName)) {
			projectOutputs.from(configurations.named(name).map { configuration ->
				configuration.incoming.artifactView {
					componentFilter { it is ProjectComponentIdentifier }
				}.files
			})
		}
	}
	return libraries.minus(projectOutputs)
}
