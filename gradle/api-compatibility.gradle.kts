abstract class JarCompatibilityCheckerArgumentProvider : CommandLineArgumentProvider {
    @get:Classpath
    abstract val baselineJar: ConfigurableFileCollection

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val inputJar: RegularFileProperty

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    override fun asArguments(): Iterable<String> = listOf(
        "--api",
        "--fail",
        "--base-jar",
        baselineJar.singleFile.absolutePath,
        "--input-jar",
        inputJar.get().asFile.absolutePath,
        "--output",
        outputFile.get().asFile.absolutePath,
    )
}

data class ApiCompatibilityModule(
    val projectPath: String,
    val taskName: String,
    val artifactSuffix: String,
)

repositories {
    maven("https://maven.neoforged.net/releases") {
        content {
            includeGroup("net.neoforged")
        }
    }
    maven("https://maven.blamejared.com") {
        content {
            includeGroup("mezz.jei")
        }
    }
}

// gradle.properties
val minecraftVersion: String by extra
val modGroup: String by extra
val modId: String by extra
val specificationVersion: String by extra

val apiCompatibilityCheckerVersion = "0.1.15"
val apiCompatibilityAsmVersion = "9.10.1"
val apiCompatibilityMajorVersion = specificationVersion.substringBefore('.').toInt()
val apiCompatibilityBaselineVersion = providers.gradleProperty("apiCompatibilityBaselineVersion")
    .orElse("[$apiCompatibilityMajorVersion.0.0,${apiCompatibilityMajorVersion + 1}.0.0)")

val apiCompatibilityChecker: Configuration by configurations.creating {
    description = "Runtime classpath for JEI's API compatibility checker."
    isCanBeConsumed = false
    isCanBeResolved = true
    attributes {
        attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_RUNTIME))
        attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.EXTERNAL))
    }
    resolutionStrategy.eachDependency {
        if (requested.group == "org.ow2.asm") {
            useVersion(apiCompatibilityAsmVersion)
            because("ASM 9.7 cannot read Java 25 class files.")
        }
    }
}

dependencies {
    apiCompatibilityChecker("net.neoforged:jarcompatibilitychecker:$apiCompatibilityCheckerVersion")
    apiCompatibilityChecker("org.ow2.asm:asm:$apiCompatibilityAsmVersion")
    apiCompatibilityChecker("org.ow2.asm:asm-analysis:$apiCompatibilityAsmVersion")
    apiCompatibilityChecker("org.ow2.asm:asm-commons:$apiCompatibilityAsmVersion")
    apiCompatibilityChecker("org.ow2.asm:asm-tree:$apiCompatibilityAsmVersion")
}

val apiCompatibilityModules = listOf(
    ApiCompatibilityModule(":CommonApi", "checkCommonApiCompatibility", "common-api"),
    ApiCompatibilityModule(":FabricApi", "checkFabricApiCompatibility", "fabric-api"),
    ApiCompatibilityModule(":NeoForgeApi", "checkNeoForgeApiCompatibility", "neoforge-api"),
)

val apiCompatibilityCheckTasks = apiCompatibilityModules.map { module ->
    val apiProject = evaluationDependsOn(module.projectPath)
    val artifactId = "$modId-$minecraftVersion-${module.artifactSuffix}"
    val baselineConfiguration = configurations.create("${module.taskName}Baseline") {
        description = "Published baseline artifact for ${apiProject.path} API compatibility checks."
        isCanBeConsumed = false
        isCanBeResolved = true
        resolutionStrategy.cacheDynamicVersionsFor(0, java.util.concurrent.TimeUnit.SECONDS)
    }
    dependencies.add(baselineConfiguration.name, "$modGroup:$artifactId:${apiCompatibilityBaselineVersion.get()}") {
        isTransitive = false
    }

    val jarTask = apiProject.tasks.named<Jar>("jar")
    val inputJar = jarTask.flatMap { it.archiveFile }
    val outputFile = layout.buildDirectory.file("reports/apiCompatibility/${module.artifactSuffix}.json")

    tasks.register<JavaExec>(module.taskName) {
        group = LifecycleBasePlugin.VERIFICATION_GROUP
        description = "Checks ${apiProject.path} against the latest published $artifactId API jar in the same major version."

        dependsOn(jarTask)
        classpath = apiCompatibilityChecker
        mainClass.set("net.neoforged.jarcompatibilitychecker.ConsoleTool")

        inputs.property("apiCompatibilityBaselineVersion", apiCompatibilityBaselineVersion)
        outputs.upToDateWhen { false }

        val checkerArguments = objects.newInstance(JarCompatibilityCheckerArgumentProvider::class.java)
        checkerArguments.baselineJar.from(baselineConfiguration)
        checkerArguments.inputJar.set(inputJar)
        checkerArguments.outputFile.set(outputFile)
        argumentProviders.add(checkerArguments)
    }
}

tasks.register("checkApiCompatibility") {
    group = LifecycleBasePlugin.VERIFICATION_GROUP
    description = "Checks all published JEI API jars for compatibility with the latest published API jars in the same major version."
    dependsOn(apiCompatibilityCheckTasks)
}
