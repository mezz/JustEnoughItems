import me.modmuss50.mpp.PublishModTask
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.slf4j.event.Level

plugins {
	id("java")
	id("idea")
	id("eclipse")
	id("maven-publish")
	id("net.neoforged.moddev.legacyforge")
	id("me.modmuss50.mod-publish-plugin")
	id("net.mezzdev.modshade")
}

// gradle.properties
val curseHomepageUrl: String by extra
val curseProjectId: String by extra
val forgeVersion: String by extra
val jUnitVersion: String by extra
val minecraftVersion: String by extra
val minecraftVersionRangeStart: String by extra
val modGroup: String by extra
val modId: String by extra
val modJavaVersion: String by extra
val parchmentMinecraftVersion: String by extra
val parchmentVersionForge: String by extra
val modrinthId: String by extra
val bakedSubstringIndexVersion: String by extra

val forgeArtifactVersion = "${minecraftVersion}-${forgeVersion}"

// set by ORG_GRADLE_PROJECT_modrinthToken in Jenkinsfile
val modrinthToken: String? by project
// set by ORG_GRADLE_PROJECT_curseforgeApikey in Jenkinsfile
val curseforgeApikey: String? by project

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
val debugProject = project(":Debug")

dependencyProjects.forEach {
	project.evaluationDependsOn(it.path)
}
project.evaluationDependsOn(debugProject.path)
project.evaluationDependsOn(":Changelog")

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
	modShadeImplementation("net.mezzdev:baked-substring-index:${bakedSubstringIndexVersion}") {
		isTransitive = false
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

legacyForge {
	validateAccessTransformers = true
	setAccessTransformers("src/main/resources/META-INF/accesstransformer.cfg")

	parchment {
		minecraftVersion = parchmentMinecraftVersion
		mappingsVersion = parchmentVersionForge.removeSuffix("-$parchmentMinecraftVersion")
	}

	enable {
		setForgeVersion(forgeArtifactVersion)
		setEnabledSourceSets(setOf(sourceSets.main.get(), sourceSets.test.get()))
		// The default CI binary path keeps invalid Forge jar signatures that break unit tests.
		setDisableRecompilation(false)
	}

	mods {
		create(modId) {
			sourceSet(sourceSets.main.get())
			for (p in dependencyProjects) {
				sourceSet(p.sourceSets.main.get())
			}
		}
		create("${modId}debug") {
			sourceSet(debugProject.sourceSets.main.get())
		}
	}

	runs {
		create("clientDev") {
			client()
			systemProperty("forge.logging.console.level", "debug")
			gameDirectory = file("run/client/Dev")
			logLevel = Level.DEBUG
		}
		create("clientPlayer01") {
			client()
			systemProperty("forge.logging.console.level", "debug")
			gameDirectory = file("run/client/Player01")
			programArguments.addAll("--username", "Player01")
			logLevel = Level.DEBUG
		}
		create("clientPlayer02") {
			client()
			systemProperty("forge.logging.console.level", "debug")
			gameDirectory = file("run/client/Player02")
			programArguments.addAll("--username", "Player02")
			logLevel = Level.DEBUG
		}
		create("server") {
			server()
			systemProperty("forge.logging.console.level", "debug")
			gameDirectory = file("run/server")
			programArguments.add("nogui")
			logLevel = Level.DEBUG
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

val reobfJarTask = tasks.named<AbstractArchiveTask>("reobfJar")

val shadedJar = modShade.shadeJar()
val shadedSourcesJar = modShade.shadeSourcesJar()

publishMods {
	file.set(shadedJar.flatMap { it.archiveFile })
	changelog.set(provider { file("../Changelog/changelog.md").readText() })
	type = BETA
	modLoaders.add("forge")
	displayName.set("${project.version} for Forge $minecraftVersion")
	version.set(project.version.toString())

	curseforge {
		projectId = curseProjectId
		projectSlug = curseHomepageUrl.substringAfterLast("/")
		accessToken.set(curseforgeApikey ?: "0")
		changelog.set(provider { file("../Changelog/changelog.html").readText() })
		changelogType = "html"
		minecraftVersionRange {
			start = minecraftVersionRangeStart
			end = minecraftVersion
		}
		javaVersions.add(JavaVersion.toVersion(modJavaVersion))
		client = true
		server = true
		dryRun = curseforgeApikey == null
	}

	modrinth {
		projectId = modrinthId
		accessToken = modrinthToken
		minecraftVersionRange {
			start = minecraftVersionRangeStart
			end = minecraftVersion
		}
		dryRun = modrinthToken == null
	}
}
tasks.withType<PublishModTask> {
	dependsOn(tasks.jar, ":Changelog:makeChangelog", ":Changelog:makeMarkdownChangelog")
}

tasks.named<Test>("test") {
	useJUnitPlatform()
	include("mezz/jei/gui/config/**")
	include("mezz/jei/test/**")
	exclude("mezz/jei/test/lib/**")
	outputs.upToDateWhen { false }
	testLogging {
		events = setOf(TestLogEvent.FAILED)
		exceptionFormat = TestExceptionFormat.FULL
	}
}

artifacts {
	archives(reobfJarTask)
	archives(sourcesJarTask.get())
}

publishing {
	publications {
		register<MavenPublication>("forgeJar") {
			artifactId = baseArchivesName
			from(components["modShade"])
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
