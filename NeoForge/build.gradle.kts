import mezz.jei.gradle.gradleProperty
import mezz.jei.gradle.isolatedProjectDirectory
import mezz.jei.gradle.optionalGradleProperty
import net.neoforged.moddevgradle.dsl.ModModel
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
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
val bakedSubstringIndexVersion = gradleProperty("bakedSubstringIndexVersion")
val suffixtreeVersion = gradleProperty("suffixtreeVersion")

// set by ORG_GRADLE_PROJECT_modrinthToken in Jenkinsfile
val modrinthToken = optionalGradleProperty("modrinthToken")
// set by ORG_GRADLE_PROJECT_curseforgeApikey in Jenkinsfile
val curseforgeApikey = optionalGradleProperty("curseforgeApikey")

val baseArchivesName = "${modId}-${minecraftVersion}-neoforge"
base {
	archivesName.set(baseArchivesName)
}

val gameTestJunitResultsDir = layout.buildDirectory.dir("test-results/gameTest")
val dependencyProjectPaths = listOf(":Common", ":CommonApi", ":Library", ":Gui", ":NeoForgeApi")
val dependencyProjectDirectories = dependencyProjectPaths.map { isolatedProjectDirectory(it) }
val commonProjectDirectory = isolatedProjectDirectory(":Common")
val debugProjectDirectory = isolatedProjectDirectory(":Debug")
val commonClientTestFixturesSource = commonProjectDirectory.dir("src/clientTestFixtures/java")

sourceSets {
	named("main") {
		java {
			dependencyProjectDirectories.forEach {
				srcDir(it.dir("src/main/java"))
			}
		}
	}
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

val debugSourceSet = sourceSets.create("debug") {
	java.srcDir(debugProjectDirectory.dir("src/main/java"))
	resources.srcDir(debugProjectDirectory.dir("src/main/resources"))
	compileClasspath += sourceSets.main.get().compileClasspath
	runtimeClasspath += output + compileClasspath
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
	dependencyProjectPaths.forEach {
		compileOnly(project(it))
	}
	modShadeImplementation("net.mezzdev:baked-substring-index:${bakedSubstringIndexVersion}") {
		isTransitive = false
	}
	modShadeImplementation("net.mezzdev:suffixtree:${suffixtreeVersion}") {
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
	validateAccessTransformers = true

	addModdingDependenciesTo(sourceSets.test.get())
	addModdingDependenciesTo(sourceSets.named("gameTest").get())
	addModdingDependenciesTo(sourceSets.named("clientGameTest").get())

	mods {
		create("jei") {
			sourceSet(sourceSets.main.get())
		}
		create("jeidebug") {
			sourceSet(debugSourceSet)
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
	commonProjectDirectory.file("build/moddev/$vanillaServerRunName$suffix").asFile

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

val copyClientResourcePackTasks = clientRecipeSyncRuns.associate { (runName, _) ->
	runName to tasks.register<Sync>("copy${capitalizedRunName(runName)}ResourcePack") {
		from(layout.projectDirectory.file("src/clientGameTest/templates/resourcepacks/$clientResourcePackName/pack.mcmeta"))
		// Override JEI's 16x16 config button with an existing 32x32 texture to catch stale atlas coordinates.
		from(commonProjectDirectory.file("src/main/resources/assets/jei/textures/jei/atlas/gui/icons/shapeless_icon.png")) {
			into("assets/jei/textures/jei/atlas/gui/icons")
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
	from(sourceSets.main.get().output)
	duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

val sourcesJarTask = tasks.named<Jar>("sourcesJar") {
	from(sourceSets.main.get().allJava)
	exclude("**/Readme.md")
	duplicatesStrategy = DuplicatesStrategy.EXCLUDE
	archiveClassifier.set("sources")
}

tasks.named<ProcessResources>(sourceSets.main.get().processResourcesTaskName) {
	dependsOn(":Common:generateJeiGuiColors")
	dependencyProjectDirectories.forEach {
		from(it.dir("src/main/resources"))
	}
	from(commonProjectDirectory.dir("build/generated/resources/jeiGuiColors"))
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
