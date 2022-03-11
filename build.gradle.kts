buildscript {
	repositories {
		maven { setUrl("https://maven.minecraftforge.net") }
		maven { setUrl("https://maven.parchmentmc.org") }
		mavenCentral()
	}
	dependencies {
		classpath(
			group: "net.minecraftforge.gradle",
			name: "ForgeGradle",
			version: "5.1.+",
		) {
			changing = true
		}
		classpath(
			group: "org.parchmentmc",
			name: "librarian",
			version: "1.+"
		)
	}
}

plugins {
	id("java")
	id("eclipse")
	id("idea")
	id("maven-publish")
	id("com.matthewprenger.cursegradle") version("1.4.0")
	id("se.bjurr.gitchangelog.git-changelog-gradle-plugin") version("1.71.4")
	id("com.diffplug.spotless") version("5.14.3")
}
apply {
	plugin("net.minecraftforge.gradle")
	plugin("org.parchmentmc.librarian.forgegradle")
	from("buildtools/ColoredOutput.gradle")
	from("buildtools/AppleSiliconSuport.gradle")
	from("buildtools/Test.gradle")
}

// gradle.properties
//val modName: String by extra
//val modId: String by extra
//val modAuthor: String by extra
//val modGroup: String by extra
//val forgeVersion: String by extra
//val minecraftVersion: String by extra
//val mappingChannel: String by extra
//val mappingVersion: String by extra
//val specificationVersion: String by extra

//adds the build number to the end of the version string if on a build server
var buildNumber = System.getenv()["BUILD_NUMBER"]
if (buildNumber == null) {
	buildNumber = "9999"
}

version = "${specificationVersion}.${buildNumber}"
group = "${modGroup}"

def baseArchiveName = "${modName}-${minecraftVersion}"
base {
	archivesName.set(baseArchiveName)
}

sourceSets {
	def api = create("api") {
		//The API has no resources
		resources.srcDirs = []

		java {
			srcDir("src/api/java")
		}
	}
	named("main") {
		compileClasspath += api.output
		runtimeClasspath += api.output
		java {
			srcDir("src/main/java")
		}
	}
	named("test") {
		//The test module has no resources
		resources.srcDirs = []

		compileClasspath += api.output
		runtimeClasspath += api.output
		java {
			srcDir("src/test/java")
		}
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
		// Mojang ships Java 17 to end users in 1.18+
		languageVersion.set(JavaLanguageVersion.of(17))
	}
}

dependencies {
	"minecraft"(
		group: "net.minecraftforge",
		name: "forge",
		version: "${minecraftVersion}-${forgeVersion}"
	)
}

minecraft {
	mappings(channel: "${mappingChannel}", version: "${mappingVersion}")

	accessTransformer = file("src/main/resources/META-INF/accesstransformer.cfg")

	runs(closureOf<NamedDomainObjectContainer<RunConfig>> {
		client {
			taskName("Client")
			property("forge.logging.console.level", "debug")
			workingDirectory(file("runClient"))
			mods {
				create(modId) {
					source(sourceSets.main.get())
					source(sourceSets.api.get())
				}
			}
		}
		client_player_01 {
			taskName("Client Player 01")
			parent(runs.client)
			workingDirectory(file("runClient01"))
			args("--username", "Player01")
		}
		client_player_02 {
			taskName("Client Player 02")
			parent(runs.client)
			workingDirectory(file("runClient02"))
			args("--username", "Player02")
		}
		server {
			taskName("Server")
			property("forge.logging.console.level", "debug")
			workingDirectory(file("runServer"))
			mods {
				create(modId) {
					source(sourceSets.main.get())
					source(sourceSets.api.get())
				}
			}
		}
	}
}

def replaceResources = tasks.register("replaceResources", Copy) {
	it.outputs.upToDateWhen { false }
	//Copy it into the build dir
	it.from(sourceSets.main.resources) {
		include("META-INF/mods.toml")
		expand(Map.of(
			"version", version,
			"mc_version", minecraft_version_range,
			"forge_version", forge_version_range,
			"loader_version", loader_version_range
		))
	}
	it.into("$buildDir/resources/main/")
}

processResources {
	duplicatesStrategy(DuplicatesStrategy.FAIL)
	exclude("META-INF/mods.toml")
	configure {
		finalizedBy(replaceResources)
	}
}

classes.configure {
	dependsOn(replaceResources)
}

javadoc {
	source = sourceSets.api.allJava
	// prevent java 8's strict doclint for javadocs from failing builds
	options.addStringOption("Xdoclint:none", "-quiet")
}

tasks {
	named("jar", Jar) {
		from(sourceSets.main.output)
		from(sourceSets.api.output)

		manifest.attributes(
			"Specification-Title"      : "${modName}",
			"Specification-Vendor"     : "${modAuthor}",
			"Specification-Version"    : "${specificationVersion}",
			"Implementation-Title"     : "${name}",
			"Implementation-Version"   : "${version}",
			"Implementation-Vendor"    : "${modAuthor}",
			"Implementation-Timestamp" : DateTimeFormatter.ISO_INSTANT.format(Instant.now()),
		)

		finalizedBy("reobfJar")
		setDescription("Creates an obfuscated JAR containing the compiled code, used by players.")
	}

	named("javadocJar", Jar) {
		dependsOn("javadoc")
		from(javadoc.destinationDir)
		duplicatesStrategy(DuplicatesStrategy.FAIL)
		archiveClassifier.set("javadoc")
		setDescription("Creates a JAR containing the API javadocs, used by developers.")
	}

	named("sourcesJar", Jar) {
		from(sourceSets.main.allJava)
		from(sourceSets.api.allJava)
		duplicatesStrategy(DuplicatesStrategy.FAIL)
		archiveClassifier.set("sources")
		setDescription("Creates a deobfuscated JAR containing the source code, used by developers.")
	}

	named("apiJar", Jar) {
		dependsOn("javadoc")
		from(sourceSets.api.output)
		// TODO: when FG bug is fixed, remove allJava from the api jar.
		// https://github.com/MinecraftForge/ForgeGradle/issues/369
		// Gradle should be able to pull them from the -sources jar.
		from(sourceSets.api.allJava)
		from(javadoc.destinationDir)
		duplicatesStrategy(DuplicatesStrategy.FAIL)
		finalizedBy("reobfJar")
		archiveClassifier.set("api")
		setDescription("Creates an obfuscated JAR containing the API source code and javadocs, used by developers.")
	}

	named("deobfJar", Jar) {
		from(sourceSets.main.output)
		from(sourceSets.api.output)
		duplicatesStrategy(DuplicatesStrategy.FAIL)
		archiveClassifier.set("deobf")
	}

	register("makeChangelog", se.bjurr.gitchangelog.plugin.gradle.GitChangelogTask) {
		fromRepo = file("$projectDir")
		file = file("changelog.html")
		untaggedName = "Current release ${project.specificationVersion}"
		fromCommit = "2fe051cf727adce1be210a46f778aa8fe031331e"
		toRef = "HEAD"
		templateContent = file("changelog.mustache").getText("UTF-8")
	}
}

artifacts {
	archives(javadocJar)
	archives(sourcesJar)
	archives(apiJar)
	archives(deobfJar)
}

reobf {
	apiJar { classpath.from(sourceSets.api.compileClasspath) }
	jar { classpath.from(sourceSets.main.compileClasspath) }
}

task reobf {
	dependsOn(reobfJar)
	dependsOn(reobfApiJar)
}

publishing {
	publications {
		register("maven", MavenPublication::class) {
			publication.artifacts = [apiJar, jar, javadocJar, deobfJar, sourcesJar]
		}
	}
	repositories {
		if (project.hasProperty("DEPLOY_DIR")) {
			maven { setUrl(project.property("DEPLOY_DIR")) }
		}
	}
}

idea {
	module {
		for (fileName in ["runClient", "runClient01", "runClient02", "runServer", "out", "logs"]) {
			excludeDirs.add(file(fileName))
		}
	}
}

repositories {
	mavenCentral()
}

spotless {
	java {
		target("src/*/java/mezz/jei/**/*.java")

		endWithNewline()
		trimTrailingWhitespace()
		removeUnusedImports()
	}
}

curseforge {
	apiKey = project.findProperty("curseforge_apikey") ?: "0"
	project {
		id = project.curseProjectId
		changelog = file("changelog.html")
		changelogType = "html"
		releaseType = "beta"
		addGameVersion("${project.minecraftVersion}")
	}
}

afterEvaluate {
	tasks {
		"curseforge${project.curseProjectId}" {
			dependsOn(tasks.makeChangelog)
		}
	}
}
