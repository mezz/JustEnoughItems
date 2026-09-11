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

repositories {
	maven("https://maven.blamejared.com/") {
		content { includeGroup("net.mezzdev.config") }
	}
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
val mezzConfigCurseForgeProjectSlug: String by extra
val mezzConfigModrinthProjectId: String by extra
val mezzConfigGuiCurseForgeProjectSlug: String by extra
val mezzConfigGuiModrinthProjectId: String by extra
val bakedSubstringIndexVersion: String by extra
val suffixtreeVersion: String by extra
val deduplicatingRunnerVersion: String by extra
val mezzConfigVersion: String by extra
val mezzConfigVersionRange: String by extra
val mezzConfigApiDependency: String by rootProject.extra
val mezzConfigForgeDependency: String by rootProject.extra
val mezzConfigGuiApiDependency: String by rootProject.extra
val mezzConfigGuiForgeDependency: String by rootProject.extra

val forgeArtifactVersion = "${minecraftVersion}-${forgeVersion}"

// set by ORG_GRADLE_PROJECT_modrinthToken in Jenkinsfile
val modrinthToken: String? by project
// set by ORG_GRADLE_PROJECT_curseforgeApikey in Jenkinsfile
val curseforgeApikey: String? by project

val baseArchivesName = "${modId}-${minecraftVersion}-forge"
base {
	archivesName.set(baseArchivesName)
}

val gameTestSourceSet = sourceSets.create("gameTest") {
	compileClasspath += sourceSets.main.get().output
	runtimeClasspath += sourceSets.main.get().output
}

sourceSets {
	named("test") {
		resources {
			//The test module has no resources
			setSrcDirs(emptyList<String>())
		}
	}
}

configurations.named(gameTestSourceSet.implementationConfigurationName) {
	extendsFrom(configurations.implementation.get())
}

val dependencyProjects: List<Project> = listOf(
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

java {
	toolchain {
		languageVersion.set(JavaLanguageVersion.of(modJavaVersion))
	}
	withSourcesJar()
}

val changelogHtml = configurations.create("changelogHtml") {
	isCanBeConsumed = false
	isCanBeResolved = true
	isVisible = false
	attributes {
		attribute(Usage.USAGE_ATTRIBUTE, objects.named<Usage>("changelogHtml"))
	}
}

val changelogMarkdown = configurations.create("changelogMarkdown") {
	isCanBeConsumed = false
	isCanBeResolved = true
	isVisible = false
	attributes {
		attribute(Usage.USAGE_ATTRIBUTE, objects.named<Usage>("changelogMarkdown"))
	}
}

fun Configuration.singleFileContents(): Provider<String> =
	incoming
		.files
		.elements
		.map { elements -> elements.single() }
		.map { it.asFile.readText() }

dependencies {
	compileOnly(mezzConfigApiDependency)
	modRuntimeOnly(mezzConfigForgeDependency)
	jarJar(mezzConfigForgeDependency) {
		isTransitive = false
		version {
			strictly(mezzConfigVersionRange)
			prefer(mezzConfigVersion)
		}
	}
	compileOnly(mezzConfigGuiApiDependency)
	modRuntimeOnly(mezzConfigGuiForgeDependency)
	dependencyProjects.forEach {
		compileOnly(it)
		testImplementation(it)
		add(gameTestSourceSet.implementationConfigurationName, it)
	}
	modShadeImplementation("net.mezzdev:baked-substring-index:${bakedSubstringIndexVersion}") {
		isTransitive = false
	}
	modShadeImplementation("net.mezzdev:deduplicating-runner:${deduplicatingRunnerVersion}") {
		isTransitive = false
	}
	modShadeImplementation("net.mezzdev:suffixtree:${suffixtreeVersion}") {
		isTransitive = false
	}
	changelogHtml(project(":Changelog"))
	changelogMarkdown(project(":Changelog"))
	testImplementation(mezzConfigApiDependency)
	testImplementation(
		group = "org.junit.jupiter",
		name = "junit-jupiter",
		version = jUnitVersion
	)
	testRuntimeOnly(
		group = "org.junit.platform",
		name = "junit-platform-launcher"
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
		setEnabledSourceSets(setOf(sourceSets.main.get(), sourceSets.test.get(), gameTestSourceSet))
		// The default CI binary path keeps invalid Forge jar signatures that break unit tests.
		setDisableRecompilation(false)
	}

	mods {
		create(modId) {
			sourceSet(sourceSets.main.get())
			sourceSet(gameTestSourceSet)
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
		create("gameTestServer") {
			type.set("gameTestServer")
			systemProperty("forge.enabledGameTestNamespaces", modId)
			gameDirectory = file("run/gameTestServer-$minecraftVersion")
			logLevel = Level.INFO
		}
	}
}

val copyGameTestStructures = tasks.register<Copy>("copyGameTestStructures") {
	from(layout.projectDirectory.dir("src/gameTest/resources/gameteststructures"))
	into(layout.projectDirectory.dir("run/gameTestServer-$minecraftVersion/gameteststructures"))
}

tasks.named("runGameTestServer") {
	dependsOn(copyGameTestStructures)
}

tasks.check {
	dependsOn("runGameTestServer")
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
	changelog.set(changelogMarkdown.singleFileContents())
	type = BETA
	modLoaders.add("forge")
	displayName.set("${project.version} for Forge $minecraftVersion")
	version.set(project.version.toString())

	curseforge {
		projectId = curseProjectId
		projectSlug = curseHomepageUrl.substringAfterLast("/")
		accessToken.set(curseforgeApikey ?: "0")
		requires(mezzConfigCurseForgeProjectSlug)
		optional(mezzConfigGuiCurseForgeProjectSlug)
		changelog.set(changelogHtml.singleFileContents())
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
		requires(mezzConfigModrinthProjectId)
		optional(mezzConfigGuiModrinthProjectId)
		minecraftVersionRange {
			start = minecraftVersionRangeStart
			end = minecraftVersion
		}
		dryRun = modrinthToken == null
	}
}

tasks.named<Test>("test") {
	useJUnitPlatform()
	include("mezz/jei/gui/config/**")
	include("mezz/jei/gui/input/focus/**")
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
			artifact(shadedJar)
			artifact(shadedSourcesJar)

			val dependencyInfos = listOf(
				dependencyInfo(mezzConfigGuiForgeDependency) + ("optional" to "true")
			) + dependencyProjects.map {
				mapOf(
					"groupId" to it.group,
					"artifactId" to it.base.archivesName.get(),
					"version" to it.version
				)
			}

			pom.withXml {
				val dependenciesNode = asNode().appendNode("dependencies")
				dependencyInfos.forEach {
					val dependencyNode = dependenciesNode.appendNode("dependency")
					it.forEach { (key, value) ->
						dependencyNode.appendNode(key, value)
					}
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

fun dependencyInfo(notation: String): Map<String, String> {
	val (groupId, artifactId, version) = notation.split(":")
	return mapOf(
		"groupId" to groupId,
		"artifactId" to artifactId,
		"version" to version
	)
}

idea {
	module {
		for (fileName in listOf("build", "run", "out", "logs")) {
			excludeDirs.add(file(fileName))
		}
	}
}
