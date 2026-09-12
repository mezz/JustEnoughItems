package mezz.jei.gradle

import org.gradle.api.Project

fun Project.gradleProperty(name: String): String =
	providers.gradleProperty(name).get()

fun Project.optionalGradleProperty(name: String): String? =
	providers.gradleProperty(name).orNull

fun Project.mezzConfigDependency(artifact: String): String =
	"${gradleProperty("configModGroup")}:${gradleProperty("configModId")}-${gradleProperty("minecraftVersion")}-$artifact:${gradleProperty("mezzConfigVersion")}"

fun dependencyInfo(notation: String): Map<String, String> {
	val (groupId, artifactId, version) = notation.split(":")
	return mapOf("groupId" to groupId, "artifactId" to artifactId, "version" to version)
}
