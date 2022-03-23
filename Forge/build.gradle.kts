import net.darkhax.curseforgegradle.TaskPublishCurseForge
import net.darkhax.curseforgegradle.Constants as CFG_Constants
import java.text.SimpleDateFormat
import java.util.*

plugins {
	id("java")
	id("idea")
	id("eclipse")
	id("maven-publish")
	id("net.minecraftforge.gradle") version ("5.1.+")
	id("org.parchmentmc.librarian.forgegradle") version ("1.+")
	id("net.darkhax.curseforgegradle") version ("1.0.8")
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
val modAuthor: String by extra
val modGroup: String by extra
val modId: String by extra
val modJavaVersion: String by extra
val modName: String by extra
val specificationVersion: String by extra

//adds the build number to the end of the version string if on a build server
var buildNumber = project.findProperty("BUILD_NUMBER")
if (buildNumber == null) {
	buildNumber = "9999"
}

version = "${specificationVersion}.${buildNumber}"
group = modGroup

val baseArchiveName = "${modId}-${minecraftVersion}"
base {
	archivesName.set(baseArchiveName)
}

sourceSets {
	val api = register("api") {
		resources {
			//The API has no resources
			setSrcDirs(emptyList<String>())
		}
	}
	named("main") {
		compileClasspath += api.get().output
		runtimeClasspath += api.get().output
	}
	named("test") {
		resources {
			//The test module has no resources
			setSrcDirs(emptyList<String>())
		}

		compileClasspath += api.get().output
		runtimeClasspath += api.get().output
	}
}

configurations {
	named("apiImplementation") {
		extendsFrom(getByName("implementation"))
	}
	named("apiRuntimeOnly") {
		extendsFrom(getByName("runtimeOnly"))
	}
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
					source(sourceSets.getByName("api"))
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
					source(sourceSets.getByName("api"))
				}
			}
		}
	}
}

tasks {
	withType<Javadoc> {
		source(sourceSets.main.get().allJava)
		source(sourceSets.getByName("api").allJava)

		// workaround cast for https://github.com/gradle/gradle/issues/7038
		val standardJavadocDocletOptions = options as StandardJavadocDocletOptions
		// prevent java 8's strict doclint for javadocs from failing builds
		standardJavadocDocletOptions.addStringOption("Xdoclint:none", "-quiet")
	}

	withType<Jar> {
		duplicatesStrategy = DuplicatesStrategy.FAIL
		finalizedBy("reobfJar")
	}

	named<Jar>("jar") {
		from(sourceSets.main.get().output)
		from(sourceSets.getByName("api").output)

		val now = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(Date())
		manifest {
			attributes(mapOf(
				"Specification-Title" to modName,
				"Specification-Vendor" to modAuthor,
				"Specification-Version" to specificationVersion,
				"Implementation-Title" to name,
				"Implementation-Version" to archiveVersion,
				"Implementation-Vendor" to modAuthor,
				"Implementation-Timestamp" to now,
			))
		}

		description = "Creates a JAR containing the compiled code, used by players."
	}

	register<Jar>("javadocJar") {
		dependsOn(javadoc.get())
		from(javadoc.get().destinationDir)

		archiveClassifier.set("javadoc")
		description = "Creates a JAR containing the javadocs, used by developers."
	}

	register<Jar>("sourcesJar") {
		from(sourceSets.main.get().allJava)
		from(sourceSets.getByName("api").allJava)

		archiveClassifier.set("sources")
		description = "Creates a JAR containing the source code, used by developers."
	}

	register<Jar>("apiJar") {
		val api = sourceSets.getByName("api")
		from(api.output)
		// TODO: when FG bug is fixed, remove allJava from the api jar.
		// https://github.com/MinecraftForge/ForgeGradle/issues/369
		// Gradle should be able to pull them from the -sources jar.
		from(api.allJava)

		archiveClassifier.set("api")
		description = "Creates an obfuscated JAR containing the API source code and javadocs, used by developers."
	}

	named<ProcessResources>("processResources") {
		// this will ensure that this task is redone when the versions change.
		inputs.property("version", version)

		duplicatesStrategy = DuplicatesStrategy.FAIL
		filesMatching("META-INF/mods.toml") {
			expand(mapOf(
				"modId" to modId,
				"version" to version,
				"minecraftVersionRange" to minecraftVersionRange,
				"forgeVersionRange" to forgeVersionRange,
				"loaderVersionRange" to loaderVersionRange,
				"githubUrl" to githubUrl
			))
		}
	}

	register<TaskPublishCurseForge>("publishCurseForge") {
		dependsOn(":makeChangelog")

		apiToken = project.findProperty("curseforge_apikey") ?: "0"

		val mainFile = upload(curseProjectId, file("${project.buildDir}/libs/$baseArchiveName-$version.jar"))
		mainFile.changelogType = CFG_Constants.CHANGELOG_HTML
		mainFile.changelog = file("../changelog.html")
		mainFile.releaseType = CFG_Constants.RELEASE_TYPE_BETA
		mainFile.addJavaVersion("Java $modJavaVersion")
		mainFile.addGameVersion(minecraftVersion)

		doLast {
			project.ext.set("curse_file_url", "${curseHomepageLink}/files/${mainFile.curseFileId}")
		}
	}
}

artifacts {
	archives(tasks.getByName("javadocJar"))
	archives(tasks.getByName("sourcesJar"))
	archives(tasks.getByName("apiJar"))
}

publishing {
	publications {
		register<MavenPublication>("maven") {
			artifactId = baseArchiveName
			setArtifacts(listOf(
				tasks.getByName("apiJar"),
				tasks.getByName("jar"),
				tasks.getByName("javadocJar"),
				tasks.getByName("sourcesJar")
			))
		}
	}
	repositories {
		val deployDir = project.findProperty("DEPLOY_DIR")
		if (deployDir != null) {
			maven(deployDir)
		} else {
			logger.info("No DEPLOY_DIR property is set, skipping maven publish.")
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
