import mezz.jei.gradle.gradleProperty
import mezz.jei.gradle.ValidateApiCompatibilityReports
import net.neoforged.jarcompatibilitychecker.core.NonExtendableApiCheckMode
import net.neoforged.jarcompatibilitychecker.gradle.CompatibilityTask
import org.gradle.language.base.plugins.LifecycleBasePlugin

plugins {
    id("net.mezzdev.java-formatting") version("0.4.0")

    // https://plugins.gradle.org/plugin/com.dorongold.task-tree
    id("com.dorongold.task-tree") version("4.0.2")

    // https://maven.fabricmc.net/fabric-loom/fabric-loom.gradle.plugin/maven-metadata.xml
    id("fabric-loom") version("1.18.2") apply(false)

    // https://projects.neoforged.net/neoforged/moddevgradle
    id("net.neoforged.moddev") version("2.0.147") apply(false)

    id("net.mezzdev.modshade") version("0.7.0") apply(false)

    // https://plugins.gradle.org/plugin/me.modmuss50.mod-publish-plugin
    id("me.modmuss50.mod-publish-plugin") version("2.2.0") apply(false)

    // https://plugins.gradle.org/plugin/net.minecraftforge.gradle
    id("net.minecraftforge.accesstransformers") version("5.0.3") apply(false)
    id("net.minecraftforge.gradle") version("7.0.40") apply(false)
    id("net.minecraftforge.jarjar") version("0.2.3") apply(false)

    id("net.neoforged.jarcompatibilitychecker") version("0.1.22") apply(false)
}

javaFormatting {
    all()
}

apply {
	from("buildtools/ColoredOutput.gradle")
}

repositories {
    mavenCentral()
}

allprojects {
    repositories {
        maven("https://maven.blamejared.com") {
            content {
                includeGroup("net.mezzdev.config")
            }
        }
    }
}
// gradle.properties
val curseHomepageUrl = gradleProperty("curseHomepageUrl")
val curseProjectId = gradleProperty("curseProjectId")
val configModId = gradleProperty("configModId")
val configGuiModId = gradleProperty("configGuiModId")
val configModGroup = gradleProperty("configModGroup")
val amecsKeyModifiersVersionFabric = gradleProperty("amecsKeyModifiersVersionFabric")
val amecsVersionFabric = gradleProperty("amecsVersionFabric")
val fabricApiVersion = gradleProperty("fabricApiVersion")
val fabricApiVersionRange = gradleProperty("fabricApiVersionRange")
val fabricLoaderVersion = gradleProperty("fabricLoaderVersion")
val fabricLoaderVersionRange = gradleProperty("fabricLoaderVersionRange")
val forgeVersion = gradleProperty("forgeVersion")
val forgeVersionRange = gradleProperty("forgeVersionRange")
val githubUrl = gradleProperty("githubUrl")
val forgeLoaderVersionRange = gradleProperty("forgeLoaderVersionRange")
val neoforgeVersionRange = gradleProperty("neoforgeVersionRange")
val neoforgeLoaderVersionRange = gradleProperty("neoforgeLoaderVersionRange")
val minecraftVersion = gradleProperty("minecraftVersion")
val minecraftVersionRange = gradleProperty("minecraftVersionRange")
val modAuthor = gradleProperty("modAuthor")
val modDescription = gradleProperty("modDescription")
val modGroup = gradleProperty("modGroup")
val modId = gradleProperty("modId")
val modJavaVersion = gradleProperty("modJavaVersion")
val modName = gradleProperty("modName")
val mezzConfigVersion = gradleProperty("mezzConfigVersion")
val mezzConfigVersionRange = gradleProperty("mezzConfigVersionRange")
val mezzConfigFabricVersionRange = gradleProperty("mezzConfigFabricVersionRange")
val mezzConfigGuiVersion = gradleProperty("mezzConfigGuiVersion")
val mezzConfigGuiMinimumVersion = gradleProperty("mezzConfigGuiMinimumVersion")
val specificationVersion = gradleProperty("specificationVersion")

val mezzConfigApiDependency = "$configModGroup:${configModId}-${minecraftVersion}-config-api:$mezzConfigVersion"
extra["mezzConfigApiDependency"] = mezzConfigApiDependency
extra["mezzConfigFabricDependency"] = "$configModGroup:${configModId}-${minecraftVersion}-fabric:$mezzConfigVersion"
extra["mezzConfigForgeDependency"] = "$configModGroup:${configModId}-${minecraftVersion}-forge:$mezzConfigVersion"
extra["mezzConfigNeoForgeDependency"] = "$configModGroup:${configModId}-${minecraftVersion}-neoforge:$mezzConfigVersion"
extra["mezzConfigGuiApiDependency"] = "$configModGroup:${configGuiModId}-${minecraftVersion}-config-gui-api:$mezzConfigGuiVersion"
extra["mezzConfigGuiFabricDependency"] = "$configModGroup:${configGuiModId}-${minecraftVersion}-fabric:$mezzConfigGuiVersion"
extra["mezzConfigGuiForgeDependency"] = "$configModGroup:${configGuiModId}-${minecraftVersion}-forge:$mezzConfigGuiVersion"
extra["mezzConfigGuiNeoForgeDependency"] = "$configModGroup:${configGuiModId}-${minecraftVersion}-neoforge:$mezzConfigGuiVersion"

subprojects {
    //adds the build number to the end of the version string if on a build server
    var buildNumber = project.findProperty("BUILD_NUMBER")
    if (buildNumber == null) {
        buildNumber = "9999"
    }

    version = "${specificationVersion}.${buildNumber}"
    group = modGroup

    tasks.withType<Javadoc> {
        // workaround cast for https://github.com/gradle/gradle/issues/7038
        val standardJavadocDocletOptions = options as StandardJavadocDocletOptions
        // prevent java 8's strict doclint for javadocs from failing builds
        standardJavadocDocletOptions.addStringOption("Xdoclint:none", "-quiet")
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release.set(JavaLanguageVersion.of(modJavaVersion).asInt())
    }

    tasks.withType<Jar> {
        manifest {
            attributes(mapOf(
                "Specification-Title" to modName,
                "Specification-Vendor" to modAuthor,
                "Specification-Version" to specificationVersion,
                "Implementation-Title" to name,
                "Implementation-Version" to archiveVersion,
                "Implementation-Vendor" to modAuthor
            ))
        }
    }

    tasks.withType<ProcessResources> {
        exclude("**/.DS_Store")

        val properties = mapOf(
            "amecsKeyModifiersVersionFabric" to amecsKeyModifiersVersionFabric,
            "amecsVersionFabric" to amecsVersionFabric,
            "curseHomepageUrl" to curseHomepageUrl,
            "configGuiModId" to configGuiModId,
            "configModId" to configModId,
            "fabricApiVersion" to fabricApiVersion,
            "fabricApiVersionRange" to fabricApiVersionRange,
            "fabricLoaderVersion" to fabricLoaderVersion,
            "fabricLoaderVersionRange" to fabricLoaderVersionRange,
            "forgeVersionRange" to forgeVersionRange,
            "githubUrl" to githubUrl,
            "forgeLoaderVersionRange" to forgeLoaderVersionRange,
            "neoforgeVersionRange" to neoforgeVersionRange,
            "neoforgeLoaderVersionRange" to neoforgeLoaderVersionRange,
            "minecraftVersion" to minecraftVersion,
            "minecraftVersionRange" to minecraftVersionRange,
            "modAuthor" to modAuthor,
            "modDescription" to modDescription,
            "modId" to modId,
            "modJavaVersion" to modJavaVersion,
            "modName" to modName,
            "mezzConfigGuiVersionRange" to "[$mezzConfigGuiMinimumVersion,)",
            "mezzConfigGuiFabricVersionRange" to ">=$mezzConfigGuiMinimumVersion",
            "mezzConfigGuiFabricBreaksVersionRange" to "<$mezzConfigGuiMinimumVersion",
            "mezzConfigVersionRange" to mezzConfigVersionRange,
            "mezzConfigFabricVersionRange" to mezzConfigFabricVersionRange,
            "version" to version,
        )
        inputs.properties(properties)
        filesMatching(listOf("META-INF/mods.toml", "META-INF/neoforge.mods.toml", "pack.mcmeta", "fabric.mod.json")) {
            expand(properties)
        }
    }

    // Activate reproducible builds
    // https://docs.gradle.org/current/userguide/working_with_files.html#sec:reproducible_archives
    tasks.withType<AbstractArchiveTask>().configureEach {
        isPreserveFileTimestamps = false
        isReproducibleFileOrder = true
    }
}

subprojects {
    tasks.withType<JavaCompile> {
        options.isDeprecation = true
        options.compilerArgs.add("-Xlint:unchecked")
    }
}

val apiProjectPaths = listOf(":Common", ":Fabric", ":NeoForge", ":Forge")
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
            val apiJarTaskName = if (apiProjectPath == ":Fabric") "remapApiJar" else "apiJar"
            inputJar.set(apiProject.tasks.named<AbstractArchiveTask>(apiJarTaskName).flatMap { it.archiveFile })
            artifact.set("${modGroup}:${modId}-${minecraftVersion}-${apiProject.name.lowercase()}-api")
            mavens.set(listOf("https://maven.blamejared.com"))
            // Match the previous CLI check and avoid loading the full Minecraft compile classpath.
            libraries.setFrom(emptyList<Any>())
            nonExtendableApiCheckMode.set(NonExtendableApiCheckMode.SKIP)
            // Keep fail disabled so the target branch validator can filter its known non-extendable API exceptions.
        }
    }
}

val checkApiCompatibility = tasks.register<ValidateApiCompatibilityReports>("checkApiCompatibility") {
    group = LifecycleBasePlugin.VERIFICATION_GROUP
    description = "Checks all published JEI API jars for compatibility with the latest published API jars in the same major version."
    dependsOn(apiProjectPaths.map { "$it:checkJarCompatibility" })
    reportFiles.from(apiCompatibilityReports.values)
    apiSourceFiles.from(apiProjectPaths.map { apiProjectPath ->
        project(apiProjectPath).fileTree("src/api/java") {
            include("**/*.java")
        }
    })
}

tasks.named(LifecycleBasePlugin.CHECK_TASK_NAME) {
    dependsOn(checkApiCompatibility)
}
