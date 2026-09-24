@file:Suppress("UnstableApiUsage")

package mezz.jei.gradle

import org.gradle.api.Project
import org.gradle.api.file.Directory

fun Project.isolatedProjectDirectory(path: String): Directory =
	project(path).isolated.projectDirectory
