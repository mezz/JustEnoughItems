import net.darkhax.curseforgegradle.TaskPublishCurseForge
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import net.darkhax.curseforgegradle.Constants as CFG_Constants

plugins {
	id("java")
	id("idea")
	id("eclipse")
	id("maven-publish")
	id("net.darkhax.curseforgegradle")
	id("com.modrinth.minotaur")
	id("net.neoforged.moddev")
}

// gradle.properties
val curseHomepageUrl: String by extra
val curseProjectId: String by extra
val neoforgeVersion: String by extra
val jUnitVersion: String by extra
val minecraftVersion: String by extra
val minecraftExtraCompatibleVersion: String by extra
val modGroup: String by extra
val modId: String by extra
val modJavaVersion: String by extra

val resourceProperties: Map<String, String> by rootProject.extra

// set by ORG_GRADLE_PROJECT_modrinthToken in Jenkinsfile
val modrinthToken: String? by project

val baseArchivesName = "${modId}-${minecraftVersion}-neoforge"
base {
	archivesName.set(baseArchivesName)
}

sourceSets {
	named("test") {
		resources {
			//The test module has no resources
			setSrcDirs(emptyList<String>())
		}
	}
}

val dependencyProjects: List<Project> = listOf(
	project(":Core"),
	project(":Common"),
	project(":CommonApi"),
	project(":Library"),
	project(":Gui"),
	project(":NeoForgeApi"),
)

dependencyProjects.forEach {
	project.evaluationDependsOn(it.path)
}
project.evaluationDependsOn(":Changelog")

tasks.withType<JavaCompile>().configureEach {
    dependencyProjects.forEach {
        source(it.sourceSets.main.get().allSource)
    }
}

tasks.withType<ProcessResources> {
    dependencyProjects.forEach {
		inputs.files(it.sourceSets.main.get().resources)
        from(it.sourceSets.main.get().resources)
    }
}

java {
	toolchain {
		languageVersion.set(JavaLanguageVersion.of(modJavaVersion))
	}
	withSourcesJar()
}

dependencies {
	dependencyProjects.forEach {
		implementation(it)
	}
	testImplementation(
		group = "org.junit.jupiter",
		name = "junit-jupiter-api",
		version = jUnitVersion
	)
	testRuntimeOnly(
		group = "org.junit.jupiter",
		name = "junit-jupiter-engine",
		version = jUnitVersion
	)
}

neoForge {
	version = neoforgeVersion
	// MDG already defaults to this, but override it for clarity.
	setAccessTransformers("src/main/resources/META-INF/accesstransformer.cfg")

	addModdingDependenciesTo(sourceSets.test.get())

	mods {
		create("jei") {
			sourceSet(sourceSets.main.get())
			for (dependencyProject in dependencyProjects) {
				sourceSet(dependencyProject.sourceSets.main.get())
			}
		}
	}

	runs {
		create("client") {
			client()
			systemProperty("forge.logging.console.level", "debug")
			gameDirectory = file("run/client/Dev")
		}
		create("client_01") {
			client()
			gameDirectory = file("run/client/Player01")
			programArguments.addAll("--username", "Player01")
		}
		create("client_02") {
			client()
			gameDirectory = file("run/client/Player02")
			programArguments.addAll("--username", "Player02")
		}
		create("server") {
			server()
			systemProperty("forge.logging.console.level", "debug")
			gameDirectory = file("run/server")
			programArguments.addAll("nogui")
		}
	}
}

tasks.jar {
	from(sourceSets.main.get().output)
	for (p in dependencyProjects) {
		from(p.sourceSets.main.get().output)
	}

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

tasks.register<TaskPublishCurseForge>("publishCurseForge") {
	dependsOn(tasks.jar)
	dependsOn(":Changelog:makeChangelog")

	apiToken = project.findProperty("curseforge_apikey") ?: "0"

	val mainFile = upload(curseProjectId, tasks.jar.get().archiveFile)
	mainFile.changelogType = CFG_Constants.CHANGELOG_HTML
	mainFile.changelog = file("../Changelog/changelog.html")
	mainFile.releaseType = CFG_Constants.RELEASE_TYPE_BETA
	mainFile.addJavaVersion("Java $modJavaVersion")
	mainFile.addGameVersion(minecraftVersion)
	mainFile.addGameVersion(minecraftExtraCompatibleVersion)
	mainFile.addModLoader("NeoForge")

	doLast {
		project.ext.set("curse_file_url", "${curseHomepageUrl}/files/${mainFile.curseFileId}")
	}
}

modrinth {
	token.set(modrinthToken)
	projectId.set("jei")
	versionNumber.set("${project.version}")
	versionName.set("${project.version} for NeoForge $minecraftVersion")
	versionType.set("beta")
	uploadFile.set(tasks.jar.get())
	changelog.set(provider { file("../Changelog/changelog.md").readText() })
	gameVersions.set(listOf(minecraftVersion, minecraftExtraCompatibleVersion))
	detectLoaders.set(false)
	loaders.add("neoforge")
}
tasks.modrinth.get().dependsOn(tasks.jar)
tasks.modrinth.get().dependsOn(":Changelog:makeMarkdownChangelog")

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
	archives(tasks.jar.get())
	archives(sourcesJarTask.get())
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
		for (fileName in listOf("run", "out", "logs")) {
			excludeDirs.add(file(fileName))
		}
	}
}
