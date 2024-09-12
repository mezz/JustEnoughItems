import net.darkhax.curseforgegradle.TaskPublishCurseForge
import net.darkhax.curseforgegradle.Constants as CFG_Constants

repositories {
    maven("https://maven.parchmentmc.org")
    maven("https://maven.siphalor.de/") {
        // for optional AMECS integration
        content {
            includeGroupByRegex("de\\.siphalor.*")
        }
    }
}

plugins {
    java
    idea
    `maven-publish`
    id("fabric-loom")
    id("net.darkhax.curseforgegradle")
    id("com.modrinth.minotaur")
}

// gradle.properties
val curseHomepageUrl: String by extra
val curseProjectId: String by extra
val fabricApiVersion: String by extra
val fabricLoaderVersion: String by extra
val minecraftVersion: String by extra
val modGroup: String by extra
val modId: String by extra
val modJavaVersion: String by extra
val parchmentVersionFabric: String by extra
val parchmentMinecraftVersion: String by extra
val amecsVersionFabric: String by extra
val amecsMinecraftVersion: String by extra

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
    modCompileOnly(
        group = "de.siphalor",
        name = "amecsapi-${amecsMinecraftVersion}",
        version = amecsVersionFabric
    ) {
        exclude(group = "de.siphalor", module = "nmuk-${amecsMinecraftVersion}")
    }
    dependencyProjects.forEach {
        implementation(it)
    }
}

loom {
    mods {
        create(clientTestModId) {
            sourceSet(clientGameTestSourceSet)
        }
    }
    runs {
        val dependencyJarPaths = dependencyProjects.map {
            it.dependencyProject.tasks.jar.get().archiveFile.get().asFile
        }
        val classPaths = sourceSets.main.get().output.classesDirs
        val resourcesPaths = listOf(
            sourceSets.main.get().output.resourcesDir
        )
        val classPathGroups = listOf(dependencyJarPaths, classPaths, resourcesPaths).flatten().filterNotNull()
        val classPathGroupsString = classPathGroups.joinToString(separator = File.pathSeparator) {
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
            inherit(named("client").get())
            source(clientGameTestSourceSet)
            configName = "Fabric Client Key Mapping Test"
            ideConfigGenerated(false)
            runDir(loomRunDir.resolve("clientKeyMappingTest").toString())
            property("jei.fabric.clientTest", "keyMapping")
            vmArgs("-Dfabric.log.level=info")
            vmArgs("-Dfabric.dli.main=net.fabricmc.loader.impl.launch.knot.KnotClient")
            vmArgs("-Dfabric.classPathGroups=${classPathGroupsString}")
            programArgs("--username", "JeiClientTest")
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

tasks.register<Copy>("writeClientKeyMappingTestOptions") {
    from(layout.projectDirectory.file("src/clientGameTest/templates/options.txt"))
    into(clientTestGameDirectory("clientKeyMappingTest"))
}

tasks.named<JavaExec>("runClientKeyMappingTest") {
    dependsOn("writeClientKeyMappingTestOptions")
    if (System.getProperty("os.name").contains("Mac")) {
        jvmArgs("-XstartOnFirstThread")
    }
    jvmArgs("-Dfabric.dli.main=net.fabricmc.loader.impl.launch.knot.KnotClient")
    jvmArgs("-Dfabric.dli.env=client")
    jvmArgs("-Dfabric.dli.config=${project.projectDir.resolve(".gradle/loom-cache/launch.cfg").absolutePath}")
    jvmArgs("-Dfabric.log.level=info")
    jvmArgs("-Djei.fabric.clientTest=keyMapping")
}

tasks.register("runClientGameTest") {
    group = "mod development"
    description = "Runs JEI Fabric client tests."
    dependsOn("runClientKeyMappingTest")
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

    apiToken = project.findProperty("curseforge_apikey") ?: "0"

    val mainFile = upload(curseProjectId, tasks.remapJar.get().archiveFile)
    mainFile.changelogType = CFG_Constants.CHANGELOG_HTML
    mainFile.changelog = file("../Changelog/changelog.html")
    mainFile.releaseType = CFG_Constants.RELEASE_TYPE_BETA
    mainFile.addJavaVersion("Java $modJavaVersion")
    mainFile.addGameVersion(minecraftVersion)
    mainFile.addModLoader("Fabric")

    doLast {
        project.ext.set("curse_file_url", "${curseHomepageUrl}/files/${mainFile.curseFileId}")
    }
}

modrinth {
    token.set(modrinthToken)
    projectId.set("jei")
    versionType.set("beta")
    uploadFile.set(tasks.remapJar.get())
    changelog.set(provider { file("../Changelog/changelog.md").readText() })
}
tasks.modrinth.get().dependsOn(tasks.remapJar)
tasks.modrinth.get().dependsOn(":Changelog:makeMarkdownChangelog")

tasks.named<Test>("test") {
    useJUnitPlatform()
    include("mezz/jei/test/**")
    exclude("mezz/jei/test/lib/**")
    outputs.upToDateWhen { false }
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

            pom.withXml {
                val dependenciesNode = asNode().appendNode("dependencies")
                dependencyProjects.forEach {
                    val dependencyNode = dependenciesNode.appendNode("dependency")
                    dependencyNode.appendNode("groupId", it.group)
                    dependencyNode.appendNode("artifactId", it.dependencyProject.base.archivesName.get())
                    dependencyNode.appendNode("version", it.version)
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
