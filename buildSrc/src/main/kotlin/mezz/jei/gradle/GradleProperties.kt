package mezz.jei.gradle

import org.gradle.api.Project

fun Project.gradleProperty(name: String): String =
	providers.gradleProperty(name).get()

fun Project.optionalGradleProperty(name: String): String? =
	providers.gradleProperty(name).orNull

fun Project.addFabricMinecraftDependencies() {
	dependencies.add("minecraft", "com.mojang:minecraft:${gradleProperty("minecraftVersion")}")
	dependencies.add("implementation", "net.fabricmc:fabric-loader:${gradleProperty("fabricLoaderVersion")}")
	dependencies.add("compileOnly", "com.google.code.findbugs:jsr305:3.0.2")
}
