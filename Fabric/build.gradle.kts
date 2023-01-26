import me.modmuss50.mpp.PublishModTask

repositories {
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
val bakedSubstringIndexVersion: String by extra
val parchmentVersionFabric: String by extra
val parchmentMinecraftVersion: String by extra
val amecsVersionFabric: String by extra
val amecsMinecraftVersion: String by extra
val modrinthId: String by extra

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
    implementation(
        group = "com.google.code.findbugs",
        name = "jsr305",
        version = "3.0.1"
    )
    dependencyProjects.forEach {
        implementation(it)
    }
    modShadeImplementation("net.mezzdev:baked-substring-index:${bakedSubstringIndexVersion}") {
        isTransitive = false
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
        minecraftVersions.add(minecraftVersion)
        javaVersions.add(JavaVersion.toVersion(modJavaVersion))
        clientRequired.set(true)
        serverRequired.set(true)
    }

    modrinth {
        projectId = modrinthId
        accessToken = modrinthToken
        minecraftVersions.add(minecraftVersion)
    }
}
tasks.withType<PublishModTask> {
    dependsOn(shadedJar, ":Changelog:makeChangelog", ":Changelog:makeMarkdownChangelog")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
    include("mezz/jei/test/**")
    exclude("mezz/jei/test/lib/**")
    outputs.upToDateWhen { false }
}

artifacts {
    archives(shadedJar)
    archives(shadedSourcesJar)
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
        for (fileName in listOf("run", "out", "logs")) {
            excludeDirs.add(file(fileName))
        }
    }
}
