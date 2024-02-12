import com.gtnewhorizons.retrofuturagradle.mcp.DeobfuscateTask
import net.darkhax.curseforgegradle.Constants as CFG_Constants
import net.darkhax.curseforgegradle.TaskPublishCurseForge
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import se.bjurr.gitchangelog.plugin.gradle.GitChangelogTask

plugins {
	id("com.gtnewhorizons.retrofuturagradle") version("1.3.+")
	id("com.modrinth.minotaur") version("2.+")
	id("eclipse")
	id("java")
	id("maven-publish")
	id("net.darkhax.curseforgegradle") version("1.0.8")
	id("org.jetbrains.gradle.plugin.idea-ext") version "1.1.7"
	id("se.bjurr.gitchangelog.git-changelog-gradle-plugin") version("1.77.2")
}

// gradle.properties
val curseHomepageUrl: String by extra
val curseProjectId: String by extra
val jUnitVersion: String by extra
val mappingsVersion: String by extra
val minecraftVersion: String by extra
val modGroup: String by extra
val modName: String by extra
val modId: String by extra
val modJavaVersion: String by extra
val specificationVersion: String by extra

// set by ORG_GRADLE_PROJECT_modrinthToken in Jenkinsfile
val modrinthToken: String? by project

group = modGroup
val baseArchivesName = "${modId}_${minecraftVersion}"
base {
	archivesName.set(baseArchivesName)
}

// adds the build number to the end of the version string if on a build server
var buildNumber = project.findProperty("BUILD_NUMBER") ?: "9999"
version = "${specificationVersion}.${buildNumber}"

java {
	toolchain {
		languageVersion.set(JavaLanguageVersion.of(modJavaVersion))
	}
}

dependencies {
	testImplementation(
		group = "junit",
		name = "junit",
		version = jUnitVersion
	)
	testRuntimeOnly("org.junit.vintage:junit-vintage-engine:5.8.2")
}

minecraft {
	mcVersion.set(minecraftVersion)
	mcpMappingChannel.set("stable")
	mcpMappingVersion.set(mappingsVersion)

	injectedTags.set(mapOf("VERSION" to project.version))
}

tasks.withType<DeobfuscateTask> {
	accessTransformerFiles.from("${projectDir}/src/main/resources/jei_at.cfg")
}

tasks.withType<ProcessResources> {
	// this will ensure that this task is redone when the versions change.
	inputs.property("version", project.version)

	filesMatching(listOf("mcmod.info")) {
		expand(mapOf(
			"curseHomepageUrl" to curseHomepageUrl,
			"minecraftVersion" to minecraftVersion,
			"modId" to modId,
			"modJavaVersion" to modJavaVersion,
			"modName" to modName,
			"version" to project.version,
		))
	}

	// Move access transformers to META-INF
	rename("(.+_at\\.cfg)", "META-INF/$1")
}

tasks.injectTags.configure {
	outputClassName.set("java.mezz.jei.config.Tags")
}

tasks.processIdeaSettings.configure {
	dependsOn(tasks.injectTags)
}

// IDE Settings
eclipse {
	classpath {
		isDownloadSources = true
		isDownloadJavadoc = true
	}
}

idea {
	module {
		isDownloadJavadoc = true
		isDownloadSources = true
		inheritOutputDirs = true // Fix resources in IJ-Native runs
	}
}

tasks.register<GitChangelogTask>("makeChangelog") {
	fromRepo = projectDir.absolutePath.toString()
	file = file("changelog.html")
	untaggedName = "Current release ${project.version}"
	fromCommit = "2fe051cf727adce1be210a46f778aa8fe031331e"
	toRef = "HEAD"
	templateContent = file("changelog.mustache").readText()
}

tasks.register<GitChangelogTask>("makeMarkdownChangelog") {
	fromRepo = projectDir.absolutePath.toString()
	file = file("changelog.md")
	untaggedName = "Current release ${project.version}"
	fromCommit = System.getenv("GIT_PREVIOUS_SUCCESSFUL_COMMIT") ?: "HEAD~10"
	toRef = "HEAD"
	templateContent = file("changelog-markdown.mustache").readText()
}

tasks.register<TaskPublishCurseForge>("publishCurseForge") {
	dependsOn(tasks.reobfJar)
	dependsOn(":makeChangelog")

	// set by ORG_GRADLE_PROJECT_curseforge_apikey in Jenkinsfile
	apiToken = project.findProperty("curseforge_apikey") ?: "0"

	val mainFile = upload(curseProjectId, tasks.reobfJar.get().archiveFile)
	mainFile.changelogType = CFG_Constants.CHANGELOG_HTML
	mainFile.changelog = file("changelog.html")
	mainFile.releaseType = CFG_Constants.RELEASE_TYPE_BETA
	mainFile.addJavaVersion("Java $modJavaVersion")
	mainFile.addGameVersion(minecraftVersion)
	mainFile.addModLoader("Forge")

	doLast {
		project.ext.set("curse_file_url", "${curseHomepageUrl}/files/${mainFile.curseFileId}")
	}
}

modrinth {
	token.set(modrinthToken ?: "0")
	projectId.set("jei")
	versionNumber.set("${project.version}")
	versionName.set("${project.version} for Forge $minecraftVersion")
	loaders.add("forge")
	gameVersions.add(minecraftVersion)
	versionType.set("beta")
	uploadFile.set(tasks.reobfJar.get())
	changelog.set(provider { file("changelog.md").readText() })
}

tasks.modrinth {
	dependsOn(tasks.reobfJar)
	dependsOn(":makeMarkdownChangelog")
}

tasks.withType<Javadoc> {
	// workaround cast for https://github.com/gradle/gradle/issues/7038
	val standardJavadocDocletOptions = options as StandardJavadocDocletOptions
	// prevent java 8"s strict doclint for javadocs from failing builds
	standardJavadocDocletOptions.addStringOption("Xdoclint:none", "-quiet")
}

tasks.jar {
	manifest {
		attributes(mapOf("FMLAT" to "jei_at.cfg"))
	}
	from(sourceSets.main.get().output)
	from(sourceSets.api.get().output)
	duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

val sourcesJarTask = tasks.register<Jar>("sourcesJar") {
	from(sourceSets.main.get().allJava)
	from(sourceSets.api.get().allJava)

	archiveClassifier.set("sources")
	duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

val apiJarTask = tasks.register<Jar>("apiJar") {
	from(sourceSets.api.get().output)

	// Because of this FG bug, I have to include allJava in the api jar.
	// Otherwise, users of the API will not see the documentation for it.
	// https://github.com/MinecraftForge/ForgeGradle/issues/369
	// Gradle is supposed to be able to pull this info from the separate -sources jar.
	from(sourceSets.api.get().allJava)

	archiveClassifier.set("api")
	duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

val javadocJarTask = tasks.register<Jar>("javadocJar") {
	dependsOn(tasks.javadoc)
	from(tasks.javadoc.get().destinationDir)
	archiveClassifier.set("javadoc")
	duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

artifacts {
	archives(tasks.reobfJar.get())
	archives(javadocJarTask.get())
	archives(sourcesJarTask.get())
	archives(apiJarTask.get())
}

publishing {
	publications {
		register<MavenPublication>("jars") {
			artifactId = baseArchivesName
			artifact(tasks.reobfJar.get())
			artifact(sourcesJarTask.get())
			artifact(apiJarTask.get())
			artifact(javadocJarTask.get())
		}
	}
	repositories {
		val deployDir = project.findProperty("DEPLOY_DIR")
		if (deployDir != null) {
			maven(deployDir)
		}
	}
}

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
