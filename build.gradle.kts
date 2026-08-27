import mezz.jei.gradle.ValidateApiCompatibilityReports
import net.neoforged.jarcompatibilitychecker.core.NonExtendableApiCheckMode
import net.neoforged.jarcompatibilitychecker.gradle.CompatibilityTask
import org.gradle.language.base.plugins.LifecycleBasePlugin

plugins {
    // https://plugins.gradle.org/plugin/com.diffplug.gradle.spotless
	id("com.diffplug.spotless") version("8.10.0")

	// https://maven.fabricmc.net/fabric-loom/fabric-loom.gradle.plugin/maven-metadata.xml
	id("fabric-loom") version("1.17.20") apply(false)

    // https://maven.fabricmc.net/net/fabricmc/fabric-loom-companion/net.fabricmc.fabric-loom-companion.gradle.plugin/maven-metadata.xml
    // applying this to all projects allows loom projects to access the required data in a manner that follows Gradle's best practices.
    id("net.fabricmc.fabric-loom-companion") version("1.17.20")

    // https://projects.neoforged.net/neoforged/moddevgradle
    id("net.neoforged.moddev") version("2.0.144") apply(false)

    id("net.mezzdev.modshade") version("0.7.0") apply(false)

    // https://plugins.gradle.org/plugin/me.modmuss50.mod-publish-plugin
    id("me.modmuss50.mod-publish-plugin") version("2.2.0") apply(false)

    id("net.neoforged.jarcompatibilitychecker") version("0.1.19") apply(false)
}
apply {
	from("buildtools/ColoredOutput.gradle")
}

repositories {
    mavenCentral()
}
spotless {
	java {
		target("*/src/*/java/mezz/jei/**/*.java")

		endWithNewline()
		trimTrailingWhitespace()
		removeUnusedImports()
		forbidWildcardImports()
		replaceRegex(
			"single-line if block formatting",
			"""(?m)^([ \t]*)if[ \t]*(\([^{}\r\n]+\))[ \t]*\{[ \t]*([^{}\r\n]+?)[ \t]*}${'$'}""",
			"${'$'}1if ${'$'}2 {\n${'$'}1\t${'$'}3\n${'$'}1}"
		)
		leadingSpacesToTabs(4)
		replaceRegex("class-level javadoc indentation fix", "^\\*", " *")
		replaceRegex("method-level javadoc indentation fix", "\t\\*", "\t *")
	}
}

val apiProjectPaths = listOf(":CommonApi", ":FabricApi", ":NeoForgeApi")
val apiCompatibilityReports = apiProjectPaths.associateWith { apiProjectPath ->
    project(apiProjectPath).layout.buildDirectory.file("checkJarCompatibility/output.json")
}
apiProjectPaths.forEach { apiProjectPath ->
    val apiProject = project(apiProjectPath)
    apiProject.pluginManager.apply("net.neoforged.jarcompatibilitychecker")
    apiProject.pluginManager.withPlugin("java") {
        apiProject.tasks.named<CompatibilityTask>("checkJarCompatibility") {
            group = LifecycleBasePlugin.VERIFICATION_GROUP
            description = "Checks $apiProjectPath against the latest published API jar in the same major version."
            output.set(apiCompatibilityReports.getValue(apiProjectPath))
            mavens.set(listOf("https://maven.blamejared.com"))
            // Match the previous CLI check and avoid loading the full Minecraft compile classpath.
            libraries.setFrom(emptyList<Any>())
            nonExtendableApiCheckMode.set(NonExtendableApiCheckMode.SKIP)
            // Do not set fail: the plugin invokes ConsoleTool in-process and its fail mode calls System.exit.
            // checkApiCompatibility validates the generated reports instead.
        }
    }
}

val checkApiCompatibility = tasks.register<ValidateApiCompatibilityReports>("checkApiCompatibility") {
    group = LifecycleBasePlugin.VERIFICATION_GROUP
    description = "Checks all published JEI API jars for compatibility with the latest published API jars in the same major version."
	dependsOn(apiProjectPaths.map { "$it:checkJarCompatibility" })
	reportFiles.from(apiCompatibilityReports.values)
	apiSourceFiles.from(apiProjectPaths.map { apiProjectPath ->
		project(apiProjectPath).fileTree("src/main/java") {
			include("**/*.java")
		}
	})
}

tasks.named(LifecycleBasePlugin.CHECK_TASK_NAME) {
    dependsOn(checkApiCompatibility)
}
