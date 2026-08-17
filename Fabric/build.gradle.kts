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
val jUnitVersion: String by extra
val minecraftVersion: String by extra
val modGroup: String by extra
val modId: String by extra
val modJavaVersion: String by extra
val bakedSubstringIndexVersion: String by extra
val suffixtreeVersion: String by extra
val parchmentVersionFabric: String by extra
val parchmentMinecraftVersion: String by extra
val amecsVersionFabric: String by extra
val amecsKeyModifiersVersionFabric: String by extra
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
val vanillaDependencyProjects: List<Project> = listOf(
    project(":Common"),
    project(":CommonApi"),
    project(":Library"),
    project(":Gui"),
)
val loomDependencyProjects: List<Project> = listOf(project(":FabricApi"))
val dependencyProjects: List<Project> = vanillaDependencyProjects + loomDependencyProjects
val debugProject = project(":Debug")

dependencyProjects.forEach {
    project.evaluationDependsOn(it.path)
}
project.evaluationDependsOn(debugProject.path)
val debugSourceSet = debugProject.sourceSets.main.get()

val clientGameTestSourceSet = sourceSets.create("clientGameTest") {
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

fun clientTestGameDirectory(runName: String) =
    layout.projectDirectory.dir("run/$runName")

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(modJavaVersion))
    }
    withSourcesJar()
}

val changelogHtml = configurations.create("changelogHtml") {
    isCanBeConsumed = false
    isCanBeResolved = true
    isVisible = false
    attributes {
        attribute(Usage.USAGE_ATTRIBUTE, objects.named<Usage>("changelogHtml"))
    }
}

val changelogMarkdown = configurations.create("changelogMarkdown") {
    isCanBeConsumed = false
    isCanBeResolved = true
    isVisible = false
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
    val jsr305 = "com.google.code.findbugs:jsr305:3.0.1"
    compileOnly(jsr305)
    testCompileOnly(jsr305)
    vanillaDependencyProjects.forEach {
        compileOnly(it)
        testImplementation(it)
        localRuntime(it)
    }
    loomDependencyProjects.forEach {
        val namedElements = project(it.path, "namedElements")
        compileOnly(namedElements)
        testImplementation(namedElements)
        localRuntime(namedElements)
    }
    changelogHtml(project(":Changelog"))
    changelogMarkdown(project(":Changelog"))
    modShadeImplementation("net.mezzdev:baked-substring-index:${bakedSubstringIndexVersion}") {
        isTransitive = false
    }
    modShadeImplementation("net.mezzdev:suffixtree:${suffixtreeVersion}") {
        isTransitive = false
    }
    testImplementation(
        group = "org.junit.jupiter",
        name = "junit-jupiter",
        version = jUnitVersion
    )
    testRuntimeOnly(
        group = "org.junit.platform",
        name = "junit-platform-launcher",
        version = jUnitVersion
    )
}

loom {
    mods {
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
        create("clientCreativeInventoryTest") {
            client()
            source(clientGameTestSourceSet)
            configName = "Fabric Client Creative Inventory Test"
            ideConfigGenerated(false)
            runDir(loomRunDir.resolve("clientCreativeInventoryTest").toString())
            property("jei.fabric.clientTest", "creativeInventory")
            vmArgs(
                "-Dfabric.log.level=info",
                "-Dfabric.dli.main=net.fabricmc.loader.impl.launch.knot.KnotClient",
                "-Dfabric.classPathGroups=${classPathGroupsString}"
            )
            programArgs("--username", "JeiClientTest", "--width", "1280", "--height", "720")
        }
        create("clientCreativeInventoryTestWithoutAmecs") {
            client()
            source(clientGameTestWithoutAmecsSourceSet)
            configName = "Fabric Client Creative Inventory Test Without AMECS"
            runDir(loomRunDir.resolve("clientCreativeInventoryTestWithoutAmecs").toString())
            property("jei.fabric.clientTest", "creativeInventory")
            vmArgs(
                "-Dfabric.log.level=info",
                "-Dfabric.dli.main=net.fabricmc.loader.impl.launch.knot.KnotClient",
                "-Dfabric.classPathGroups=${classPathGroupsString}"
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
                "-Dfabric.log.level=info",
                "-Dfabric.dli.main=net.fabricmc.loader.impl.launch.knot.KnotClient",
                "-Dfabric.classPathGroups=${classPathGroupsString}"
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
                "-Dfabric.log.level=info",
                "-Dfabric.dli.main=net.fabricmc.loader.impl.launch.knot.KnotClient",
                "-Dfabric.classPathGroups=${classPathGroupsString}"
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

tasks.register<Copy>("writeClientKeyMappingTestOptions") {
    from(layout.projectDirectory.file("src/clientGameTest/templates/options.txt"))
    into(clientTestGameDirectory("clientKeyMappingTest"))
}

tasks.register<Copy>("writeClientKeyMappingTestWithoutAmecsOptions") {
    from(layout.projectDirectory.file("src/clientGameTest/templates/options.txt"))
    into(clientTestGameDirectory("clientKeyMappingTestWithoutAmecs"))
}

tasks.register<Copy>("writeClientCreativeInventoryTestOptions") {
    from(layout.projectDirectory.file("src/clientGameTest/templates/options.txt"))
    into(clientTestGameDirectory("clientCreativeInventoryTest"))
}

tasks.register<Copy>("writeClientCreativeInventoryTestWithoutAmecsOptions") {
    from(layout.projectDirectory.file("src/clientGameTest/templates/options.txt"))
    into(clientTestGameDirectory("clientCreativeInventoryTestWithoutAmecs"))
}

tasks.named<JavaExec>("runClientCreativeInventoryTest") {
    dependsOn("writeClientCreativeInventoryTestOptions")
    if (System.getProperty("os.name").contains("Mac")) {
        jvmArgs("-XstartOnFirstThread")
    }
    jvmArgs("-Dfabric.dli.main=net.fabricmc.loader.impl.launch.knot.KnotClient")
    jvmArgs("-Dfabric.dli.env=client")
    jvmArgs("-Dfabric.dli.config=${project.projectDir.resolve(".gradle/loom-cache/launch.cfg").absolutePath}")
    jvmArgs("-Dfabric.log.level=info")
    jvmArgs("-Djei.fabric.clientTest=creativeInventory")
}

tasks.named<JavaExec>("runClientCreativeInventoryTestWithoutAmecs") {
    dependsOn("writeClientCreativeInventoryTestWithoutAmecsOptions")
    mustRunAfter("runClientCreativeInventoryTest", "runClientKeyMappingTest")
    if (System.getProperty("os.name").contains("Mac")) {
        jvmArgs("-XstartOnFirstThread")
    }
    jvmArgs("-Dfabric.dli.main=net.fabricmc.loader.impl.launch.knot.KnotClient")
    jvmArgs("-Dfabric.dli.env=client")
    jvmArgs("-Dfabric.dli.config=${project.projectDir.resolve(".gradle/loom-cache/launch.cfg").absolutePath}")
    jvmArgs("-Dfabric.log.level=info")
    jvmArgs("-Djei.fabric.clientTest=creativeInventory")
}

tasks.named<JavaExec>("runClientKeyMappingTest") {
    dependsOn("writeClientKeyMappingTestOptions")
    mustRunAfter("runClientCreativeInventoryTest")
    if (System.getProperty("os.name").contains("Mac")) {
        jvmArgs("-XstartOnFirstThread")
    }
    jvmArgs("-Dfabric.dli.main=net.fabricmc.loader.impl.launch.knot.KnotClient")
    jvmArgs("-Dfabric.dli.env=client")
    jvmArgs("-Dfabric.dli.config=${project.projectDir.resolve(".gradle/loom-cache/launch.cfg").absolutePath}")
    jvmArgs("-Dfabric.log.level=info")
    jvmArgs("-Djei.fabric.clientTest=keyMapping")
}

tasks.named<JavaExec>("runClientKeyMappingTestWithoutAmecs") {
    dependsOn("writeClientKeyMappingTestWithoutAmecsOptions")
    mustRunAfter("runClientCreativeInventoryTestWithoutAmecs")
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
    dependsOn("runClientCreativeInventoryTest", "runClientKeyMappingTest")
}

tasks.register("runClientGameTestWithoutAmecs") {
    group = "mod development"
    description = "Runs JEI Fabric client tests without AMECS on the runtime classpath."
    dependsOn("runClientCreativeInventoryTestWithoutAmecs", "runClientKeyMappingTestWithoutAmecs")
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
            start = minecraftVersion
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
            start = minecraftVersion
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
