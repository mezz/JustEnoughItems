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
	from("../buildtools/Test.gradle")
	from("buildtools/AppleSiliconSupport.gradle")
}

// gradle.properties
val curseHomepageLink: String by extra
val curseProjectId: String by extra
val forgeVersion: String by extra
val mappingsChannel: String by extra
val mappingsVersion: String by extra
val minecraftVersion: String by extra
val modId: String by extra
val modJavaVersion: String by extra

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
	project(":Common"),
	project(":CommonApi"),
	project(":ForgeApi"),
)

dependencyProjects.forEach {
	project.evaluationDependsOn(it.path)
}
project.evaluationDependsOn(project(":Changelog").path)

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

tasks.named<Jar>("jar") {
	from(sourceSets.main.get().output)
	for (p in dependencyProjects) {
		from(p.sourceSets.main.get().output)
	}

	duplicatesStrategy = DuplicatesStrategy.WARN
	finalizedBy("reobfJar")
}

val apiJar = tasks.register<Jar>("apiJar") {
	from(project(":CommonApi").sourceSets.main.get().output)
	from(project(":ForgeApi").sourceSets.main.get().output)

	// TODO: when FG bug is fixed, remove allJava from the api jar.
	// https://github.com/MinecraftForge/ForgeGradle/issues/369
	// Gradle should be able to pull them from the -sources jar.
	from(project(":CommonApi").sourceSets.main.get().allJava)
	from(project(":ForgeApi").sourceSets.main.get().allJava)

	duplicatesStrategy = DuplicatesStrategy.WARN
	finalizedBy("reobfJar")
	archiveClassifier.set("api")
}

val sourcesJar = tasks.register<Jar>("sourcesJar") {
	from(sourceSets.main.get().allJava)
	for (p in dependencyProjects) {
		from(p.sourceSets.main.get().allJava)
	}

	duplicatesStrategy = DuplicatesStrategy.WARN
	finalizedBy("reobfJar")
	archiveClassifier.set("sources")
}

tasks.register<TaskPublishCurseForge>("publishCurseForge") {
	dependsOn(":Changelog:makeChangelog")

	apiToken = project.findProperty("curseforge_apikey") ?: "0"

	val mainFile = upload(curseProjectId, file("${project.buildDir}/libs/$baseArchivesName-$version.jar"))
	mainFile.changelogType = CFG_Constants.CHANGELOG_HTML
	mainFile.changelog = file("../Changelog/changelog.html")
	mainFile.releaseType = CFG_Constants.RELEASE_TYPE_BETA
	mainFile.addJavaVersion("Java $modJavaVersion")
	mainFile.addGameVersion(minecraftVersion)

	doLast {
		project.ext.set("curse_file_url", "${curseHomepageLink}/files/${mainFile.curseFileId}")
	}
}

artifacts {
	archives(apiJar.get())
	archives(sourcesJar.get())
	archives(tasks.jar.get())
}

publishing {
	publications {
		register<MavenPublication>("maven") {
			artifactId = baseArchivesName
			artifact(apiJar.get())
			artifact(sourcesJar.get())
			artifact(tasks.jar.get())
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
