import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    java
    idea
    `maven-publish`
    id("net.fabricmc.fabric-loom")
    id("net.mezzdev.modshade")
    id("me.modmuss50.mod-publish-plugin")
}

repositories {
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
val modId: String by extra
val modJavaVersion: String by extra
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

val vanillaDependencyProjects: List<Project> = listOf(
    project(":Common"),
    project(":CommonApi"),
    project(":Library"),
    project(":Gui"),
)
val loomDependencyProjects: List<Project> = listOf(
    project(":FabricApi"),
)
val dependencyProjects = vanillaDependencyProjects + loomDependencyProjects
val debugProject = project(":Debug")

val keyMappingGametestModId = "${modId}-key-mapping-test"
val commonClientTestFixturesSource = project(":Common").layout.projectDirectory.dir("src/clientTestFixtures/java")
val clientGameTestRunDirectory = layout.buildDirectory.dir("run/clientGameTest")
val clientGameTestWithoutAmecsRunDirectory = layout.buildDirectory.dir("run/clientGameTestWithoutAmecs")

dependencyProjects.forEach {
    project.evaluationDependsOn(it.path)
}
project.evaluationDependsOn(debugProject.path)
val debugSourceSet = debugProject.sourceSets.main.get()

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
    minecraft("com.mojang:minecraft:${minecraftVersion}")
    implementation("net.fabricmc:fabric-loader:${fabricLoaderVersion}")
    implementation("net.fabricmc.fabric-api:fabric-api:${fabricApiVersion}")
    dependencyProjects.forEach {
        implementation(it)
    }
    modShadeImplementation("net.mezzdev:baked-substring-index:${bakedSubstringIndexVersion}") {
        isTransitive = false
    }
    modShadeImplementation("net.mezzdev:suffixtree:${suffixtreeVersion}") {
        isTransitive = false
    }
    implementation("de.siphalor.amecs.amecs-key-modifiers:amecs-key-modifiers-${amecsMinecraftVersion}:$amecsVersionFabric")
    changelogHtml(project(":Changelog"))
    changelogMarkdown(project(":Changelog"))
}

fabricApi {
    configureTests {
        createSourceSet = true
        modId = "${modId}-test"
        enableGameTests = true
        enableClientGameTests = true
        eula = true
    }
}

val keyMappingGametestSourceSet = sourceSets.create("keyMappingGametest") {
    val gametestSourceSet = sourceSets.named("gametest").get()
    compileClasspath += sourceSets.main.get().output + gametestSourceSet.compileClasspath
    runtimeClasspath += output + compileClasspath + gametestSourceSet.runtimeClasspath.minus(gametestSourceSet.output)
}

dependencies {
    "gametestImplementation"(testFixtures(project(":Common")))
}

loom {
    mods {
        create("jei") {
            sourceSet(sourceSets.main.get())
            for (dependencyProject in loomDependencyProjects) {
                sourceSet(dependencyProject.sourceSets.main.get())
            }
        }
        create(keyMappingGametestModId) {
            sourceSet(keyMappingGametestSourceSet)
        }
    }

    runs {
        // loom 1.11 runDir takes a directory relative to the root directory
        val loomRunDir = File("run")

        named("client") {
            client()
            displayName.set("Fabric Client")
            generateRunConfig.set(true)
            runDirectory.set(loomRunDir.resolve("client"))
            jvmArguments.addAll(
                "-Dfabric.log.level=info"
            )
        }
        named("server") {
            server()
            displayName.set("Fabric Server")
            generateRunConfig.set(true)
            runDirectory.set(loomRunDir.resolve("server"))
            jvmArguments.addAll(
                "-Dfabric.log.level=info"
            )
        }
        create("client debug") {
            client()
            displayName.set("Fabric Client Debug")
            generateRunConfig.set(true)
            runDirectory.set(loomRunDir.resolve("client"))
            jvmArguments.addAll(
                "-Dfabric.log.level=debug"
            )
        }
        create("server debug") {
            server()
            displayName.set("Fabric Server Debug")
            generateRunConfig.set(true)
            runDirectory.set(loomRunDir.resolve("server"))
            jvmArguments.addAll(
                "-Dfabric.log.level=debug"
            )
        }
        named("gameTest") {
            val gameTestJunitReportFile = layout.buildDirectory.file("test-results/gameTest/TEST-fabric-game-tests.xml")
            systemProperties.put("fabric-api.gametest.report-file", gameTestJunitReportFile.get().asFile.absolutePath)
        }
        create("clientGameTestWithoutAmecs") {
            inherit(named("clientGameTest").get())
            displayName.set("Fabric Client GameTest Without AMECS")
            runDirectory.set(clientGameTestWithoutAmecsRunDirectory.get().asFile)
            systemProperties.put("jei.fabric.disableAmecsSupport", "true")
            systemProperties.put("fabric.client.gametest.modid", keyMappingGametestModId)
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
    named("gametest") {
        java.srcDir(commonClientTestFixturesSource)
        runtimeClasspath += keyMappingGametestSourceSet.output
    }
}

tasks.named("runClientGameTest") {
    dependsOn(keyMappingGametestSourceSet.classesTaskName)
}

tasks.named("runClientGameTestWithoutAmecs") {
    dependsOn(keyMappingGametestSourceSet.classesTaskName)
}

fun registerWriteClientGameTestOptionsTask(name: String, runDirectory: Provider<Directory>) =
    tasks.register<Copy>(name) {
        from(layout.projectDirectory.file("src/gametest/templates/options.txt"))
        into(runDirectory)
        mustRunAfter("deleteGameTestRunDir")
    }

val writeClientGameTestOptions = registerWriteClientGameTestOptionsTask(
    "writeClientGameTestOptions",
    clientGameTestRunDirectory
)
val writeClientGameTestWithoutAmecsOptions = registerWriteClientGameTestOptionsTask(
    "writeClientGameTestWithoutAmecsOptions",
    clientGameTestWithoutAmecsRunDirectory
)

tasks.named("runClientGameTest") {
    dependsOn(writeClientGameTestOptions)
}

tasks.named("runClientGameTestWithoutAmecs") {
    dependsOn(writeClientGameTestWithoutAmecsOptions)
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
        changelog.set(changelogMarkdown.singleFileContents())
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
    dependsOn(tasks.named("sourcesJar"))
}

publishing {
    publications {
        register<MavenPublication>("fabricJar") {
            artifactId = baseArchivesName
            artifact(shadedJar)
            artifact(shadedSourcesJar)
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
