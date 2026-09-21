import mezz.jei.gradle.dependencyInfo
import mezz.jei.gradle.mezzConfigDependency
import mezz.jei.gradle.mezzConfigGuiDependency
import mezz.jei.gradle.UnpackArchives
import mezz.jei.gradle.gradleProperty
import mezz.jei.gradle.isolatedProjectDirectory
import mezz.jei.gradle.optionalGradleProperty
import net.fabricmc.loom.task.RemapJarTask
import net.fabricmc.loom.task.RemapSourcesJarTask
import net.fabricmc.loom.task.ManifestModificationAction
import net.fabricmc.loom.task.service.JarManifestService
import net.neoforged.jarcompatibilitychecker.core.NonExtendableApiCheckMode
import net.neoforged.jarcompatibilitychecker.gradle.CompatibilityTask
import org.gradle.api.publish.maven.internal.publication.MavenPublicationInternal
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.gradle.language.base.plugins.LifecycleBasePlugin

plugins {
    java
    idea
    `maven-publish`
    id("fabric-loom")
    id("net.neoforged.jarcompatibilitychecker")
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
val mezzConfigApiDependency = mezzConfigDependency("config-api")
val mezzConfigGuiApiDependency = mezzConfigGuiDependency("config-gui-api")
val mezzConfigGuiFabricDependency = mezzConfigGuiDependency("fabric")

// gradle.properties
val curseHomepageUrl = gradleProperty("curseHomepageUrl")
val curseProjectId = gradleProperty("curseProjectId")
val fabricApiVersion = gradleProperty("fabricApiVersion")
val fabricLoaderVersion = gradleProperty("fabricLoaderVersion")
val minecraftVersionRangeStart = gradleProperty("minecraftVersionRangeStart")
val minecraftVersion = gradleProperty("minecraftVersion")
val modId = gradleProperty("modId")
val modGroup = gradleProperty("modGroup")
val modJavaVersion = gradleProperty("modJavaVersion")
val parchmentMinecraftVersion = gradleProperty("parchmentMinecraftVersion")
val parchmentVersionFabric = gradleProperty("parchmentVersionFabric")
val modrinthId = gradleProperty("modrinthId")
val amecsVersionFabric = gradleProperty("amecsVersionFabric")
val amecsMinecraftVersion = gradleProperty("amecsMinecraftVersion")
val mezzConfigCurseForgeProjectSlug = gradleProperty("mezzConfigCurseForgeProjectSlug")
val mezzConfigModrinthProjectId = gradleProperty("mezzConfigModrinthProjectId")
val mezzConfigGuiCurseForgeProjectSlug = gradleProperty("mezzConfigGuiCurseForgeProjectSlug")
val mezzConfigGuiModrinthProjectId = gradleProperty("mezzConfigGuiModrinthProjectId")
val bakedSubstringIndexVersion = gradleProperty("bakedSubstringIndexVersion")
val suffixtreeVersion = gradleProperty("suffixtreeVersion")

// set by ORG_GRADLE_PROJECT_modrinthToken in Jenkinsfile
val modrinthToken = optionalGradleProperty("modrinthToken")
// set by ORG_GRADLE_PROJECT_curseforgeApikey in Jenkinsfile
val curseforgeApikey = optionalGradleProperty("curseforgeApikey")

val baseArchivesName = "${modId}-${minecraftVersion}-fabric"
val apiArchivesName = "${modId}-${minecraftVersion}-fabric-api"
base {
    archivesName.set(baseArchivesName)
}

val dependencyProjectPaths = listOf(":Common", ":Library", ":Gui")
val commonProjectDirectory = isolatedProjectDirectory(":Common")
val debugProjectDirectory = isolatedProjectDirectory(":Debug")

val keyMappingGametestModId = "${modId}-key-mapping-test"
val commonClientTestFixturesSource = commonProjectDirectory.dir("src/clientTestFixtures/java")
val clientGameTestRunDirectory = layout.buildDirectory.dir("run/clientGameTest")
val clientGameTestWithoutAmecsRunDirectory = layout.buildDirectory.dir("run/clientGameTestWithoutAmecs")

val apiSourceSet = sourceSets.create("api") {
    resources.setSrcDirs(emptyList<String>())
    output.setResourcesDir(layout.buildDirectory.dir("classes/java/api"))
}
val debugSourceSet = sourceSets.create("debug") {
    java.srcDir(debugProjectDirectory.dir("src/main/java"))
    resources.srcDir(debugProjectDirectory.dir("src/main/resources"))
    compileClasspath += sourceSets.main.get().compileClasspath
    runtimeClasspath += output + compileClasspath
}

loom.createRemapConfigurations(apiSourceSet)

afterEvaluate {
    configurations.named(apiSourceSet.compileClasspathConfigurationName) {
        extendsFrom(configurations.getByName("minecraftNamedCompile"))
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(modJavaVersion))
    }
    withSourcesJar()
}

val changelogHtml = configurations.create("changelogHtml") {
    isCanBeConsumed = false
    isCanBeResolved = true
    attributes {
        attribute(Usage.USAGE_ATTRIBUTE, objects.named<Usage>("changelogHtml"))
    }
}

val changelogMarkdown = configurations.create("changelogMarkdown") {
    isCanBeConsumed = false
    isCanBeResolved = true
    attributes {
        attribute(Usage.USAGE_ATTRIBUTE, objects.named<Usage>("changelogMarkdown"))
    }
}

val dependencyClasses = configurations.create("dependencyClasses") {
    isCanBeConsumed = false
    isCanBeResolved = true
    attributes {
        attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.LIBRARY))
        attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_RUNTIME))
        attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements.CLASSES))
    }
}

val dependencyResources = configurations.create("dependencyResources") {
    isCanBeConsumed = false
    isCanBeResolved = true
    attributes {
        attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.LIBRARY))
        attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_RUNTIME))
        attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements.RESOURCES))
    }
}

val dependencySources = configurations.create("dependencySources") {
    isCanBeConsumed = false
    isCanBeResolved = true
    attributes {
        attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.DOCUMENTATION))
        attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_RUNTIME))
        attribute(DocsType.DOCS_TYPE_ATTRIBUTE, objects.named(DocsType.SOURCES))
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
    compileOnly(mezzConfigApiDependency)
    modRuntimeOnly(mezzConfigDependency("fabric"))
    include(mezzConfigDependency("fabric"))
    compileOnly(mezzConfigGuiApiDependency)
    modLocalRuntime(mezzConfigGuiFabricDependency)
    minecraft("com.mojang:minecraft:${minecraftVersion}")
    @Suppress("UnstableApiUsage")
    mappings(loom.layered {
        officialMojangMappings()
        parchment("org.parchmentmc.data:parchment-${parchmentMinecraftVersion}:${parchmentVersionFabric}@zip")
    })
    modImplementation("net.fabricmc:fabric-loader:${fabricLoaderVersion}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${fabricApiVersion}")
    implementation(apiSourceSet.output)
    implementation(project(path = ":Common", configuration = "apiClassesElements"))
    add("modApiImplementation", "net.fabricmc:fabric-loader:${fabricLoaderVersion}")
    add("modApiImplementation", "net.fabricmc.fabric-api:fabric-api:${fabricApiVersion}")
    add(apiSourceSet.implementationConfigurationName, "org.jetbrains:annotations:26.0.2")
    add(apiSourceSet.implementationConfigurationName, "org.jspecify:jspecify:1.0.0")
    add(apiSourceSet.implementationConfigurationName, project(path = ":Common", configuration = "apiClassesElements"))
    dependencyProjectPaths.forEach {
        compileOnly(project(it))
        localRuntime(project(it))
        dependencyClasses(project(it)) {
            isTransitive = false
        }
        dependencyResources(project(it)) {
            isTransitive = false
        }
        dependencySources(project(it)) {
            isTransitive = false
        }
    }
    dependencyClasses(project(path = ":Common", configuration = "apiClassesElements")) {
        isTransitive = false
    }
    dependencySources(project(path = ":Common", configuration = "apiSourcesElements")) {
        isTransitive = false
    }
    modShadeImplementation("net.mezzdev:baked-substring-index:${bakedSubstringIndexVersion}") {
        isTransitive = false
    }
    modShadeImplementation("net.mezzdev:suffixtree:${suffixtreeVersion}") {
        isTransitive = false
    }
    val amecsKeyModifiers = "de.siphalor.amecs.amecs-key-modifiers:amecs-key-modifiers-${amecsMinecraftVersion}:$amecsVersionFabric"
    modCompileOnly(amecsKeyModifiers) {
        isTransitive = false
    }
    modLocalRuntime(amecsKeyModifiers) {
        isTransitive = false
    }
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
val keyMappingGametestWithoutAmecsSourceSet = sourceSets.create("keyMappingGametestWithoutAmecs") {
    runtimeClasspath += keyMappingGametestSourceSet.runtimeClasspath.filter {
        !it.name.startsWith("amecs-key-modifiers-")
    }
}

dependencies {
    "gametestImplementation"(testFixtures(project(":Common")))
}

loom {
    mods {
        create("jei") {
            sourceSet(sourceSets.main.get())
            sourceSet(apiSourceSet)
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
        named("clientGameTest") {
            systemProperties.put("fabric.client.gametest.disableNetworkSynchronizer", "true")
        }
        create("clientGameTestWithoutAmecs") {
            inherit(named("clientGameTest").get())
            displayName.set("Fabric Client GameTest Without AMECS")
            sourceSet.set(keyMappingGametestWithoutAmecsSourceSet.name)
            runDirectory.set(clientGameTestWithoutAmecsRunDirectory.get().asFile)
            systemProperties.put("fabric.client.gametest.modid", keyMappingGametestModId)
        }
    }

    accessWidenerPath.set(file("src/main/resources/jei.accesswidener"))
}

sourceSets {
    named("main") {
        resources.srcDir(dependencyResources)
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

val debugClassesTask = tasks.named(debugSourceSet.classesTaskName)
val debugModPath = layout.buildDirectory.dir("resources/${debugSourceSet.name}").get().asFile.absolutePath
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
    from(apiSourceSet.output)
    from(dependencyClasses)
    from(dependencyResources)
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

val unpackDependencySources = tasks.register<UnpackArchives>("unpackDependencySources") {
    archives.from(dependencySources)
    excludedPatterns.add("META-INF/MANIFEST.MF")
    outputDirectory.set(layout.buildDirectory.dir("generated/dependencySources"))
}

tasks.named<Jar>("sourcesJar") {
    from(sourceSets.main.get().allJava)
    from(apiSourceSet.allSource)
    from(unpackDependencySources)
    exclude("**/Readme.md")
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

val apiJarTask = tasks.register<Jar>("apiJar") {
    archiveBaseName.set(apiArchivesName)
    archiveClassifier.set("dev")
    from(apiSourceSet.output)
    manifest {
        attributes["Implementation-Title"] = "jar"
        attributes["Fabric-Loom-Remap"] = true
    }
    val manifestService = JarManifestService.get(project)
    doLast(ManifestModificationAction(
        manifestService,
        "named",
        providers.provider { loom.areEnvironmentSourceSetsSplit() },
        providers.provider { emptyList() }
    ))
    usesService(manifestService)
}

val apiSourcesJarTask = tasks.register<Jar>("apiSourcesJar") {
    archiveBaseName.set(apiArchivesName)
    archiveClassifier.set("dev-sources")
    from(apiSourceSet.allSource)
    manifest {
        attributes["Implementation-Title"] = "sourcesJar"
        attributes["Fabric-Loom-Remap"] = true
    }
}

val remapApiJar = tasks.register<RemapJarTask>("remapApiJar") {
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
val commonApiJar = configurations.create("commonApiJar") {
    isCanBeConsumed = false
    isCanBeResolved = true
}
val commonApiSourcesJar = configurations.create("commonApiSourcesJar") {
    isCanBeConsumed = false
    isCanBeResolved = true
}
dependencies {
    commonApiJar(project(path = ":Common", configuration = "apiJarElements")) { isTransitive = false }
    commonApiSourcesJar(project(path = ":Common", configuration = "apiSourcesElements")) { isTransitive = false }
}
val commonApiIntermediaryJar = tasks.register<RemapJarTask>("commonApiIntermediaryJar") {
    inputFile.set(layout.file(commonApiJar.elements.map { it.single().asFile }))
    archiveBaseName.set(commonApiIntermediaryBaseArchivesName)
    archiveClassifier.set("")
}
val commonApiIntermediarySourcesJar = tasks.register<RemapSourcesJarTask>("commonApiIntermediarySourcesJar") {
    inputFile.set(layout.file(commonApiSourcesJar.elements.map { it.single().asFile }))
    archiveBaseName.set(commonApiIntermediaryBaseArchivesName)
    archiveClassifier.set("sources")
}

tasks.named<CompatibilityTask>("checkJarCompatibility") {
    group = LifecycleBasePlugin.VERIFICATION_GROUP
    description = "Checks the Fabric API against the latest published API jar in the same major version."
    mavens.set(listOf("https://maven.blamejared.com"))
    // The plugin defaults auxiliary libraries to the main compile classpath.
    // This API check intentionally runs without them, avoiding the full Minecraft classpath.
    libraries.setFrom(emptyList<Any>())
    nonExtendableApiCheckMode.set(NonExtendableApiCheckMode.SKIP)
    fail.set(true)
    inputJar.set(remapApiJar.flatMap { it.archiveFile })
    artifact.set("${project.group}:$apiArchivesName")
}

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
    dependsOn(tasks.remapJar, tasks.remapSourcesJar, remapApiJar, remapApiSourcesJar, commonApiIntermediaryJar, commonApiIntermediarySourcesJar)
}

publishing {
    publications {
        register<MavenPublication>("commonApiIntermediary") {
            (this as MavenPublicationInternal).isAlias = true
            artifactId = commonApiIntermediaryBaseArchivesName
            artifact(commonApiIntermediaryJar)
            artifact(commonApiIntermediarySourcesJar)
        }
        register<MavenPublication>("fabricApi") {
            // Project dependencies should resolve to the main publication's coordinates.
            (this as MavenPublicationInternal).isAlias = true
            artifactId = apiArchivesName
            @Suppress("UnstableApiUsage")
            loom.disableDeprecatedPomGeneration(this)
            artifact(remapApiJar)
            artifact(remapApiSourcesJar)

            val dependencyInfos = listOf("common-api-intermediary").map {
                mapOf(
                    "groupId" to modGroup,
                    "artifactId" to "${modId}-${minecraftVersion}-$it",
                    "version" to project.version
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
        register<MavenPublication>("fabricJar") {
            @Suppress("UnstableApiUsage")
            loom.disableDeprecatedPomGeneration(this)
            artifactId = baseArchivesName
            from(components["java"])

            val dependencyInfos = listOf(
                dependencyInfo(mezzConfigDependency("fabric")) + ("scope" to "runtime"),
                dependencyInfo(mezzConfigGuiFabricDependency) + ("optional" to "true")
            )
            pom.withXml {
                val dependenciesNode =
                    ((asNode().get("dependencies") as groovy.util.NodeList).firstOrNull() as? groovy.util.Node)
                        ?: asNode().appendNode("dependencies")
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
