import com.gtnewhorizons.retrofuturagradle.mcp.DeobfuscateTask
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import se.bjurr.gitchangelog.plugin.gradle.GitChangelogTask
import java.io.ByteArrayOutputStream

plugins {
	id("com.gtnewhorizons.retrofuturagradle") version("1.4.9")
	id("eclipse")
	id("java")
	id("me.modmuss50.mod-publish-plugin") version("2.0.1")
	id("maven-publish")
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
val modrinthId: String by extra
val specificationVersion: String by extra

// set by ORG_GRADLE_PROJECT_curseforgeApikey or ORG_GRADLE_PROJECT_curseforge_apikey in Jenkinsfile
val curseforgeApikey = providers.gradleProperty("curseforgeApikey")
	.orElse(providers.gradleProperty("curseforge_apikey"))
// set by ORG_GRADLE_PROJECT_modrinthToken in Jenkinsfile
val modrinthToken = providers.gradleProperty("modrinthToken")

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
	testImplementation(
		group = "org.mockito",
		name = "mockito-core",
		version = "3.12.4"
	)
	testRuntimeOnly("org.lwjgl.lwjgl:lwjgl:2.9.4-nightly-20150209")
	testRuntimeOnly("org.junit.vintage:junit-vintage-engine:5.8.2")
}

val macOsxX64Lwjgl2Natives = configurations.create("macOsxX64Lwjgl2Natives") {
	isCanBeConsumed = false
	isCanBeResolved = true
	isTransitive = false
}

dependencies {
	add(macOsxX64Lwjgl2Natives.name, "org.lwjgl.lwjgl:lwjgl-platform:2.9.4-nightly-20150209:natives-osx")
	add(macOsxX64Lwjgl2Natives.name, "net.java.jinput:jinput-platform:2.0.5:natives-osx")
}

val extractMacOsxX64Lwjgl2Natives = tasks.register<Copy>("extractMacOsxX64Lwjgl2Natives") {
	group = "Internal Vanilla Minecraft"
	description = "Replaces LWJGL 2 natives with x86_64 macOS natives when runClient uses an x86_64 Java launcher."
	from({ macOsxX64Lwjgl2Natives.map { zipTree(it) } })
	into(layout.projectDirectory.dir("run/natives/lwjgl2"))
	onlyIf("runClient uses an x86_64 Java launcher on macOS") {
		val isMacOs = System.getProperty("os.name").startsWith("Mac")
		val javaExec = tasks.named<JavaExec>("runClient").get()
		val javaSettings = ByteArrayOutputStream()
		exec {
			commandLine(javaExec.javaLauncher.get().executablePath.asFile, "-XshowSettings:properties", "-version")
			standardOutput = javaSettings
			errorOutput = javaSettings
			isIgnoreExitValue = true
		}
		isMacOs && javaSettings.toString().contains("os.arch = x86_64")
	}
}

tasks.named("extractNatives2") {
	finalizedBy(extractMacOsxX64Lwjgl2Natives)
}

tasks.named("runClient") {
	dependsOn(extractMacOsxX64Lwjgl2Natives)
}

extractMacOsxX64Lwjgl2Natives.configure {
	mustRunAfter(tasks.named("extractNatives2"))
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

publishMods {
	file.set(tasks.reobfJar.flatMap { it.archiveFile })
	type = BETA
	modLoaders.add("forge")
	displayName.set("${project.version} for Forge $minecraftVersion")
	version.set(project.version.toString())
	dryRun.set(providers.provider {
		!curseforgeApikey.isPresent && !modrinthToken.isPresent
	})

	curseforge {
		projectId = curseProjectId
		accessToken.set(curseforgeApikey)
		changelog.set(provider { file("changelog.html").readText() })
		changelogType = "html"
		minecraftVersions.add(minecraftVersion)
		javaVersions.add(JavaVersion.toVersion(modJavaVersion))
		client = true
		server = true
	}

	modrinth {
		projectId = modrinthId
		accessToken.set(modrinthToken)
		changelog.set(provider { file("changelog.md").readText() })
		minecraftVersions.add(minecraftVersion)
	}
}

tasks.named("publishCurseforge") {
	dependsOn(tasks.reobfJar)
	dependsOn(":makeChangelog")
}

tasks.named("publishModrinth") {
	dependsOn(tasks.reobfJar)
	dependsOn(":makeMarkdownChangelog")
}

tasks.register("publishCurseForge") {
	group = "publishing"
	description = "Compatibility alias for publishCurseforge."
	dependsOn(tasks.named("publishCurseforge"))
}

tasks.register("modrinth") {
	group = "publishing"
	description = "Compatibility alias for publishModrinth."
	dependsOn(tasks.named("publishModrinth"))
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
	include("mezz/jei/config/**")
	include("mezz/jei/test/**")
	include("mezz/jei/gui/recipes/**")
	exclude("mezz/jei/test/lib/**")
	outputs.upToDateWhen { false }
	testLogging {
		events = setOf(TestLogEvent.FAILED)
		exceptionFormat = TestExceptionFormat.FULL
	}
}
