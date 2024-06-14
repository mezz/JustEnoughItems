import net.darkhax.curseforgegradle.TaskPublishCurseForge
import net.neoforged.gradle.dsl.common.runs.run.Run
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import net.darkhax.curseforgegradle.Constants as CFG_Constants

plugins {
	id("java")
	id("idea")
	id("eclipse")
	id("maven-publish")
	id("net.darkhax.curseforgegradle") version("1.0.8")
	id("com.modrinth.minotaur") version("2.+")
	id("net.neoforged.gradle.userdev")
}

// gradle.properties
val curseHomepageUrl: String by extra
val curseProjectId: String by extra
val neoforgeVersion: String by extra
val jUnitVersion: String by extra
val minecraftVersion: String by extra
val modGroup: String by extra
val modId: String by extra
val modJavaVersion: String by extra

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

java {
	toolchain {
		languageVersion.set(JavaLanguageVersion.of(modJavaVersion))
	}
	withSourcesJar()
}

dependencies {
	implementation(
		group = "net.neoforged",
		name = "neoforge",
		version = neoforgeVersion
	)
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

minecraft {
	accessTransformers {
		file("src/main/resources/META-INF/accesstransformer.cfg")
	}
}

fun commonRunProperties(run: Run) {
	for (dependencyProject in dependencyProjects) {
		run.modSources.add(project.name, dependencyProject.sourceSets.main.get())
	}
}

runs {
	named("client") {
		systemProperty("forge.logging.console.level", "debug")
		workingDirectory(file("run/client/Dev"))
		commonRunProperties(this)
	}
	create("client_01") {
		configure("client")
		workingDirectory(file("run/client/Player01"))
		programArguments("--username", "Player01")
		commonRunProperties(this)
	}
	create("client_02") {
		configure("client")
		workingDirectory(file("run/client/Player02"))
		programArguments("--username", "Player02")
		commonRunProperties(this)
	}
	named("server") {
		systemProperty("forge.logging.console.level", "debug")
		workingDirectory(file("run/server"))
		commonRunProperties(this)
	}
}

val sourcesJarTask = tasks.named<Jar>("sourcesJar")
val fatJarTask = tasks.create<Jar>("fatJar") {
	from(sourceSets.main.get().output)
	dependencyProjects.forEach {
		from(it.sourceSets.main.get().output)
	}
	archiveClassifier.set("all")
}

tasks.register<TaskPublishCurseForge>("publishCurseForge") {
	dependsOn(fatJarTask)
	dependsOn(":Changelog:makeChangelog")

	apiToken = project.findProperty("curseforge_apikey") ?: "0"

	val mainFile = upload(curseProjectId, fatJarTask.archiveFile)
	mainFile.changelogType = CFG_Constants.CHANGELOG_HTML
	mainFile.changelog = file("../Changelog/changelog.html")
	mainFile.releaseType = CFG_Constants.RELEASE_TYPE_BETA
	mainFile.addJavaVersion("Java $modJavaVersion")
	mainFile.addGameVersion(minecraftVersion)
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
	uploadFile.set(fatJarTask)
	changelog.set(provider { file("../Changelog/changelog.md").readText() })
}
tasks.modrinth.get().dependsOn(fatJarTask)
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
	archives(fatJarTask)
}

publishing {
	publications {
		register<MavenPublication>("neoforgeJar") {
			artifactId = baseArchivesName
			artifact(tasks.jar.get())
			artifact(sourcesJarTask.get())

			pom.withXml {
				val dependenciesNode = asNode().appendNode("dependencies")
				dependencyProjects.forEach {
					val dependencyNode = dependenciesNode.appendNode("dependency")
					dependencyNode.appendNode("groupId", it.group)
					dependencyNode.appendNode("artifactId", it.base.archivesName.get())
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
		isDownloadJavadoc = true
		isDownloadSources = true
		for (fileName in listOf("run", "out", "logs")) {
			excludeDirs.add(file(fileName))
		}
	}
}
