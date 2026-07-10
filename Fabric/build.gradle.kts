import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    java
    idea
    `maven-publish`
    id("fabric-loom")
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
            includeGroup("de.siphalor")
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
val amecsMinecraftVersion: String by extra
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

val embeddedLibraries: Configuration by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}
configurations.implementation {
    extendsFrom(embeddedLibraries)
}
val commonClientTestFixturesSource = project(":Common").layout.projectDirectory.dir("src/clientTestFixtures/java")
val clientGameTestSourceSet = sourceSets.create("clientGameTest") {
    java.srcDir(commonClientTestFixturesSource)
    compileClasspath += sourceSets.main.get().output + sourceSets.main.get().compileClasspath
    runtimeClasspath += output + sourceSets.main.get().runtimeClasspath
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

val commonTestFixturesSourceSet = project(":Common").sourceSets.named("testFixtures").get()
val commonTestFixturesClasses = commonTestFixturesSourceSet.output.classesDirs
clientGameTestSourceSet.compileClasspath += commonTestFixturesClasses
clientGameTestSourceSet.runtimeClasspath += commonTestFixturesClasses

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
    implementation(
        group = "com.google.code.findbugs",
        name = "jsr305",
        version = "3.0.1"
    )
    modImplementation(
        group = "de.siphalor",
        name = "amecsapi-${amecsMinecraftVersion}",
        version = amecsVersionFabric
    )
    "clientGameTestCompileOnly"("org.jspecify:jspecify:1.0.0")
    vanillaDependencyProjects.forEach {
        implementation(it)
    }
    loomDependencyProjects.forEach {
        implementation(project(it.path, "namedElements"))
    }
    embeddedLibraries("net.mezzdev:suffixtree:${suffixtreeVersion}") {
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
            programArgs("--username", "JeiClientTest")
        }
        create("clientKeyMappingTestWithoutAmecs") {
            inherit(named("clientKeyMappingTest").get())
            configName = "Fabric Client Key Mapping Test Without AMECS"
            runDir(loomRunDir.resolve("clientKeyMappingTestWithoutAmecs").toString())
            property("jei.fabric.disableAmecsSupport", "true")
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
        listOf("clientKeyMappingTest", "clientKeyMappingTestWithoutAmecs")
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

val clientRecipeSyncTestRunTasks = clientRecipeSyncTestCases.map { (runName, _) ->
    tasks.named("run${capitalizedRunName(runName)}")
}
clientRecipeSyncTestRunTasks.zipWithNext().forEach { (previousTask, nextTask) ->
    nextTask.configure {
        mustRunAfter(previousTask)
    }
}

tasks.named("runClientKeyMappingTest") {
    mustRunAfter(clientRecipeSyncTestRunTasks)
}

tasks.named("runClientKeyMappingTestWithoutAmecs") {
    mustRunAfter("runClientKeyMappingTest")
}

tasks.register("runClientRecipeSyncTest") {
    group = "mod development"
    description = "Runs all JEI Fabric client recipe-sync test scenarios."
    dependsOn(clientRecipeSyncTestRunTasks)
}

tasks.register("runClientGameTest") {
    group = "mod development"
    description = "Runs JEI Fabric client tests with AMECS support enabled."
    dependsOn(clientRecipeSyncTestRunTasks, "runClientKeyMappingTest")
}

tasks.register("runClientGameTestWithoutAmecs") {
    group = "mod development"
    description = "Runs JEI Fabric client tests with AMECS support disabled."
    dependsOn("runClientKeyMappingTestWithoutAmecs")
}

tasks.jar {
    dependsOn(embeddedLibraries)
    from(sourceSets.main.get().output)
    for (p in dependencyProjects) {
        from(p.sourceSets.main.get().output)
    }
    from(embeddedLibraries.map(::zipTree))
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

publishMods {
    file.set(tasks.remapJar.get().archiveFile)
    changelog.set(changelogMarkdown.singleFileContents())
    type = BETA
    modLoaders.add("fabric")
    displayName.set("${project.version} for Fabric $minecraftVersion")
    version.set(project.version.toString())

    curseforge {
        projectId = curseProjectId
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
            artifact(tasks.remapJar)
            artifact(tasks.remapSourcesJar)

            val dependencyInfos = dependencyProjects.map {
                mapOf(
                    "groupId" to it.group,
                    "artifactId" to it.base.archivesName.get(),
                    "version" to it.version
                )
            }

            pom.withXml {
                val dependenciesNode = asNode().appendNode("dependencies")
                dependencyInfos.forEach {
                    val dependencyNode = dependenciesNode.appendNode("dependency")
                    it.forEach { (key, value) ->
                        dependencyNode.appendNode(key, value)
                    }
                }
            }
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
