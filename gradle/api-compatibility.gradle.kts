import groovy.json.JsonSlurper
import java.io.File

abstract class JarCompatibilityCheckerArgumentProvider : CommandLineArgumentProvider {
    @get:Classpath
    abstract val baselineJar: ConfigurableFileCollection

    @get:Classpath
    abstract val baseLibraries: ConfigurableFileCollection

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val inputJar: RegularFileProperty

    @get:Classpath
    abstract val concreteLibraries: ConfigurableFileCollection

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    override fun asArguments(): Iterable<String> {
        val arguments = mutableListOf(
            "--api",
            "--base-jar",
            baselineJar.singleFile.absolutePath,
            "--input-jar",
            inputJar.get().asFile.absolutePath,
            "--output",
            outputFile.get().asFile.absolutePath,
        )
        baseLibraries.files.forEach {
            arguments.add("--base-lib")
            arguments.add(it.absolutePath)
        }
        concreteLibraries.files.forEach {
            arguments.add("--concrete-lib")
            arguments.add(it.absolutePath)
        }
        return arguments
    }
}

abstract class ApiCompatibilityReportValidator : DefaultTask() {
    private val nonExtendableAnnotation = Regex("""@\s*(?:ApiStatus\.)?NonExtendable\b""")

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val reportFile: RegularFileProperty

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val sourceFiles: ConfigurableFileCollection

    @TaskAction
    fun validateReport() {
        val report = reportFile.get().asFile
        if (!report.isFile || report.length() == 0L) {
            return
        }

        val nonExtendableInterfaceCache = mutableMapOf<String, Boolean>()
        val allowedNewMethods = mutableListOf<String>()
        val disallowedIncompatibilities = mutableListOf<String>()
        val reportData = JsonSlurper().parse(report) as Map<*, *>

        reportData.forEach { (classNameValue, classReportValue) ->
            val className = classNameValue.toString()
            val classReport = classReportValue as Map<*, *>

            collectErrors(className, null, classReport["classIncompatibilities"], disallowedIncompatibilities)
            collectErrors(className, null, classReport["fieldIncompatibilities"], disallowedIncompatibilities)

            val methodIncompatibilities = classReport["methodIncompatibilities"] as? Map<*, *> ?: emptyMap<Any, Any>()
            methodIncompatibilities.forEach { (methodSignatureValue, incompatibilitiesValue) ->
                val methodSignature = methodSignatureValue.toString()
                val incompatibilities = incompatibilitiesValue as? Iterable<*> ?: emptyList<Any>()
                incompatibilities.forEach { incompatibility ->
                    val incompatibilityReport = incompatibility as Map<*, *>
                    val message = incompatibilityReport["message"].toString()
                    val isError = incompatibilityReport["isError"] as? Boolean ?: true
                    if (isError) {
                        val isNonExtendableInterface = nonExtendableInterfaceCache.getOrPut(className) {
                            isNonExtendableInterface(className)
                        }
                        if (message == METHOD_MADE_ABSTRACT && isNonExtendableInterface) {
                            allowedNewMethods.add("$className#$methodSignature")
                        } else {
                            disallowedIncompatibilities.add("$className#$methodSignature - $message")
                        }
                    }
                }
            }
        }

        if (allowedNewMethods.isNotEmpty()) {
            logger.lifecycle("Allowed ${allowedNewMethods.size} abstract API method changes on non-extendable interfaces in ${report.name}.")
        }

        if (disallowedIncompatibilities.isNotEmpty()) {
            throw GradleException(
                "API compatibility check failed for ${report.name}:\n" +
                    disallowedIncompatibilities.joinToString(separator = "\n") { "- $it" }
            )
        }
    }

    private fun collectErrors(
        className: String,
        memberName: String?,
        incompatibilitiesValue: Any?,
        disallowedIncompatibilities: MutableList<String>
    ) {
        when (incompatibilitiesValue) {
            is Map<*, *> -> {
                incompatibilitiesValue.forEach { (nestedMemberName, nestedIncompatibilities) ->
                    collectErrors(className, nestedMemberName.toString(), nestedIncompatibilities, disallowedIncompatibilities)
                }
            }
            is Iterable<*> -> {
                incompatibilitiesValue.forEach { incompatibility ->
                    val incompatibilityReport = incompatibility as Map<*, *>
                    val isError = incompatibilityReport["isError"] as? Boolean ?: true
                    if (isError) {
                        val message = incompatibilityReport["message"].toString()
                        val memberPrefix = memberName?.let { "#$it" } ?: ""
                        disallowedIncompatibilities.add("$className$memberPrefix - $message")
                    }
                }
            }
        }
    }

    private fun isNonExtendableInterface(className: String): Boolean {
        val sourceFile = findSourceFile(className) ?: return false
        val source = sourceFile.readText()
        return nonExtendableAnnotation.containsMatchIn(source) &&
            Regex("\\binterface\\s+${Regex.escape(sourceFile.nameWithoutExtension)}\\b").containsMatchIn(source)
    }

    private fun findSourceFile(className: String): File? {
        val pathSuffix = className.replace('/', File.separatorChar) + ".java"
        return sourceFiles.files.firstOrNull { it.path.endsWith(pathSuffix) }
    }

    companion object {
        private const val METHOD_MADE_ABSTRACT = "Method was made abstract"
    }
}

data class ApiCompatibilityDependency(
    val projectPath: String,
    val artifactSuffix: String,
    val archiveTaskName: String = "jar",
)

val commonApiCompatibilityDependency = ApiCompatibilityDependency(":CommonApi", "common-api")

fun Configuration.configureApiCompatibilityBaseline(descriptionText: String) {
    description = descriptionText
    isCanBeConsumed = false
    isCanBeResolved = true
    resolutionStrategy.cacheDynamicVersionsFor(0, java.util.concurrent.TimeUnit.SECONDS)
}

data class ApiCompatibilityModule(
    val projectPath: String,
    val taskName: String,
    val artifactSuffix: String,
    val archiveTaskName: String = "jar",
    val apiDependencies: List<ApiCompatibilityDependency> = emptyList(),
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
    ApiCompatibilityModule(":FabricApi", "checkFabricApiCompatibility", "fabric-api", "remapJar", listOf(commonApiCompatibilityDependency)),
    ApiCompatibilityModule(":ForgeApi", "checkForgeApiCompatibility", "forge-api", apiDependencies = listOf(commonApiCompatibilityDependency)),
)

val apiCompatibilityCheckTasks = apiCompatibilityModules.map { module ->
    val apiProject = evaluationDependsOn(module.projectPath)
    val artifactId = "$modId-$minecraftVersion-${module.artifactSuffix}"
    val baselineConfiguration = configurations.create("${module.taskName}Baseline") {
        configureApiCompatibilityBaseline("Published baseline artifact for ${apiProject.path} API compatibility checks.")
    }
    dependencies.add(baselineConfiguration.name, "$modGroup:$artifactId:${apiCompatibilityBaselineVersion.get()}") {
        isTransitive = false
    }

    val apiDependencyPairs = module.apiDependencies.map { dependency ->
        val dependencyProject = evaluationDependsOn(dependency.projectPath)
        val dependencyArtifactId = "$modId-$minecraftVersion-${dependency.artifactSuffix}"
        val dependencyConfigurationName = "${module.taskName}${dependency.projectPath.removePrefix(":").replace(":", "")}BaseLib"
        val dependencyConfiguration = configurations.create(dependencyConfigurationName) {
            configureApiCompatibilityBaseline("Published dependency artifact for ${apiProject.path} API compatibility checks.")
        }
        dependencies.add(dependencyConfiguration.name, "$modGroup:$dependencyArtifactId:${apiCompatibilityBaselineVersion.get()}") {
            isTransitive = false
        }
        val dependencyArchiveTask = dependencyProject.tasks.named<AbstractArchiveTask>(dependency.archiveTaskName)
        dependencyConfiguration to dependencyArchiveTask
    }

    val archiveTask = apiProject.tasks.named<AbstractArchiveTask>(module.archiveTaskName)
    val inputJar = archiveTask.flatMap { it.archiveFile }
    val outputFile = layout.buildDirectory.file("reports/apiCompatibility/${module.artifactSuffix}.json")
    val reportTaskName = "generate${module.taskName.replaceFirstChar { it.uppercase() }}Report"

    val reportTask = tasks.register<JavaExec>(reportTaskName) {
        group = LifecycleBasePlugin.VERIFICATION_GROUP
        description = "Generates an API compatibility report for ${apiProject.path} against the latest published $artifactId API jar in the same major version."

        dependsOn(archiveTask)
        dependsOn(apiDependencyPairs.map { it.second })
        classpath = apiCompatibilityChecker
        mainClass.set("net.neoforged.jarcompatibilitychecker.ConsoleTool")

        inputs.property("apiCompatibilityBaselineVersion", apiCompatibilityBaselineVersion)
        outputs.upToDateWhen { false }

        val checkerArguments = objects.newInstance(JarCompatibilityCheckerArgumentProvider::class.java)
        checkerArguments.baselineJar.from(baselineConfiguration)
        checkerArguments.baseLibraries.from(apiDependencyPairs.map { it.first })
        checkerArguments.inputJar.set(inputJar)
        checkerArguments.concreteLibraries.from(apiDependencyPairs.map { it.second.flatMap { task -> task.archiveFile } })
        checkerArguments.outputFile.set(outputFile)
        argumentProviders.add(checkerArguments)
    }

    tasks.register<ApiCompatibilityReportValidator>(module.taskName) {
        group = LifecycleBasePlugin.VERIFICATION_GROUP
        description = "Checks ${apiProject.path} against the latest published $artifactId API jar in the same major version."

        dependsOn(reportTask)
        reportFile.set(outputFile)
        sourceFiles.from(apiProject.fileTree("src/main/java") {
            include("**/*.java")
        })
    }
}

tasks.register("checkApiCompatibility") {
    group = LifecycleBasePlugin.VERIFICATION_GROUP
    description = "Checks all published JEI API jars for compatibility with the latest published API jars in the same major version."
    dependsOn(apiCompatibilityCheckTasks)
}
