import me.modmuss50.mpp.PublishModTask
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
val dependencyProjects: List<ProjectDependency> = listOf(
    project.dependencies.project(":Core"),
    project.dependencies.project(":Common"),
    project.dependencies.project(":CommonApi"),
    project.dependencies.project(":Library"),
    project.dependencies.project(":Gui"),
    project.dependencies.project(":FabricApi", configuration = "namedElements")
)
val debugProject = project(":Debug")

dependencyProjects.forEach {
    project.evaluationDependsOn(it.dependencyProject.path)
}
project.evaluationDependsOn(debugProject.path)
project.evaluationDependsOn(":Changelog")
val debugSourceSet = debugProject.sourceSets.main.get()

val clientGameTestSourceSet = sourceSets.create("clientGameTest") {
    compileClasspath += sourceSets.main.get().output + sourceSets.main.get().compileClasspath
    runtimeClasspath += output + sourceSets.main.get().runtimeClasspath
}
configurations.named(clientGameTestSourceSet.runtimeOnlyConfigurationName) {
    extendsFrom(configurations.runtimeOnly.get())
}
val clientTestModId = "${modId}-client-tests"

fun clientTestGameDirectory(runName: String) =
    layout.projectDirectory.dir("run/$runName")

fun capitalizedRunName(runName: String): String =
    runName.replaceFirstChar { it.uppercase() }

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(modJavaVersion))
    }
    withSourcesJar()
}

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
    dependencyProjects.forEach {
        implementation(it)
    }
    modShadeImplementation("net.mezzdev:baked-substring-index:${bakedSubstringIndexVersion}") {
        isTransitive = false
    }
    modShadeImplementation("net.mezzdev:suffixtree:${suffixtreeVersion}") {
        isTransitive = false
    }
}

loom {
    mods {
        create("jei") {
            sourceSet(sourceSets.main.get())
            for (dependencyProject in dependencyProjects) {
                sourceSet(dependencyProject.dependencyProject.sourceSets.main.get())
            }
        }
        create(clientTestModId) {
            sourceSet(clientGameTestSourceSet)
        }
    }
    runs {
        val dependencyJarPaths = dependencyProjects.map {
            it.dependencyProject.tasks.jar.get().archiveFile.get().asFile
        }
        val classPaths = sourceSets.main.get().output.classesDirs
        val resourcesPaths = listOfNotNull(
            sourceSets.main.get().output.resourcesDir
        )
        val classPathGroups = listOf(dependencyJarPaths, classPaths, resourcesPaths).flatten()
        val classPathGroupsString = classPathGroups.joinToString(separator = File.pathSeparator) {
            it.absoluteFile.toString()
        }

        // Loom 1.8 runDir takes a directory relative to this project directory.
        val loomRunDir = File("run")

        named("client") {
            client()
            configName = "Fabric Client"
            ideConfigGenerated(true)
            runDir(loomRunDir.resolve("client").toString())
            vmArgs("-Dfabric.classPathGroups=${classPathGroupsString}")
        }
        named("server") {
            server()
            configName = "Fabric Server"
            ideConfigGenerated(true)
            runDir(loomRunDir.resolve("server").toString())
            vmArgs("-Dfabric.classPathGroups=${classPathGroupsString}")
        }
        create("clientKeyMappingTest") {
            client()
            source(clientGameTestSourceSet)
            configName = "Fabric Client Key Mapping Test"
            ideConfigGenerated(false)
            runDir(loomRunDir.resolve("clientKeyMappingTest").toString())
            property("jei.fabric.clientTest", "keyMapping")
            vmArgs("-Dfabric.log.level=info")
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
                srcDir(p.dependencyProject.sourceSets.main.get().resources)
            }
        }
    }
}

val writeClientTestOptionsTasks = listOf(
    "clientKeyMappingTest",
    "clientKeyMappingTestWithoutAmecs"
).associateWith { runName ->
    tasks.register<Copy>("write${capitalizedRunName(runName)}Options") {
        from(layout.projectDirectory.file("src/clientGameTest/templates/options.txt"))
        into(clientTestGameDirectory(runName))
    }
}

tasks.named("runClientKeyMappingTest") {
    dependsOn(writeClientTestOptionsTasks.getValue("clientKeyMappingTest"))
}

tasks.named("runClientKeyMappingTestWithoutAmecs") {
    dependsOn(writeClientTestOptionsTasks.getValue("clientKeyMappingTestWithoutAmecs"))
    mustRunAfter("runClientKeyMappingTest")
}

tasks.register("runClientGameTest") {
    group = "mod development"
    description = "Runs JEI Fabric client tests with AMECS support enabled."
    dependsOn("runClientKeyMappingTest")
}

tasks.register("runClientGameTestWithoutAmecs") {
    group = "mod development"
    description = "Runs JEI Fabric client tests with AMECS support disabled."
    dependsOn("runClientKeyMappingTestWithoutAmecs")
}

val debugClassesTask = debugProject.tasks.named(debugSourceSet.classesTaskName)
val debugModPath = debugProject.layout.buildDirectory.dir("resources/main").get().asFile.absolutePath
val debugRunTasks = setOf("runClient", "runServer")
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
        from(p.dependencyProject.sourceSets.main.get().output)
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.named<Jar>("sourcesJar") {
    from(sourceSets.main.get().allJava)
    for (p in dependencyProjects) {
        from(p.dependencyProject.sourceSets.main.get().allJava)
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    archiveClassifier.set("sources")
}

val shadedJar = modShade.shadeJar()
val shadedSourcesJar = modShade.shadeSourcesJar()

publishMods {
    file.set(shadedJar.flatMap { it.archiveFile })
    changelog.set(provider { file("../Changelog/changelog.md").readText() })
    type = BETA
    modLoaders.add("fabric")
    displayName.set("${project.version} for Fabric $minecraftVersion")
    version.set(project.version.toString())

    curseforge {
        projectId = curseProjectId
        projectSlug = curseHomepageUrl.substringAfterLast("/")
        accessToken.set(curseforgeApikey ?: "0")
        changelog.set(provider { file("../Changelog/changelog.html").readText() })
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
tasks.withType<PublishModTask> {
    dependsOn(tasks.jar, ":Changelog:makeChangelog", ":Changelog:makeMarkdownChangelog")
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

artifacts {
    archives(tasks.remapJar)
    archives(tasks.remapSourcesJar)
}

publishing {
    publications {
        register<MavenPublication>("fabricJar") {
            @Suppress("UnstableApiUsage")
            loom.disableDeprecatedPomGeneration(this)
            artifactId = baseArchivesName
            from(components["modShade"])
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
