import net.darkhax.curseforgegradle.TaskPublishCurseForge
import net.darkhax.curseforgegradle.Constants as CFG_Constants

plugins {
	java
	idea
	eclipse
	`maven-publish`
	id("net.minecraftforge.gradle") version("5.1.+")
	id("org.parchmentmc.librarian.forgegradle") version("1.+")
	id("net.darkhax.curseforgegradle") version("1.0.8")
}
apply {
	from("buildtools/AppleSiliconSupport.gradle")
}

// gradle.properties
val curseHomepageUrl: String by extra
val curseProjectId: String by extra
val forgeVersion: String by extra
val mappingsChannel: String by extra
val mappingsVersion: String by extra
val minecraftVersion: String by extra
val modId: String by extra
val modJavaVersion: String by extra
val jUnitVersion: String by extra

val baseArchivesName = "${modId}-${minecraftVersion}"
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
}

dependencies {
	"minecraft"(
		group = "net.minecraftforge",
		name = "forge",
		version = "${minecraftVersion}-${forgeVersion}"
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
	mappings(mappingsChannel, mappingsVersion)

	accessTransformer(file("src/main/resources/META-INF/accesstransformer.cfg"))

	runs {
		val client = create("client") {
			taskName("runClientDev")
			property("forge.logging.console.level", "debug")
			workingDirectory(file("run/client/Dev"))
			mods {
				create(modId) {
					source(sourceSets.main.get())
					for (p in dependencyProjects) {
						source(p.sourceSets.main.get())
					}
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
					for (p in dependencyProjects) {
						source(p.sourceSets.main.get())
					}
				}
			}
		}
	}
}

tasks.withType<Jar> {
	duplicatesStrategy = DuplicatesStrategy.EXCLUDE
	finalizedBy("reobfJar")
}

tasks.named<Jar>("jar") {
	from(sourceSets.main.get().output)
	for (p in dependencyProjects) {
		from(p.sourceSets.main.get().output)
	}

	archiveAppendix.set("forge")
}

val sourcesJar = tasks.register<Jar>("sourcesJar") {
	from(sourceSets.main.get().allJava)
	for (p in dependencyProjects) {
		from(p.sourceSets.main.get().allJava)
	}

	archiveAppendix.set("forge")
	archiveClassifier.set("sources")
}

val commonApiJar = tasks.register<Jar>("commonApiJar") {
	from(project(":CommonApi").sourceSets.main.get().output)
	archiveAppendix.set("common-api")
}

val commonApiSourcesJar = tasks.register<Jar>("commonApiSourcesJar") {
	from(project(":CommonApi").sourceSets.main.get().allJava)
	archiveAppendix.set("common-api")
	archiveClassifier.set("sources")
}

val forgeApiJar = tasks.register<Jar>("forgeApiJar") {
	from(project(":ForgeApi").sourceSets.main.get().output)
	archiveAppendix.set("forge-api")
}

val forgeApiSourcesJar = tasks.register<Jar>("forgeApiSourcesJar") {
	from(project(":ForgeApi").sourceSets.main.get().allJava)
	archiveAppendix.set("forge-api")
	archiveClassifier.set("sources")
}

tasks.register<TaskPublishCurseForge>("publishCurseForge") {
	dependsOn(tasks.jar.get().path)
	dependsOn(":Changelog:makeChangelog")

	apiToken = project.findProperty("curseforge_apikey") ?: "0"

	val mainFile = upload(curseProjectId, tasks.jar.get().archiveFile)
	mainFile.changelogType = CFG_Constants.CHANGELOG_HTML
	mainFile.changelog = file("../Changelog/changelog.html")
	mainFile.releaseType = CFG_Constants.RELEASE_TYPE_ALPHA
	mainFile.addJavaVersion("Java $modJavaVersion")
	mainFile.addGameVersion(minecraftVersion)
	mainFile.addModLoader("Forge")

	doLast {
		project.ext.set("curse_file_url", "${curseHomepageUrl}/files/${mainFile.curseFileId}")
	}
}

tasks.named<Test>("test") {
	useJUnitPlatform()
	include("mezz/jei/**")
	exclude("mezz/jei/mezz.jei.lib/**")
}

artifacts {
	archives(tasks.jar.get())
	archives(sourcesJar.get())
	archives(commonApiJar.get())
	archives(commonApiSourcesJar.get())
	archives(forgeApiJar.get())
	archives(forgeApiSourcesJar.get())
}

publishing {
	publications {
		register<MavenPublication>("fatJar") {
			val task = tasks.jar.get()
			artifactId = "${task.archiveBaseName.get()}-${task.archiveAppendix.get()}"
			artifact(task)
			artifact(sourcesJar.get())
		}
		register<MavenPublication>("commonApi") {
			val task = commonApiJar.get()
			artifactId = "${task.archiveBaseName.get()}-${task.archiveAppendix.get()}"
			artifact(task)
			artifact(commonApiSourcesJar.get())
		}
		register<MavenPublication>("forgeApi") {
			val task = forgeApiJar.get()
			artifactId = "${task.archiveBaseName.get()}-${task.archiveAppendix.get()}"
			artifact(task)
			artifact(forgeApiSourcesJar.get())
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
