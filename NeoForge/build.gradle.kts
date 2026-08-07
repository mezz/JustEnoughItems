import mezz.jei.gradle.optionalGradleProperty
import mezz.jei.gradle.gradleProperty
import org.gradle.api.publish.maven.internal.publication.MavenPublicationInternal
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import net.neoforged.moddevgradle.dsl.ModModel
import org.slf4j.event.Level
import java.io.File

plugins {
	id("java")
	id("idea")
	id("eclipse")
	id("maven-publish")
	id("me.modmuss50.mod-publish-plugin")
	id("net.neoforged.moddev")
	id("net.mezzdev.modshade")
}

// gradle.properties
val curseHomepageUrl = gradleProperty("curseHomepageUrl")
val curseProjectId = gradleProperty("curseProjectId")
val neoforgeVersion = gradleProperty("neoforgeVersion")
val jUnitVersion = gradleProperty("jUnitVersion")
val minecraftVersion = gradleProperty("minecraftVersion")
val minecraftVersionRangeStart = gradleProperty("minecraftVersionRangeStart")
val modGroup = gradleProperty("modGroup")
val modId = gradleProperty("modId")
val modJavaVersion = gradleProperty("modJavaVersion")
val modrinthId = gradleProperty("modrinthId")
val mezzConfigCurseForgeProjectSlug = gradleProperty("mezzConfigCurseForgeProjectSlug")
val mezzConfigModrinthProjectId = gradleProperty("mezzConfigModrinthProjectId")
val mezzConfigGuiCurseForgeProjectSlug = gradleProperty("mezzConfigGuiCurseForgeProjectSlug")
val mezzConfigGuiModrinthProjectId = gradleProperty("mezzConfigGuiModrinthProjectId")
val bakedSubstringIndexVersion = gradleProperty("bakedSubstringIndexVersion")
val suffixtreeVersion = gradleProperty("suffixtreeVersion")
val deduplicatingRunnerVersion = gradleProperty("deduplicatingRunnerVersion")
val mezzConfigVersion = gradleProperty("mezzConfigVersion")
val mezzConfigVersionRange = gradleProperty("mezzConfigVersionRange")
val mezzConfigApiDependency: String by rootProject.extra
val mezzConfigNeoForgeDependency: String by rootProject.extra
val mezzConfigGuiApiDependency: String by rootProject.extra
val mezzConfigGuiNeoForgeDependency: String by rootProject.extra

// set by ORG_GRADLE_PROJECT_modrinthToken in Jenkinsfile
val modrinthToken = optionalGradleProperty("modrinthToken")
// set by ORG_GRADLE_PROJECT_curseforgeApikey in Jenkinsfile
val curseforgeApikey = optionalGradleProperty("curseforgeApikey")

val baseArchivesName = "${modId}-${minecraftVersion}-neoforge"
val apiArchivesName = "${modId}-${minecraftVersion}-neoforge-api"
base {
	archivesName.set(baseArchivesName)
}

val apiSourceSet = sourceSets.create("api") {
    resources.setSrcDirs(emptyList<String>())
    compileClasspath += configurations.compileClasspath.get()
}

val gameTestJunitResultsDir = layout.buildDirectory.dir("test-results/gameTest")
val commonProjectDirectory = project(":Common").layout.projectDirectory
val commonClientTestFixturesSource = commonProjectDirectory.dir("src/clientTestFixtures/java")

sourceSets {
	named("test") {
		resources {
			//The test module has no resources
			setSrcDirs(emptyList<String>())
		}
	}
	create("gameTest") {
		compileClasspath += sourceSets.main.get().output
		runtimeClasspath += sourceSets.main.get().output
	}
	create("clientGameTest") {
		java.srcDir(project(":Common").layout.projectDirectory.dir("src/testFixtures/java"))
		java.srcDir(commonClientTestFixturesSource)
	}
}

val dependencyProjects: List<Project> = listOf(
	project(":Common"),

	project(":Library"),
	project(":Gui"),

)
val debugProject = project(":Debug")

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

configurations.named("gameTestImplementation") {
	extendsFrom(configurations.implementation.get())
}
configurations.named("clientGameTestImplementation") {
	extendsFrom(configurations.implementation.get())
}
configurations.named("clientGameTestCompileOnly") {
	extendsFrom(configurations.compileOnly.get())
}

tasks.named<JavaCompile>(sourceSets.main.get().compileJavaTaskName) {
    source(commonApiSourceSet.allJava)
    dependencyProjects.forEach {
        source(it.sourceSets.main.get().allSource)
    }
}

tasks.named<ProcessResources>(sourceSets.main.get().processResourcesTaskName) {
    dependencyProjects.forEach {
        from(it.sourceSets.main.get().resources)
    }
}

tasks.named(LifecycleBasePlugin.CHECK_TASK_NAME) {
	dependsOn(
		"runGameTestServer",
		tasks.named(sourceSets.named("clientGameTest").get().classesTaskName)
	)
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

val neoForgeServerWithJeiRunName = "neoForgeServerWithJei"
val neoForgeServerWithoutJeiRunName = "neoForgeServerWithoutJei"
val vanillaServerRunName = "vanillaServer"
val clientRecipeSyncTestProperty = "jei.clientRecipeSyncTest"
val clientRecipeSyncTestRunName = "clientRecipeSyncTest"
val clientResourcePackName = "jei-client-test-pack"
val clientRecipeSyncTestCaseRuns = listOf(
	"clientRecipeSyncSingleplayer" to "singleplayer",
	"clientRecipeSyncNeoForgeServerWithJei" to "neoforgeServerWithJei",
	"clientRecipeSyncNeoForgeServerWithoutJei" to "neoforgeServerWithoutJei",
	"clientRecipeSyncVanillaServerWithoutJei" to "vanillaServerWithoutJei",
)
val clientRecipeSyncRuns = listOf(clientRecipeSyncTestRunName to "all") + clientRecipeSyncTestCaseRuns

fun clientRecipeSyncTestGameDirectory(runName: String) =
	layout.projectDirectory.dir("run/$runName")

fun clientRecipeSyncTestConfigDirectory(runName: String) =
	layout.projectDirectory.dir("run/$runName/config")

fun capitalizedRunName(runName: String): String =
	runName.replaceFirstChar { it.uppercase() }

fun Configuration.singleFileContents(): Provider<String> =
	incoming
		.files
		.elements
		.map { elements -> elements.single() }
		.map { it.asFile.readText() }

dependencies {
	compileOnly(mezzConfigApiDependency)
	runtimeOnly(mezzConfigNeoForgeDependency)
	jarJar(mezzConfigNeoForgeDependency) {
		version {
			strictly(mezzConfigVersionRange)
			prefer(mezzConfigVersion)
		}
	}
	compileOnly(mezzConfigGuiApiDependency)
	runtimeOnly(mezzConfigGuiNeoForgeDependency)
	dependencyProjects.forEach {
		compileOnly(it)
	}
	modShadeImplementation("net.mezzdev:baked-substring-index:${bakedSubstringIndexVersion}") {
		isTransitive = false
	}
	modShadeImplementation("net.mezzdev:deduplicating-runner:${deduplicatingRunnerVersion}") {
		isTransitive = false
	}
	modShadeImplementation("net.mezzdev:suffixtree:${suffixtreeVersion}") {
		isTransitive = false
	}
	"gameTestImplementation"("net.neoforged:testframework:${neoforgeVersion}") {
		isTransitive = false
	}
	"clientGameTestCompileOnly"("org.jspecify:jspecify:1.0.0")
	"gameTestRuntimeOnly"(mezzConfigNeoForgeDependency)
	"gameTestRuntimeOnly"(mezzConfigGuiNeoForgeDependency)
	"clientGameTestRuntimeOnly"(mezzConfigNeoForgeDependency)
	"clientGameTestRuntimeOnly"(mezzConfigGuiNeoForgeDependency)
	testImplementation(mezzConfigApiDependency)
	testImplementation(mezzConfigGuiApiDependency)
	testImplementation(
		group = "org.junit.jupiter",
		name = "junit-jupiter",
		version = jUnitVersion
	)
	testRuntimeOnly(
		group = "org.junit.platform",
		name = "junit-platform-launcher"
	)
	changelogHtml(project(":Changelog"))
	changelogMarkdown(project(":Changelog"))
}

neoForge {
	version = neoforgeVersion
	// MDG already defaults to this, but override it for clarity.
	setAccessTransformers("src/main/resources/META-INF/accesstransformer.cfg")
	validateAccessTransformers = true

	addModdingDependenciesTo(sourceSets.test.get())
	addModdingDependenciesTo(sourceSets.named("gameTest").get())
	addModdingDependenciesTo(sourceSets.named("clientGameTest").get())

	mods {
		create("jei") {
			sourceSet(sourceSets.main.get())
				sourceSet(apiSourceSet)
			for (dependencyProject in dependencyProjects) {
				sourceSet(dependencyProject.sourceSets.main.get())
			}
		}
		create("jeidebug") {
			sourceSet(debugProject.sourceSets.main.get())
		}
		create("jeitests") {
			sourceSet(sourceSets.named("gameTest").get())
		}
		create("jeiclienttests") {
			sourceSet(sourceSets.named("clientGameTest").get())
		}
	}

	runs {
		val jeiMod = mods.named("jei")
		val jeiDebugMod = mods.named("jeidebug")
		val jeiTestsMod = mods.named("jeitests")
		val jeiClientTestsMod = mods.named("jeiclienttests")

		configureEach {
			loadedMods.set(setOf(
				jeiMod.get()
			))
		}
		create("client") {
			client()
			loadedMods.add(jeiDebugMod)
			systemProperty("forge.logging.console.level", "debug")
			gameDirectory = file("run/client/Dev")
			logLevel = Level.DEBUG
		}
		create("client_01") {
			client()
			loadedMods.add(jeiDebugMod)
			gameDirectory = file("run/client/Player01")
			programArguments.addAll("--username", "Player01")
			logLevel = Level.DEBUG
		}
		create("server") {
			server()
			loadedMods.add(jeiDebugMod)
			systemProperty("forge.logging.console.level", "debug")
			gameDirectory = file("run/server")
			programArguments.addAll("nogui")
			logLevel = Level.DEBUG
		}
		create("gameTestServer") {
			type.set("gameTestServer")
			gameDirectory = file("run/gameTestServer")
			sourceSet = sourceSets.named("gameTest")
			loadedMods.set(setOf(jeiMod.get(), jeiTestsMod.get()))
			systemProperty("jei.gameTest.junitDir", gameTestJunitResultsDir.get().asFile.absolutePath)
			logLevel = Level.INFO
		}
		clientRecipeSyncRuns.forEach { (runName, testCase) ->
			create(runName) {
				client()
				gameDirectory = clientRecipeSyncTestGameDirectory(runName).asFile
				sourceSet = sourceSets.named("clientGameTest")
				loadedMods.set(setOf(jeiMod.get(), jeiClientTestsMod.get()))
				programArguments.addAll("--username", "JeiClientTest")
				systemProperty(clientRecipeSyncTestProperty, testCase)
				logLevel = Level.INFO
			}
		}
		create(neoForgeServerWithJeiRunName) {
			server()
			gameDirectory = file("run/$neoForgeServerWithJeiRunName")
			programArguments.addAll("nogui")
			logLevel = Level.INFO
		}
		create(neoForgeServerWithoutJeiRunName) {
			server()
			gameDirectory = file("run/$neoForgeServerWithoutJeiRunName")
			loadedMods.set(emptySet())
			programArguments.addAll("nogui")
			logLevel = Level.INFO
		}
	}
}

fun neoForgeServerRunFile(runName: String, suffix: String): File =
	layout.buildDirectory.file("moddev/$runName$suffix").get().asFile

fun modFoldersProperty(vararg mods: ModModel): String =
	mods.asSequence()
		.flatMap { mod ->
			mod.modSourceSets.get().asSequence()
				.flatMap { sourceSet ->
					sourceSet.output.files.asSequence()
						.map { file -> "${mod.name}%%${file.absolutePath}" }
				}
		}
		.joinToString(File.pathSeparator)

fun vanillaServerRunFile(suffix: String): File =
	project(":Common").layout.buildDirectory.file("moddev/$vanillaServerRunName$suffix").get().asFile

val writeExternalServerLaunchProperties = tasks.register<WriteProperties>("writeExternalServerLaunchProperties") {
	destinationFile.set(layout.buildDirectory.file("generated/externalServerLaunch/resources/jei-external-server-launch.properties"))
	property("neoForgeServerWithJei.classpathArgsFile", neoForgeServerRunFile(neoForgeServerWithJeiRunName, "RunClasspath.txt").absolutePath)
	property("neoForgeServerWithJei.vmArgsFile", neoForgeServerRunFile(neoForgeServerWithJeiRunName, "RunVmArgs.txt").absolutePath)
	property("neoForgeServerWithJei.programArgsFile", neoForgeServerRunFile(neoForgeServerWithJeiRunName, "RunProgramArgs.txt").absolutePath)
	property("neoForgeServerWithJei.modFolders", modFoldersProperty(
		neoForge.mods.named("jei").get()
	))
	property("neoForgeServerWithoutJei.classpathArgsFile", neoForgeServerRunFile(neoForgeServerWithoutJeiRunName, "RunClasspath.txt").absolutePath)
	property("neoForgeServerWithoutJei.vmArgsFile", neoForgeServerRunFile(neoForgeServerWithoutJeiRunName, "RunVmArgs.txt").absolutePath)
	property("neoForgeServerWithoutJei.programArgsFile", neoForgeServerRunFile(neoForgeServerWithoutJeiRunName, "RunProgramArgs.txt").absolutePath)
	property("neoForgeServerWithoutJei.modFolders", "")
	property("vanillaServer.classpathArgsFile", vanillaServerRunFile("RunClasspath.txt").absolutePath)
	property("vanillaServer.vmArgsFile", vanillaServerRunFile("RunVmArgs.txt").absolutePath)
	property("vanillaServer.programArgsFile", vanillaServerRunFile("RunProgramArgs.txt").absolutePath)
	property("vanillaServer.modFolders", "")
	dependsOn(
		":Common:createVanillaServerLaunchScript",
		"createNeoForgeServerWithJeiLaunchScript",
		"createNeoForgeServerWithoutJeiLaunchScript"
	)
}

tasks.named<ProcessResources>(sourceSets.named("clientGameTest").get().processResourcesTaskName) {
	from(writeExternalServerLaunchProperties)
}

val copyClientGameTestModMetadataToClasses = tasks.register<Copy>("copyClientGameTestModMetadataToClasses") {
	// ModDevGradle exposes classes and resources as separate mod roots on this branch.
	from(layout.buildDirectory.file("resources/clientGameTest/META-INF/neoforge.mods.toml"))
	into(layout.buildDirectory.dir("classes/java/clientGameTest/META-INF"))
	dependsOn(
		tasks.named(sourceSets.named("clientGameTest").get().compileJavaTaskName),
		tasks.named(sourceSets.named("clientGameTest").get().processResourcesTaskName)
	)
}

tasks.named(sourceSets.named("clientGameTest").get().classesTaskName) {
	dependsOn(copyClientGameTestModMetadataToClasses)
}

val copyClientRecipeSyncTestFmlConfigTasks = clientRecipeSyncRuns.associate { (runName, _) ->
	runName to tasks.register<Copy>("copy${capitalizedRunName(runName)}FmlConfig") {
		from(layout.projectDirectory.file("src/clientGameTest/templates/config/fml.toml"))
		into(clientRecipeSyncTestConfigDirectory(runName))
	}
}

val writeClientRecipeSyncTestOptionsTasks = clientRecipeSyncRuns.associate { (runName, _) ->
	runName to tasks.register<Copy>("write${capitalizedRunName(runName)}Options") {
		from(layout.projectDirectory.file("src/clientGameTest/templates/options.txt"))
		into(clientRecipeSyncTestGameDirectory(runName))
	}
}

val copyClientResourcePackTasks = clientRecipeSyncRuns.associate { (runName, _) ->
	runName to tasks.register<Sync>("copy${capitalizedRunName(runName)}ResourcePack") {
		from(layout.projectDirectory.file("src/clientGameTest/templates/resourcepacks/$clientResourcePackName/pack.mcmeta"))
		// Override JEI's 16x16 config button with an existing 32x32 texture to catch stale atlas coordinates.
		from(commonProjectDirectory.file("src/main/resources/assets/jei/textures/gui/sprites/icons/shapeless_icon_v2.png")) {
			into("assets/jei/textures/gui/sprites/icons")
			rename { "config_button.png" }
		}
		into(clientRecipeSyncTestGameDirectory(runName).dir("resourcepacks/$clientResourcePackName"))
	}
}

val cleanGameTestJunitResults = tasks.register<Delete>("cleanGameTestJunitResults") {
	description = "Deletes NeoForge game test JUnit result files before running game tests."
	delete(gameTestJunitResultsDir)
}

tasks.named("runGameTestServer") {
	dependsOn(cleanGameTestJunitResults)
}

clientRecipeSyncRuns.forEach { (runName, _) ->
	tasks.named("prepare${capitalizedRunName(runName)}Run") {
		dependsOn(
			writeExternalServerLaunchProperties,
			copyClientRecipeSyncTestFmlConfigTasks.getValue(runName),
			writeClientRecipeSyncTestOptionsTasks.getValue(runName),
			copyClientResourcePackTasks.getValue(runName)
		)
	}
}

tasks.jar {
    from(apiSourceSet.output)
	from(sourceSets.main.get().output)
	for (p in dependencyProjects) {
		from(p.sourceSets.main.get().output)
	}

	duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

val sourcesJarTask = tasks.named<Jar>("sourcesJar") {
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

publishMods {
	file.set(shadedJar.flatMap { it.archiveFile })
	type = BETA
	modLoaders.add("neoforge")
	displayName.set("${project.version} for NeoForge $minecraftVersion")
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

tasks.test {
	useJUnitPlatform()
	include("mezz/jei/gui/**")
	include("mezz/jei/neoforge/**")
	include("mezz/jei/test/**")
	exclude("mezz/jei/test/lib/**")
	outputs.upToDateWhen { false }
	testLogging {
		events = setOf(TestLogEvent.FAILED)
		exceptionFormat = TestExceptionFormat.FULL
	}
}

tasks.assemble {
	dependsOn(sourcesJarTask)
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

tasks.assemble {
    dependsOn(apiJarTask, apiSourcesJarTask)
}

publishing {
	publications {
        register<MavenPublication>("neoforgeApiJar") {
            // Project dependencies continue to use the main publication's coordinates.
            (this as MavenPublicationInternal).isAlias = true
            artifactId = apiArchivesName
            artifact(apiJarTask)
            artifact(apiSourcesJarTask)
            val apiDependencyInfo = mapOf(
                "groupId" to project.group,
                "artifactId" to "${modId}-${minecraftVersion}-common-api",
                "version" to project.version
            )
            pom.withXml {
                val dependency = asNode().appendNode("dependencies").appendNode("dependency")
                apiDependencyInfo.forEach { (key, value) ->
                    dependency.appendNode(key, value)
                }
            }
        }

		register<MavenPublication>("neoforgeJar") {
			artifactId = baseArchivesName
			artifact(shadedJar)
			artifact(shadedSourcesJar)

			val dependencyInfos = listOf(
				dependencyInfo(mezzConfigGuiNeoForgeDependency) + ("optional" to "true")
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
