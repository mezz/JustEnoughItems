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
            includeGroupAndSubgroups("de.siphalor")
        }
    }
}
// gradle.properties
val curseProjectId: String by extra
val fabricApiVersion: String by extra
val fabricLoaderVersion: String by extra
val minecraftVersionRangeStart: String by extra
val minecraftVersion: String by extra
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
val dependencyProjects = vanillaDependencyProjects + loomDependencyProjects

val embeddedLibraries: Configuration by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}
configurations.implementation {
    extendsFrom(embeddedLibraries)
}
val keyMappingGametestModId = "${modId}-key-mapping-test"

dependencyProjects.forEach {
    project.evaluationDependsOn(it.path)
}

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
    @Suppress("UnstableApiUsage")
    mappings(loom.layered {
        officialMojangMappings()
        parchment("org.parchmentmc.data:parchment-${parchmentMinecraftVersion}:${parchmentVersionFabric}@zip")
    })
    modImplementation("net.fabricmc:fabric-loader:${fabricLoaderVersion}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${fabricApiVersion}")
    vanillaDependencyProjects.forEach {
        implementation(it)
    }
    loomDependencyProjects.forEach {
        implementation(project(it.path, "namedElements"))
    }
    embeddedLibraries("net.mezzdev:suffixtree:${suffixtreeVersion}") {
        isTransitive = false
    }
    modImplementation("de.siphalor.amecs.amecs-key-modifiers:amecs-key-modifiers-${amecsMinecraftVersion}:$amecsVersionFabric")
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
            for (dependencyProject in dependencyProjects) {
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
        named("clientGameTest") {
            systemProperties.put("fabric.client.gametest.disableNetworkSynchronizer", "true")
        }
        create("clientGameTestWithoutAmecs") {
            inherit(named("clientGameTest").get())
            displayName.set("Fabric Client GameTest Without AMECS")
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
        runtimeClasspath += keyMappingGametestSourceSet.output
    }
}

tasks.named("runClientGameTest") {
    dependsOn(keyMappingGametestSourceSet.classesTaskName)
}

tasks.named("runClientGameTestWithoutAmecs") {
    dependsOn(keyMappingGametestSourceSet.classesTaskName)
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
    changelog.set(provider { file("../Changelog/changelog.md").readText() })
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
