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
}

// gradle.properties
val curseHomepageUrl: String by extra
val curseProjectId: String by extra
val neoforgeVersion: String by extra
val jUnitVersion: String by extra
val minecraftVersion: String by extra
val minecraftVersionRangeStart: String by extra
val modGroup: String by extra
val modId: String by extra
val modJavaVersion: String by extra
val modrinthId: String by extra
val bakedSubstringIndexVersion: String by extra
val suffixtreeVersion: String by extra

// set by ORG_GRADLE_PROJECT_modrinthToken in Jenkinsfile
val modrinthToken: String? by project
// set by ORG_GRADLE_PROJECT_curseforgeApikey in Jenkinsfile
val curseforgeApikey: String? by project

val baseArchivesName = "${modId}-${minecraftVersion}-neoforge"
base {
	archivesName.set(baseArchivesName)
}

val gameTestJunitResultsDir = layout.buildDirectory.dir("test-results/gameTest")
val commonClientTestFixturesSource = project(":Common").layout.projectDirectory.dir("src/clientTestFixtures/java")

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
		java.srcDir(commonClientTestFixturesSource)
	}
}

val dependencyProjects: List<Project> = listOf(
	project(":Common"),
	project(":CommonApi"),
	project(":Library"),
	project(":Gui"),
	project(":NeoForgeApi"),
)
val debugProject = project(":Debug")

dependencyProjects.forEach {
	project.evaluationDependsOn(it.path)
}
project.evaluationDependsOn(debugProject.path)

val embeddedLibraries: Configuration by configurations.creating {
	isCanBeConsumed = false
	isCanBeResolved = true
}
configurations.implementation {
	extendsFrom(embeddedLibraries)
}
configurations.named("gameTestImplementation") {
	extendsFrom(configurations.implementation.get())
}
configurations.named("clientGameTestImplementation") {
	extendsFrom(configurations.implementation.get())
}

tasks.named<JavaCompile>(sourceSets.main.get().compileJavaTaskName) {
    dependencyProjects.forEach {
        source(it.sourceSets.main.get().allSource)
    }
}

tasks.named<ProcessResources>(sourceSets.main.get().processResourcesTaskName) {
    dependencyProjects.forEach {
        from(it.sourceSets.main.get().resources)
    }
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
	dependencyProjects.forEach {
		implementation(it)
	}
	embeddedLibraries("net.mezzdev:baked-substring-index:${bakedSubstringIndexVersion}") {
		isTransitive = false
	}
	embeddedLibraries("net.mezzdev:suffixtree:${suffixtreeVersion}") {
		isTransitive = false
	}
	"gameTestImplementation"("net.neoforged:testframework:${neoforgeVersion}") {
		isTransitive = false
	}
	"clientGameTestImplementation"(testFixtures(project(":Common")))
	testImplementation("org.junit.jupiter:junit-jupiter:${jUnitVersion}")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	changelogHtml(project(":Changelog"))
	changelogMarkdown(project(":Changelog"))
}

neoForge {
	version = neoforgeVersion
	// MDG already defaults to this, but override it for clarity.
	setAccessTransformers("src/main/resources/META-INF/accesstransformer.cfg")

	addModdingDependenciesTo(sourceSets.test.get())
	addModdingDependenciesTo(sourceSets.named("gameTest").get())
	addModdingDependenciesTo(sourceSets.named("clientGameTest").get())

	mods {
		create("jei") {
			sourceSet(sourceSets.main.get())
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
		configureEach {
			loadedMods.set(setOf(
				mods.named("jei").get()
			))
		}
		create("client") {
			client()
			loadedMods.add(mods.named("jeidebug"))
			systemProperty("forge.logging.console.level", "debug")
			gameDirectory = file("run/client/Dev")
			logLevel = Level.DEBUG
		}
		create("client_01") {
			client()
			loadedMods.add(mods.named("jeidebug"))
			gameDirectory = file("run/client/Player01")
			programArguments.addAll("--username", "Player01")
			logLevel = Level.DEBUG
		}
		create("server") {
			server()
			loadedMods.add(mods.named("jeidebug"))
			systemProperty("forge.logging.console.level", "debug")
			gameDirectory = file("run/server")
			programArguments.addAll("nogui")
			logLevel = Level.DEBUG
		}
		create("gameTestServer") {
			type.set("gameTestServer")
			gameDirectory = file("run/gameTestServer")
			sourceSet = sourceSets.named("gameTest")
			loadedMods.add(mods.named("jeitests"))
			programArguments.addAll("--tests", "jeitests:*")
			systemProperty("jei.gameTest.junitDir", gameTestJunitResultsDir.get().asFile.absolutePath)
			logLevel = Level.INFO
		}
		clientRecipeSyncRuns.forEach { (runName, testCase) ->
			create(runName) {
				client()
				gameDirectory = clientRecipeSyncTestGameDirectory(runName).asFile
				sourceSet = sourceSets.named("clientGameTest")
				loadedMods.add(mods.named("jeiclienttests"))
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
			writeClientRecipeSyncTestOptionsTasks.getValue(runName)
		)
	}
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

val sourcesJarTask = tasks.named<Jar>("sourcesJar") {
	from(sourceSets.main.get().allJava)
	for (p in dependencyProjects) {
		from(p.sourceSets.main.get().allJava)
	}
	duplicatesStrategy = DuplicatesStrategy.EXCLUDE
	archiveClassifier.set("sources")
}

publishMods {
	file.set(tasks.jar.get().archiveFile)
	type = BETA
	modLoaders.add("neoforge")
	displayName.set("${project.version} for NeoForge $minecraftVersion")
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

publishing {
	publications {
		register<MavenPublication>("neoforgeJar") {
			artifactId = baseArchivesName
			artifact(tasks.jar.get())
			artifact(sourcesJarTask.get())
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
