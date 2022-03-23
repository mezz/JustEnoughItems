import net.darkhax.curseforgegradle.TaskPublishCurseForge
import net.darkhax.curseforgegradle.Constants as CFG_Constants
import java.text.SimpleDateFormat
import java.util.*

plugins {
	id("java")
	id("idea")
	id("eclipse")
	id("maven-publish")
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
val forgeVersionRange: String by extra
val githubUrl: String by extra
val loaderVersionRange: String by extra
val mappingsChannel: String by extra
val mappingsVersion: String by extra
val minecraftVersion: String by extra
val minecraftVersionRange: String by extra
val modId: String by extra
val modJavaVersion: String by extra
val modName: String by extra

base {
	archivesName.set("${modId}-forge-${minecraftVersion}")
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

//tasks.named<Javadoc>("javadoc") {
//	for (p in mainProjects) {
//		source(project(p).sourceSets.main.get().allJava)
//	}
//
//	source(sourceSets.main.get().allJava)
//		source(project(":Common").sourceSets.main.get().allJava)
//		source(project(":CommonApi").sourceSets.main.get().allJava)
//		source(project(":ForgeApi").sourceSets.main.get().allJava)
//
//}

tasks.named<ProcessResources>("processResources") {
	// this will ensure that this task is redone when the versions change.
	inputs.property("version", version)

	filesMatching(listOf("META-INF/mods.toml", "pack.mcmeta")) {
		expand(mapOf(
			"modId" to modId,
			"modName" to modName,
			"version" to version,
			"minecraftVersionRange" to minecraftVersionRange,
			"forgeVersionRange" to forgeVersionRange,
			"loaderVersionRange" to loaderVersionRange,
			"githubUrl" to githubUrl
		))
	}
}

tasks.named<Jar>("jar") {
	from(sourceSets.main.get().allJava)
	for (p in dependencyProjects) {
		from(p.sourceSets.main.get().allJava)
	}
	duplicatesStrategy = DuplicatesStrategy.FAIL
	finalizedBy("reobfJar")
}

//tasks.register<TaskPublishCurseForge>("publishCurseForge") {
////	dependsOn(":makeChangelog")
//
//	apiToken = project.findProperty("curseforge_apikey") ?: "0"
//
//	val mainFile = upload(curseProjectId, file("${project.buildDir}/libs/$baseArchiveName-$version.jar"))
//	mainFile.changelogType = CFG_Constants.CHANGELOG_HTML
//	mainFile.changelog = file("../changelog.html")
//	mainFile.releaseType = CFG_Constants.RELEASE_TYPE_BETA
//	mainFile.addJavaVersion("Java $modJavaVersion")
//	mainFile.addGameVersion(minecraftVersion)
//
//	doLast {
//		project.ext.set("curse_file_url", "${curseHomepageLink}/files/${mainFile.curseFileId}")
//	}
//}

//val javadocJar = tasks.register<Jar>("javadocJar") {
//	val javadoc = tasks.javadoc.get()
//	dependsOn(javadoc)
//	from(javadoc.destinationDir)
//
//	archiveClassifier.set("javadoc")
//	description = "Creates a JAR containing the javadocs, used by developers."
//}

artifacts {
	archives(tasks.jar.get())
}

publishing {
	publications {
		register<MavenPublication>("maven") {
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
