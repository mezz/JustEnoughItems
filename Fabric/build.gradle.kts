import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    java
    idea
    `maven-publish`
    id("fabric-loom")
    id("net.mezzdev.modshade")
    id("me.modmuss50.mod-publish-plugin")
}

repositories {
    fun exclusiveMaven(url: String, filter: Action<InclusiveRepositoryContentDescriptor>) =
        exclusiveContent {
            forRepository { maven(url) }
            filter(filter)
        }
    exclusiveMaven("https://maven.parchmentmc.org") {
        includeGroupByRegex("org\\.parchmentmc.*")
    }
    maven("https://maven.siphalor.de/") {
        // for optional AMECS integration
        content {
            includeGroupAndSubgroups("de.siphalor")
        }
    }
}

// gradle.properties
val curseHomepageUrl: String by extra
val curseProjectId: String by extra
val fabricApiVersion: String by extra
val fabricLoaderVersion: String by extra
val minecraftVersionRangeStart: String by extra
val minecraftVersion: String by extra
val modGroup: String by extra
val modId: String by extra
val modJavaVersion: String by extra
val parchmentMinecraftVersion: String by extra
val parchmentVersionFabric: String by extra
val modrinthId: String by extra
val amecsVersionFabric: String by extra
val amecsKeyModifiersVersionFabric: String by extra
val amecsMinecraftVersion: String by extra
val bakedSubstringIndexVersion: String by extra
val suffixtreeVersion: String by extra

// set by ORG_GRADLE_PROJECT_modrinthToken in Jenkinsfile
val modrinthToken: String? by project
// set by ORG_GRADLE_PROJECT_curseforgeApikey in Jenkinsfile
val curseforgeApikey: String? by project

val baseArchivesName = "${modId}-${minecraftVersion}-fabric"
base {
    archivesName.set(baseArchivesName)
}
val vanillaDependencyProjects: List<Project> = listOf(
    project(":Common"),
    project(":CommonApi"),
    project(":Library"),
    project(":Gui"),
)
val loomDependencyProjects: List<Project> = listOf(
    project(":FabricApi"),
)
val dependencyProjects: List<Project> = vanillaDependencyProjects + loomDependencyProjects
val debugProject = project(":Debug")

val commonClientTestFixturesSource = project(":Common").layout.projectDirectory.dir("src/clientTestFixtures/java")
val clientGameTestSourceSet = sourceSets.create("clientGameTest") {
    java.srcDir(commonClientTestFixturesSource)
    compileClasspath += sourceSets.main.get().output + sourceSets.main.get().compileClasspath
    runtimeClasspath += output + sourceSets.main.get().runtimeClasspath
}
val clientGameTestWithoutAmecsSourceSet = sourceSets.create("clientGameTestWithoutAmecs") {
    runtimeClasspath += clientGameTestSourceSet.runtimeClasspath.filter {
        !it.name.startsWith("amecs-")
    }
}
configurations.named(clientGameTestSourceSet.runtimeOnlyConfigurationName) {
    extendsFrom(configurations.runtimeOnly.get())
}
val clientTestModId = "${modId}-client-tests"
val clientRecipeSyncTestCases = listOf(
    "clientRecipeSyncSingleplayer" to "singleplayer",
    "clientRecipeSyncFabricServerWithJei" to "fabricServerWithJei",
    "clientRecipeSyncFabricServerWithoutJei" to "fabricServerWithoutJei",
    "clientRecipeSyncVanillaServerWithoutJei" to "vanillaServerWithoutJei",
)

fun clientTestGameDirectory(runName: String) =
    layout.projectDirectory.dir("run/$runName")

fun capitalizedRunName(runName: String): String =
    runName.replaceFirstChar { it.uppercase() }

dependencyProjects.forEach {
    project.evaluationDependsOn(it.path)
}
project.evaluationDependsOn(debugProject.path)
val debugSourceSet = debugProject.sourceSets.main.get()

val commonTestFixturesSourceSet = project(":Common").sourceSets.named("testFixtures").get()
val commonTestFixturesClasses = commonTestFixturesSourceSet.output.classesDirs
clientGameTestSourceSet.compileClasspath += commonTestFixturesClasses
clientGameTestSourceSet.runtimeClasspath += commonTestFixturesClasses
clientGameTestWithoutAmecsSourceSet.runtimeClasspath += commonTestFixturesClasses

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(modJavaVersion))
    }
    withSourcesJar()
}

val changelogHtml: Configuration by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
    attributes {
        attribute(Usage.USAGE_ATTRIBUTE, objects.named<Usage>("changelogHtml"))
    }
}

val changelogMarkdown: Configuration by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
    attributes {
        attribute(Usage.USAGE_ATTRIBUTE, objects.named<Usage>("changelogMarkdown"))
    }
}

fun Configuration.singleFileContents(): Provider<String> =
    incoming
        .files
        .elements
        .map { elements -> elements.single() }
        .map { it.asFile.readText() }

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    javaToolchains {
        compilerFor {
            languageVersion.set(JavaLanguageVersion.of(modJavaVersion))
        }
    }
}

dependencies {
    minecraft(
        group = "com.mojang",
        name = "minecraft",
        version = minecraftVersion,
    )
    @Suppress("UnstableApiUsage")
    mappings(loom.layered {
        officialMojangMappings()
        parchment("org.parchmentmc.data:parchment-${parchmentMinecraftVersion}:${parchmentVersionFabric}@zip")
    })
    modImplementation(
        group = "net.fabricmc",
        name = "fabric-loader",
        version = fabricLoaderVersion,
    )
    modImplementation(
        group = "net.fabricmc.fabric-api",
        name = "fabric-api",
        version = fabricApiVersion,
    )
    compileOnly(
        group = "com.google.code.findbugs",
        name = "jsr305",
        version = "3.0.1"
    )
    modCompileOnly(
        group = "de.siphalor.amecs.amecs-api-legacy",
        name = "amecs-api-legacy-${amecsMinecraftVersion}",
        version = amecsVersionFabric
    )
    modLocalRuntime(
        group = "de.siphalor.amecs.amecs-api-legacy",
        name = "amecs-api-legacy-${amecsMinecraftVersion}",
        version = amecsVersionFabric
    )
    modCompileOnly(
        group = "de.siphalor.amecs.amecs-key-modifiers",
        name = "amecs-key-modifiers-${amecsMinecraftVersion}",
        version = amecsKeyModifiersVersionFabric
    )
    modLocalRuntime(
        group = "de.siphalor.amecs.amecs-key-modifiers",
        name = "amecs-key-modifiers-${amecsMinecraftVersion}",
        version = amecsKeyModifiersVersionFabric
    )
    "clientGameTestCompileOnly"("org.jspecify:jspecify:1.0.0")
    vanillaDependencyProjects.forEach {
        compileOnly(it)
        localRuntime(it)
    }
    loomDependencyProjects.forEach {
        val namedElements = project(it.path, "namedElements")
        compileOnly(namedElements)
        localRuntime(namedElements)
    }
    modShadeImplementation("net.mezzdev:baked-substring-index:${bakedSubstringIndexVersion}") {
        isTransitive = false
    }
    modShadeImplementation("net.mezzdev:suffixtree:${suffixtreeVersion}") {
        isTransitive = false
    }
    changelogHtml(project(":Changelog"))
    changelogMarkdown(project(":Changelog"))
}

loom {
    mods {
        create("jei") {
            sourceSet(sourceSets.main.get())
            for (dependencyProject in dependencyProjects) {
                sourceSet(dependencyProject.sourceSets.main.get())
            }
        }
        create(clientTestModId) {
            sourceSet(clientGameTestSourceSet)
        }
    }
    runs {
        val dependencyJarPaths = dependencyProjects.map {
            it.tasks.jar.get().archiveFile.get().asFile
        }
        val classPaths = sourceSets.main.get().output.classesDirs
        val resourcesPaths = listOfNotNull(
            sourceSets.main.get().output.resourcesDir
        )
        val classPathGroups = listOf(dependencyJarPaths, classPaths, resourcesPaths).flatten()
        val classPathGroupsString = classPathGroups
            .filterNotNull()
            .joinToString(separator = File.pathSeparator) {
                it.absoluteFile.toString()
            }

        // loom 1.11 runDir takes a directory relative to the root directory
        val loomRunDir = File("run")

        named("client") {
            client()
            configName = "Fabric Client"
            ideConfigGenerated(true)
            runDir(loomRunDir.resolve("client").toString())
            vmArgs(
                "-Dfabric.classPathGroups=${classPathGroupsString}",
                "-Dfabric.log.level=info"
            )
        }
        named("server") {
            server()
            configName = "Fabric Server"
            ideConfigGenerated(true)
            runDir(loomRunDir.resolve("server").toString())
            vmArgs(
                "-Dfabric.classPathGroups=${classPathGroupsString}",
                "-Dfabric.log.level=info"
            )
        }
        create("client debug") {
            client()
            configName = "Fabric Client Debug"
            ideConfigGenerated(true)
            runDir(loomRunDir.resolve("client").toString())
            vmArgs(
                "-Dfabric.classPathGroups=${classPathGroupsString}",
                "-Dfabric.log.level=debug"
            )
        }
        create("server debug") {
            server()
            configName = "Fabric Server Debug"
            ideConfigGenerated(true)
            runDir(loomRunDir.resolve("server").toString())
            vmArgs(
                "-Dfabric.classPathGroups=${classPathGroupsString}",
                "-Dfabric.log.level=debug"
            )
        }
        clientRecipeSyncTestCases.forEach { (runName, testCase) ->
            create(runName) {
                client()
                source(clientGameTestSourceSet)
                configName = "Fabric Client Recipe Sync Test ${capitalizedRunName(testCase)}"
                ideConfigGenerated(false)
                runDir(loomRunDir.resolve(runName).toString())
                property("jei.fabric.clientTest", "recipeSync")
                property("jei.clientRecipeSyncTest", testCase)
                vmArgs(
                    "-Dfabric.log.level=info"
                )
                programArgs("--username", "JeiClientTest")
            }
        }
        create("clientCreativeInventoryTest") {
            client()
            source(clientGameTestSourceSet)
            configName = "Fabric Client Creative Inventory Test"
            ideConfigGenerated(false)
            runDir(loomRunDir.resolve("clientCreativeInventoryTest").toString())
            property("jei.fabric.clientTest", "creativeInventory")
            vmArgs(
                "-Dfabric.log.level=info"
            )
            programArgs("--username", "JeiClientTest", "--width", "1280", "--height", "720")
        }
        create("clientFluidIngredientTest") {
            client()
            source(clientGameTestSourceSet)
            configName = "Fabric Client Fluid Ingredient Test"
            ideConfigGenerated(false)
            runDir(loomRunDir.resolve("clientFluidIngredientTest").toString())
            property("jei.fabric.clientTest", "fluidIngredients")
            vmArgs(
                "-Dfabric.log.level=info"
            )
            programArgs("--username", "JeiClientTest", "--width", "1280", "--height", "720")
        }
        create("clientCreativeInventoryTestWithoutAmecs") {
            client()
            source(clientGameTestWithoutAmecsSourceSet)
            configName = "Fabric Client Creative Inventory Test Without AMECS"
            ideConfigGenerated(false)
            runDir(loomRunDir.resolve("clientCreativeInventoryTestWithoutAmecs").toString())
            property("jei.fabric.clientTest", "creativeInventory")
            vmArgs(
                "-Dfabric.log.level=info"
            )
            programArgs("--username", "JeiClientTest", "--width", "1280", "--height", "720")
        }
        create("clientKeyMappingTest") {
            client()
            source(clientGameTestSourceSet)
            configName = "Fabric Client Key Mapping Test"
            ideConfigGenerated(false)
            runDir(loomRunDir.resolve("clientKeyMappingTest").toString())
            property("jei.fabric.clientTest", "keyMapping")
            vmArgs(
                "-Dfabric.log.level=info"
            )
            programArgs("--username", "JeiClientTest", "--width", "1280", "--height", "720")
        }
        create("clientKeyMappingTestWithoutAmecs") {
            client()
            source(clientGameTestWithoutAmecsSourceSet)
            configName = "Fabric Client Key Mapping Test Without AMECS"
            runDir(loomRunDir.resolve("clientKeyMappingTestWithoutAmecs").toString())
            property("jei.fabric.clientTest", "keyMapping")
            vmArgs(
                "-Dfabric.log.level=info"
            )
            programArgs("--username", "JeiClientTest", "--width", "1280", "--height", "720")
        }
    }

    accessWidenerPath.set(file("src/main/resources/jei.accesswidener"))
}

sourceSets {
    named("main") {
        resources {
            for (p in dependencyProjects) {
                srcDir(p.sourceSets.main.get().resources)
            }
        }
    }
}

val writeClientTestOptionsTasks = (
    clientRecipeSyncTestCases.map { it.first } +
        listOf(
            "clientCreativeInventoryTest",
            "clientCreativeInventoryTestWithoutAmecs",
            "clientFluidIngredientTest",
            "clientKeyMappingTest",
            "clientKeyMappingTestWithoutAmecs"
        )
    ).associateWith { runName ->
        tasks.register<Copy>("write${capitalizedRunName(runName)}Options") {
            from(layout.projectDirectory.file("src/clientGameTest/templates/options.txt"))
            into(clientTestGameDirectory(runName))
        }
    }

clientRecipeSyncTestCases.forEach { (runName, _) ->
    tasks.named("run${capitalizedRunName(runName)}") {
        dependsOn(writeClientTestOptionsTasks.getValue(runName))
    }
}

tasks.named("runClientKeyMappingTest") {
    dependsOn(writeClientTestOptionsTasks.getValue("clientKeyMappingTest"))
}

tasks.named("runClientKeyMappingTestWithoutAmecs") {
    dependsOn(writeClientTestOptionsTasks.getValue("clientKeyMappingTestWithoutAmecs"))
}

tasks.named("runClientCreativeInventoryTest") {
    dependsOn(writeClientTestOptionsTasks.getValue("clientCreativeInventoryTest"))
}

tasks.named("runClientCreativeInventoryTestWithoutAmecs") {
    dependsOn(writeClientTestOptionsTasks.getValue("clientCreativeInventoryTestWithoutAmecs"))
}

tasks.named("runClientFluidIngredientTest") {
    dependsOn(writeClientTestOptionsTasks.getValue("clientFluidIngredientTest"))
}

val clientRecipeSyncTestRunTasks = clientRecipeSyncTestCases.map { (runName, _) ->
    tasks.named("run${capitalizedRunName(runName)}")
}
clientRecipeSyncTestRunTasks.zipWithNext().forEach { (previousTask, nextTask) ->
    nextTask.configure {
        mustRunAfter(previousTask)
    }
}

tasks.named("runClientKeyMappingTest") {
    mustRunAfter("runClientCreativeInventoryTest", "runClientFluidIngredientTest")
}

tasks.named("runClientKeyMappingTestWithoutAmecs") {
    mustRunAfter("runClientCreativeInventoryTestWithoutAmecs")
}

tasks.named("runClientCreativeInventoryTest") {
    mustRunAfter(clientRecipeSyncTestRunTasks)
}

tasks.named("runClientCreativeInventoryTestWithoutAmecs") {
    mustRunAfter("runClientCreativeInventoryTest", "runClientKeyMappingTest")
}

tasks.named("runClientFluidIngredientTest") {
    mustRunAfter("runClientCreativeInventoryTest")
}

tasks.register("runClientRecipeSyncTest") {
    group = "mod development"
    description = "Runs all JEI Fabric client recipe-sync test scenarios."
    dependsOn(clientRecipeSyncTestRunTasks)
}

tasks.register("runClientGameTest") {
    group = "mod development"
    description = "Runs JEI Fabric client tests with AMECS support enabled."
    dependsOn(clientRecipeSyncTestRunTasks, "runClientCreativeInventoryTest", "runClientFluidIngredientTest", "runClientKeyMappingTest")
}

tasks.register("runClientGameTestWithoutAmecs") {
    group = "mod development"
    description = "Runs JEI Fabric client tests without AMECS on the runtime classpath."
    dependsOn("runClientCreativeInventoryTestWithoutAmecs", "runClientKeyMappingTestWithoutAmecs")
}

val debugClassesTask = debugProject.tasks.named(debugSourceSet.classesTaskName)
val debugModPath = debugProject.layout.buildDirectory.dir("resources/main").get().asFile.absolutePath
val debugRunTasks = setOf("runClient", "runServer", "runClientDebug", "runServerDebug")
tasks.matching { it.name in debugRunTasks }.configureEach {
    dependsOn(debugClassesTask)
    if (this is org.gradle.api.tasks.JavaExec) {
        classpath(debugSourceSet.output)
        jvmArgs("-Dfabric.addMods=$debugModPath")
    }
}

tasks.jar {
    from(sourceSets.main.get().output)
    for (p in dependencyProjects) {
        from(p.sourceSets.main.get().output)
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.named<Jar>("sourcesJar") {
    from(sourceSets.main.get().allJava)
    for (p in dependencyProjects) {
        from(p.sourceSets.main.get().allJava)
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    archiveClassifier.set("sources")
}

val shadedJar = modShade.shadeJar()
modShade.shadeSourcesJar()

publishMods {
    file.set(shadedJar.flatMap { it.archiveFile })
    changelog.set(changelogMarkdown.singleFileContents())
    type = BETA
    modLoaders.add("fabric")
    displayName.set("${project.version} for Fabric $minecraftVersion")
    version.set(project.version.toString())

    curseforge {
        projectId = curseProjectId
        projectSlug = curseHomepageUrl.substringAfterLast("/")
        accessToken.set(curseforgeApikey ?: "0")
        changelog.set(changelogHtml.singleFileContents())
        changelogType = "html"
        minecraftVersionRange {
            start = minecraftVersionRangeStart
            end = minecraftVersion
        }
        javaVersions.add(JavaVersion.toVersion(modJavaVersion))
        client = true
        server = true
        dryRun = curseforgeApikey == null
    }

    modrinth {
        projectId = modrinthId
        accessToken = modrinthToken
        minecraftVersionRange {
            start = minecraftVersionRangeStart
            end = minecraftVersion
        }
        dryRun = modrinthToken == null
    }
}

tasks.named<Test>("test") {
    useJUnitPlatform()
    include("mezz/jei/test/**")
    exclude("mezz/jei/test/lib/**")
    outputs.upToDateWhen { false }
    testLogging {
        events = setOf(TestLogEvent.FAILED)
        exceptionFormat = TestExceptionFormat.FULL
    }
}

tasks.assemble {
    dependsOn(tasks.remapJar, tasks.remapSourcesJar)
}

publishing {
    publications {
        register<MavenPublication>("fabricJar") {
            @Suppress("UnstableApiUsage")
            loom.disableDeprecatedPomGeneration(this)
            artifactId = baseArchivesName
            from(components["java"])
        }
    }
    repositories {
        val deployDir = project.findProperty("DEPLOY_DIR")
        if (deployDir != null) {
            maven(deployDir)
        }
    }
}

idea {
    module {
        for (fileName in listOf("build", "run", "out", "logs")) {
            excludeDirs.add(file(fileName))
        }
    }
}
