import me.modmuss50.mpp.PublishModTask
import net.darkhax.curseforgegradle.TaskPublishCurseForge
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import net.darkhax.curseforgegradle.Constants as CFG_Constants

plugins {
    java
    idea
    `maven-publish`
    id("fabric-loom")
    id("net.darkhax.curseforgegradle")
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

// set by ORG_GRADLE_PROJECT_modrinthToken in Jenkinsfile
val modrinthToken: String? by project

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

dependencyProjects.forEach {
    project.evaluationDependsOn(it.dependencyProject.path)
}
project.evaluationDependsOn(":Changelog")

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
    dependencyProjects.forEach {
        implementation(it)
    }
}

loom {
    runs {
        val dependencyJarPaths = dependencyProjects.map {
            it.dependencyProject.tasks.jar.get().archiveFile.get().asFile
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
        val loomRunDir = project.projectDir
            .relativeTo(project.rootDir)
            .resolve("run")

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

tasks.register<TaskPublishCurseForge>("publishCurseForge") {
    dependsOn(tasks.remapJar)
    dependsOn(":Changelog:makeChangelog")

    disableVersionDetection()

    apiToken = project.findProperty("curseforge_apikey") ?: "0"

    val mainFile = upload(curseProjectId, tasks.remapJar.get().archiveFile)
    mainFile.changelogType = CFG_Constants.CHANGELOG_HTML
    mainFile.changelog = file("../Changelog/changelog.html")
    mainFile.releaseType = CFG_Constants.RELEASE_TYPE_BETA
    mainFile.addJavaVersion("Java $modJavaVersion")
    mainFile.addGameVersion(minecraftVersion)
    mainFile.addGameVersion(minecraftVersionRangeStart)
    mainFile.addModLoader("Fabric")
}

publishMods {
    file.set(tasks.remapJar.get().archiveFile)
    changelog.set(provider { file("../Changelog/changelog.md").readText() })
    type = BETA
    modLoaders.add("fabric")

    modrinth {
        projectId = "jei"
        accessToken = modrinthToken
        minecraftVersionRange {
            start = minecraftVersionRangeStart
            end = minecraftVersion
        }
    }
}
tasks.withType<PublishModTask> {
    dependsOn(tasks.jar, ":Changelog:makeMarkdownChangelog")
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
            artifact(tasks.remapJar)
            artifact(tasks.remapSourcesJar)

            val dependencyInfos = dependencyProjects.map {
                mapOf(
                    "groupId" to it.group,
                    "artifactId" to it.dependencyProject.base.archivesName.get(),
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
        for (fileName in listOf("run", "out", "logs")) {
            excludeDirs.add(file(fileName))
        }
    }
}
