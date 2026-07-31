import groovy.json.JsonSlurper

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

data class ApiCompatibilityError(
    val className: String,
    val memberName: String?,
    val message: String,
) {
    fun format(): String =
        if (memberName == null) {
            "$className: $message"
        } else {
            "$className: $memberName - $message"
        }
}

abstract class ValidateApiCompatibilityReport : DefaultTask() {
    companion object {
        private const val ABSTRACT_METHOD_ERROR = "Method was made abstract"
    }

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val reportFile: RegularFileProperty

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val sourceFiles: ConfigurableFileCollection

    @TaskAction
    fun validateReport() {
        val errors = collectApiCompatibilityErrors(reportFile.get().asFile)
        val nonExtendableInterfaces = findNonExtendableInterfaces(errors)
        val ignoredErrors = errors.filter { isIgnoredNonExtendableInterfaceError(it, nonExtendableInterfaces) }
        val unexpectedErrors = errors.filterNot { isIgnoredNonExtendableInterfaceError(it, nonExtendableInterfaces) }

        if (ignoredErrors.isNotEmpty()) {
            logger.lifecycle("Ignored known API compatibility false positives:\n{}", ignoredErrors.joinToString("\n") { it.format() })
        }

        if (unexpectedErrors.isNotEmpty()) {
            throw GradleException("API compatibility check failed:\n${unexpectedErrors.joinToString("\n") { it.format() }}")
        }
    }

    private fun collectApiCompatibilityErrors(reportFile: File): List<ApiCompatibilityError> {
        val report = JsonSlurper().parse(reportFile) as? Map<*, *> ?: return emptyList()
        val errors = mutableListOf<ApiCompatibilityError>()

        for ((classNameValue, classReportValue) in report) {
            val className = classNameValue as? String ?: continue
            val classReport = classReportValue as? Map<*, *> ?: continue

            fun collectErrors(memberName: String?, incompatibilitiesValue: Any?) {
                val incompatibilities = incompatibilitiesValue as? Iterable<*> ?: return
                for (incompatibilityValue in incompatibilities) {
                    val incompatibility = incompatibilityValue as? Map<*, *> ?: continue
                    if (incompatibility["isError"] == true) {
                        val message = incompatibility["message"] as? String ?: incompatibility.toString()
                        errors.add(ApiCompatibilityError(className, memberName, message))
                    }
                }
            }

            collectErrors(null, classReport["classIncompatibilities"])

            val methodIncompatibilities = classReport["methodIncompatibilities"] as? Map<*, *>
            methodIncompatibilities?.forEach { (methodNameValue, incompatibilitiesValue) ->
                collectErrors(methodNameValue as? String, incompatibilitiesValue)
            }

            val fieldIncompatibilities = classReport["fieldIncompatibilities"] as? Map<*, *>
            fieldIncompatibilities?.forEach { (fieldNameValue, incompatibilitiesValue) ->
                collectErrors(fieldNameValue as? String, incompatibilitiesValue)
            }
        }

        return errors
    }

    // TODO Remove this once JarCompatibilityChecker accounts for @ApiStatus.NonExtendable:
    // https://github.com/neoforged/JarCompatibilityChecker/pull/5
    // Adding abstract methods to non-extendable JEI API interfaces is safe because mods should not implement them.
    private fun isIgnoredNonExtendableInterfaceError(
        error: ApiCompatibilityError,
        nonExtendableInterfaces: Set<String>,
    ): Boolean =
        error.message == ABSTRACT_METHOD_ERROR &&
            error.memberName != null &&
            nonExtendableInterfaces.contains(error.className)

    private fun findNonExtendableInterfaces(errors: List<ApiCompatibilityError>): Set<String> =
        errors.asSequence()
            .map(ApiCompatibilityError::className)
            .distinct()
            .filter(::isNonExtendableInterface)
            .toSet()

    private fun isNonExtendableInterface(className: String): Boolean {
        val sourceFile = findSourceFile(className) ?: return false
        val source = sourceFile.readText()
        return source.contains("@ApiStatus.NonExtendable") &&
            Regex("\\binterface\\s+${Regex.escape(sourceFile.nameWithoutExtension)}\\b").containsMatchIn(source)
    }

    private fun findSourceFile(className: String): File? {
        val pathSuffix = className.replace('/', File.separatorChar) + ".java"
        return sourceFiles.files.firstOrNull { it.path.endsWith(pathSuffix) }
    }
}

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

fun optionalApiCompatibilityModule(
    projectPath: String,
    taskName: String,
    artifactSuffix: String,
): ApiCompatibilityModule? =
    if (findProject(projectPath) == null) {
        null
    } else {
        ApiCompatibilityModule(projectPath, taskName, artifactSuffix)
    }

val apiCompatibilityModules = listOfNotNull(
    optionalApiCompatibilityModule(":CommonApi", "checkCommonApiCompatibility", "common-api"),
    optionalApiCompatibilityModule(":FabricApi", "checkFabricApiCompatibility", "fabric-api"),
    optionalApiCompatibilityModule(":NeoForgeApi", "checkNeoForgeApiCompatibility", "neoforge-api"),
    optionalApiCompatibilityModule(":ForgeApi", "checkForgeApiCompatibility", "forge-api"),
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
    val apiInputJar = jarTask.flatMap { it.archiveFile }
    val outputFile = layout.buildDirectory.file("reports/apiCompatibility/${module.artifactSuffix}.json")

    val checkerTask = tasks.register<JavaExec>("${module.taskName}WithJarCompatibilityChecker") {
        group = LifecycleBasePlugin.VERIFICATION_GROUP
        description = "Runs JarCompatibilityChecker for ${apiProject.path} against the latest published $artifactId API jar in the same major version."

        dependsOn(jarTask)
        classpath = apiCompatibilityChecker
        mainClass.set("net.neoforged.jarcompatibilitychecker.ConsoleTool")

        inputs.property("apiCompatibilityBaselineVersion", apiCompatibilityBaselineVersion)
        outputs.upToDateWhen { false }

        val checkerArguments = objects.newInstance(JarCompatibilityCheckerArgumentProvider::class.java)
        checkerArguments.baselineJar.from(baselineConfiguration)
        checkerArguments.inputJar.set(apiInputJar)
        checkerArguments.outputFile.set(outputFile)
        argumentProviders.add(checkerArguments)
    }

    tasks.register<ValidateApiCompatibilityReport>(module.taskName) {
        group = LifecycleBasePlugin.VERIFICATION_GROUP
        description = "Checks ${apiProject.path} against the latest published $artifactId API jar in the same major version."

        dependsOn(checkerTask)
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

tasks.named(LifecycleBasePlugin.CHECK_TASK_NAME) {
    dependsOn("checkApiCompatibility")
}
