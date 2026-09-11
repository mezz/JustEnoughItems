import net.fabricmc.loom.task.RemapJarTask
import net.fabricmc.loom.task.RemapSourcesJarTask
import org.gradle.api.publish.maven.internal.publication.MavenPublicationInternal
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
val mezzConfigCurseForgeProjectSlug: String by extra
val mezzConfigModrinthProjectId: String by extra
val mezzConfigGuiCurseForgeProjectSlug: String by extra
val mezzConfigGuiModrinthProjectId: String by extra
val amecsVersionFabric: String by extra
val amecsKeyModifiersVersionFabric: String by extra
val amecsMinecraftVersion: String by extra
val bakedSubstringIndexVersion: String by extra
val suffixtreeVersion: String by extra
val deduplicatingRunnerVersion: String by extra
val mezzConfigApiDependency: String by rootProject.extra
val mezzConfigFabricDependency: String by rootProject.extra
val mezzConfigGuiApiDependency: String by rootProject.extra
val mezzConfigGuiFabricDependency: String by rootProject.extra

// set by ORG_GRADLE_PROJECT_modrinthToken in Jenkinsfile
val modrinthToken: String? by project
// set by ORG_GRADLE_PROJECT_curseforgeApikey in Jenkinsfile
val curseforgeApikey: String? by project

val baseArchivesName = "${modId}-${minecraftVersion}-fabric"
val apiArchivesName = "${modId}-${minecraftVersion}-fabric-api"
base {
    archivesName.set(baseArchivesName)
}

val apiSourceSet = sourceSets.create("api") {
    resources.setSrcDirs(emptyList<String>())
    compileClasspath += configurations.compileClasspath.get()
}

val vanillaDependencyProjects: List<Project> = listOf(
    project(":Common"),

    project(":Library"),
    project(":Gui"),
)
val dependencyProjects: List<Project> = vanillaDependencyProjects
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

fun clientTestGameDirectory(runName: String) =
    layout.projectDirectory.dir("run/$runName")

dependencyProjects.forEach {
    project.evaluationDependsOn(it.path)
}
project.evaluationDependsOn(debugProject.path)

val commonApiSourceSet = project(":Common").sourceSets.named("api").get()
apiSourceSet.compileClasspath += commonApiSourceSet.output

sourceSets.configureEach {
    if (name != "api") {
        compileClasspath += apiSourceSet.output + commonApiSourceSet.output
        runtimeClasspath += apiSourceSet.output + commonApiSourceSet.output
    }
}

val debugSourceSet = debugProject.sourceSets.main.get()

val embeddedLibraries: Configuration by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}
configurations.implementation {
    extendsFrom(embeddedLibraries)
}

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
    "clientGameTestCompileOnly"("org.jspecify:jspecify:1.0.1")
    compileOnly(mezzConfigApiDependency)
    modLocalRuntime(mezzConfigFabricDependency)
    include(mezzConfigFabricDependency)
    compileOnly(mezzConfigGuiApiDependency)
    modRuntimeOnly(mezzConfigGuiFabricDependency)
    vanillaDependencyProjects.forEach {
        compileOnly(it)
        localRuntime(it)
    }
    modShadeImplementation("net.mezzdev:baked-substring-index:${bakedSubstringIndexVersion}") {
        isTransitive = false
    }
    modShadeImplementation("net.mezzdev:suffixtree:${suffixtreeVersion}") {
        isTransitive = false
    }
    embeddedLibraries("net.mezzdev:deduplicating-runner:${deduplicatingRunnerVersion}") {
        isTransitive = false
    }
    changelogHtml(project(":Changelog"))
    changelogMarkdown(project(":Changelog"))
}

loom {
    mods {
        create("jei") {
            sourceSet(sourceSets.main.get())
            sourceSet(apiSourceSet)
            sourceSet(commonApiSourceSet)
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
        val classPathGroups = listOf(dependencyJarPaths, classPaths, resourcesPaths, apiSourceSet.output.files, commonApiSourceSet.output.files).flatten()
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
        create("clientGameTest") {
            client()
            source(clientGameTestSourceSet)
            configName = "Fabric Client Game Tests"
            ideConfigGenerated(false)
            runDir(loomRunDir.resolve("clientGameTest").toString())
            property("jei.fabric.clientTest", "all")
            vmArgs(
                "-Dfabric.log.level=info"
            )
            programArgs("--username", "JeiClientTest", "--width", "1280", "--height", "720")
        }
        create("clientGameTestWithoutAmecs") {
            client()
            source(clientGameTestWithoutAmecsSourceSet)
            configName = "Fabric Client Game Tests Without AMECS"
            ideConfigGenerated(false)
            runDir(loomRunDir.resolve("clientGameTestWithoutAmecs").toString())
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

fun registerWriteClientTestOptionsTask(name: String, runName: String) =
    tasks.register<Copy>(name) {
        from(layout.projectDirectory.file("src/clientGameTest/templates/options.txt"))
        into(clientTestGameDirectory(runName))
    }

val writeClientGameTestOptions = registerWriteClientTestOptionsTask(
    "writeClientGameTestOptions",
    "clientGameTest"
)
val writeClientGameTestWithoutAmecsOptions = registerWriteClientTestOptionsTask(
    "writeClientGameTestWithoutAmecsOptions",
    "clientGameTestWithoutAmecs"
)

val cleanClientGameTestResults = tasks.register<Delete>("cleanClientGameTestResults") {
    delete(
        layout.buildDirectory.dir("test-results/fabric-client-recipe-sync"),
        layout.buildDirectory.dir("test-results/fabric-client-creative-inventory"),
        layout.buildDirectory.dir("test-results/fabric-client-fluid-ingredients"),
        layout.buildDirectory.dir("test-results/fabric-client-key-mapping"),
        layout.buildDirectory.dir("test-results/fabric-client-gametest")
    )
}
val cleanClientGameTestWithoutAmecsResults = tasks.register<Delete>("cleanClientGameTestWithoutAmecsResults") {
    delete(
        layout.buildDirectory.dir("test-results/fabric-client-creative-inventory-without-amecs"),
        layout.buildDirectory.dir("test-results/fabric-client-key-mapping-without-amecs"),
        layout.buildDirectory.dir("test-results/fabric-client-gametest-without-amecs")
    )
}

tasks.named("runClientGameTest") {
    dependsOn(cleanClientGameTestResults, writeClientGameTestOptions)
}

tasks.named("runClientGameTestWithoutAmecs") {
    dependsOn(cleanClientGameTestWithoutAmecsResults, writeClientGameTestWithoutAmecsOptions)
    mustRunAfter("runClientGameTest")
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
    from(apiSourceSet.output)
    from(commonApiSourceSet.output)
    dependsOn(embeddedLibraries)
    from(sourceSets.main.get().output)
    for (p in dependencyProjects) {
        from(p.sourceSets.main.get().output)
    }
    from(embeddedLibraries.map(::zipTree))
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.named<Jar>("sourcesJar") {
    from(apiSourceSet.allJava)
    from(commonApiSourceSet.allJava)
    from(sourceSets.main.get().allJava)
    for (p in dependencyProjects) {
        from(p.sourceSets.main.get().allJava)
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    archiveClassifier.set("sources")
}

val shadedJar = modShade.shadeJar()
val shadedSourcesJar = modShade.shadeSourcesJar()

configurations.named("modShadeRuntimeElements") {
    // These project dependencies are unpacked into the Fabric jar above.
    // Do not also publish them as external Maven dependencies.
    setExtendsFrom(emptyList())
}

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
        requires(mezzConfigCurseForgeProjectSlug)
        optional(mezzConfigGuiCurseForgeProjectSlug)
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
        requires(mezzConfigModrinthProjectId)
        optional(mezzConfigGuiModrinthProjectId)
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

val apiJarTask = tasks.register<Jar>("apiJar") {
    archiveBaseName.set(apiArchivesName)
    from(apiSourceSet.output)
    manifest.attributes["Implementation-Title"] = "jar"
}

val apiSourcesJarTask = tasks.register<Jar>("apiSourcesJar") {
    archiveBaseName.set(apiArchivesName)
    archiveClassifier.set("sources")
    from(apiSourceSet.allSource)
    manifest.attributes["Implementation-Title"] = "sourcesJar"
}

apiJarTask.configure {
    archiveClassifier.set("dev")
    manifest.attributes["Fabric-Loom-Remap"] = true
    manifest.attributes["Fabric-Loom-Mapping-Namespace"] = "named"
}
apiSourcesJarTask.configure {
    archiveClassifier.set("dev-sources")
    manifest.attributes["Fabric-Loom-Remap"] = true
}
val remapApiJar = tasks.register<RemapJarTask>("remapApiJar") {
    addNestedDependencies.set(false)
    inputFile.set(apiJarTask.flatMap { it.archiveFile })
    archiveBaseName.set(apiArchivesName)
    archiveClassifier.set("")
}
val remapApiSourcesJar = tasks.register<RemapSourcesJarTask>("remapApiSourcesJar") {
    inputFile.set(apiSourcesJarTask.flatMap { it.archiveFile })
    archiveBaseName.set(apiArchivesName)
    archiveClassifier.set("sources")
}
val commonApiIntermediaryBaseArchivesName = "${modId}-${minecraftVersion}-common-api-intermediary"
val commonApiIntermediaryJar = tasks.register<RemapJarTask>("commonApiIntermediaryJar") {
    addNestedDependencies.set(false)
    inputFile.set(project(":Common").tasks.named<Jar>("apiJar").flatMap { it.archiveFile })
    archiveBaseName.set(commonApiIntermediaryBaseArchivesName)
    archiveClassifier.set("")
}
val commonApiIntermediarySourcesJar = tasks.register<RemapSourcesJarTask>("commonApiIntermediarySourcesJar") {
    inputFile.set(project(":Common").tasks.named<Jar>("apiSourcesJar").flatMap { it.archiveFile })
    archiveBaseName.set(commonApiIntermediaryBaseArchivesName)
    archiveClassifier.set("sources")
}
tasks.assemble {
    dependsOn(remapApiJar, remapApiSourcesJar, commonApiIntermediaryJar, commonApiIntermediarySourcesJar)
}

publishing {
    publications {
        register<MavenPublication>("fabricApiJar") {
            // Project dependencies continue to use the main publication's coordinates.
            (this as MavenPublicationInternal).isAlias = true
            artifactId = apiArchivesName
            artifact(remapApiJar)
            artifact(remapApiSourcesJar)
            @Suppress("UnstableApiUsage")
            loom.disableDeprecatedPomGeneration(this)
            val apiDependencyInfo = mapOf(
                "groupId" to project.group,
                "artifactId" to "${modId}-${minecraftVersion}-common-api-intermediary",
                "version" to project.version
            )
            pom.withXml {
                val dependency = asNode().appendNode("dependencies").appendNode("dependency")
                apiDependencyInfo.forEach { (key, value) ->
                    dependency.appendNode(key, value)
                }
            }
        }
        register<MavenPublication>("commonApiIntermediary") {
            (this as MavenPublicationInternal).isAlias = true
            artifactId = commonApiIntermediaryBaseArchivesName
            artifact(commonApiIntermediaryJar)
            artifact(commonApiIntermediarySourcesJar)
        }

        register<MavenPublication>("fabricJar") {
            @Suppress("UnstableApiUsage")
            loom.disableDeprecatedPomGeneration(this)
            artifactId = baseArchivesName
            artifact(shadedJar)
            artifact(shadedSourcesJar)

            val dependencyInfos = listOf(
                dependencyInfo(mezzConfigGuiFabricDependency) + ("optional" to "true")
            ) + dependencyProjects.map {
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

fun dependencyInfo(notation: String): Map<String, String> {
    val (groupId, artifactId, version) = notation.split(":")
    return mapOf(
        "groupId" to groupId,
        "artifactId" to artifactId,
        "version" to version
    )
}

idea {
    module {
        for (fileName in listOf("build", "run", "out", "logs")) {
            excludeDirs.add(file(fileName))
        }
    }
}
