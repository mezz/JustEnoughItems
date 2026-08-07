package mezz.jei.gradle

import org.gradle.api.Project

fun Project.gradleProperty(name: String): String =
	providers.gradleProperty(name).get()

fun Project.optionalGradleProperty(name: String): String? =
	providers.gradleProperty(name).orNull
