import net.darkhax.curseforgegradle.TaskPublishCurseForge
import net.minecraftforge.gradle.common.tasks.DownloadMavenArtifact
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import net.darkhax.curseforgegradle.Constants as CFG_Constants

plugins {
	id("java")
	id("idea")
	id("eclipse")
	id("maven-publish")
	id("net.minecraftforge.gradle")
	id("org.parchmentmc.librarian.forgegradle")
	id("net.darkhax.curseforgegradle")
	id("com.modrinth.minotaur")
}

// gradle.properties
val curseHomepageUrl: String by extra
val curseProjectId: String by extra
val forgeVersion: String by extra
val jUnitVersion: String by extra
val minecraftVersion: String by extra
val modGroup: String by extra
val modId: String by extra
val modJavaVersion: String by extra
val parchmentVersionForge: String by extra

val resourceProperties: Map<String, String> by rootProject.extra

// set by ORG_GRADLE_PROJECT_modrinthToken in Jenkinsfile
val modrinthToken: String? by project

val baseArchivesName = "${modId}-${minecraftVersion}-forge"
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
	project(":ForgeApi"),
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

// Hack fix: FG can't resolve deps like lwjgl-freetype-3.3.3-natives-macos-patch.jar without this
repositories {
	maven("https://libraries.minecraft.net")
}

dependencies {
	"minecraft"(
		group = "net.minecraftforge",
		name = "forge",
		version = "${minecraftVersion}-${forgeVersion}"
	)
	dependencyProjects.forEach {
		compileOnly(it)
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

	// Hack fix for now, force jopt-simple to be exactly 5.0.4 because Mojang ships that version, but some transitive dependencies request 6.0+
	implementation("net.sf.jopt-simple:jopt-simple:5.0.4") {
		version {
			strictly("5.0.4")
		}
	}
}

minecraft {
	mappings("official", minecraftVersion)

	// use Official mappings at runtime
	reobf = false

	copyIdeResources.set(true)

	accessTransformer(file("src/main/resources/META-INF/accesstransformer.cfg"))

	runs {
		val client = create("client") {
			taskName("runClientDev")
			property("forge.logging.console.level", "debug")
			workingDirectory(file("run/client/Dev"))
			mods {
				create(modId) {
					source(sourceSets.main.get())
				}
			}
		}
		create("client_01") {
			taskName("runClientPlayer01")
			parent(client)
			workingDirectory(file("run/client/Player01"))
			args("--username", "Player01")
		}
		create("client_02") {
			taskName("runClientPlayer02")
			parent(client)
			workingDirectory(file("run/client/Player02"))
			args("--username", "Player02")
		}
		create("server") {
			taskName("Server")
			property("forge.logging.console.level", "debug")
			workingDirectory(file("run/server"))
			mods {
				create(modId) {
					source(sourceSets.main.get())
				}
			}
		}
	}
}

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

	// this will ensure that this task is redone when the properties change.
	inputs.properties(resourceProperties)

	filesMatching("META-INF/mods.toml") {
		expand(resourceProperties)
	}
}

tasks.jar {
	from(sourceSets.main.get().output)

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
	mainFile.addModLoader("Forge")

	doLast {
		project.ext.set("curse_file_url", "${curseHomepageUrl}/files/${mainFile.curseFileId}")
	}
}

modrinth {
	token.set(modrinthToken)
	projectId.set("jei")
	versionNumber.set("${project.version}")
	versionName.set("${project.version} for Forge $minecraftVersion")
	versionType.set("beta")
	uploadFile.set(tasks.jar.get())
	changelog.set(provider { file("../Changelog/changelog.md").readText() })
	detectLoaders.set(false)
	loaders.add("forge")
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
		register<MavenPublication>("forgeJar") {
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
// Required because FG, copied from the MDK
sourceSets.forEach {
    val outputDir = layout.buildDirectory.file("sourcesSets/${it.name}").get().asFile
    it.output.setResourcesDir(outputDir)
    it.java.destinationDirectory.set(outputDir)
}

tasks.withType<DownloadMavenArtifact> {
	notCompatibleWithConfigurationCache("uses Task.project at execution time")
}
