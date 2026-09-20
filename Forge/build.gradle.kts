import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
	id("java")
	id("idea")
	id("eclipse")
	id("maven-publish")
	id("net.minecraftforge.accesstransformers")
	id("net.minecraftforge.gradle")
	id("net.minecraftforge.jarjar")
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

val debugSourceSet = debugProject.sourceSets.main.get()
val forgeDebugOutput = layout.buildDirectory.dir("sourceSets/forgeDebug")
val prepareForgeDebug = tasks.register<Sync>("prepareForgeDebug") {
	from(debugSourceSet.output)
	into(forgeDebugOutput)
	dependsOn(debugProject.tasks.named(debugSourceSet.classesTaskName))
	duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
val forgeDebugSourceSet = sourceSets.create("forgeDebug") {
	java.setSrcDirs(emptyList<String>())
	resources.setSrcDirs(emptyList<String>())
	output.setResourcesDir(forgeDebugOutput)
	(output.classesDirs as ConfigurableFileCollection).setFrom(forgeDebugOutput)
}
tasks.named(forgeDebugSourceSet.compileJavaTaskName) {
	enabled = false
}
tasks.named(forgeDebugSourceSet.processResourcesTaskName) {
	enabled = false
}
tasks.named(forgeDebugSourceSet.classesTaskName) {
	dependsOn(prepareForgeDebug)
}

val mezzConfigLocalRuntime by configurations.creating {
	isCanBeConsumed = false
	isCanBeResolved = false
}
configurations.runtimeClasspath {
	extendsFrom(mezzConfigLocalRuntime)
}

jarJar.register()

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

minecraft.mavenizer(repositories)
repositories {
	// Mojang provides patched LWJGL natives that are absent from Maven Central.
	maven("https://libraries.minecraft.net")
	mavenCentral()
	maven("https://maven.minecraftforge.net")
}

dependencies {
	val forgeDependency = create("net.minecraftforge:forge:${minecraftVersion}-${forgeVersion}") as ExternalModuleDependency
	// ForgeApi also generates Parchment variants; pin the development runtime to official mappings.
	forgeDependency.attributes {
		attribute(Attribute.of("net.minecraftforge.mappings.channel", String::class.java), "official")
		attribute(Attribute.of("net.minecraftforge.mappings.version", String::class.java), minecraftVersion)
	}
	implementation(minecraft.dependency(forgeDependency))
	compileOnly(mezzConfigApiDependency)
	mezzConfigLocalRuntime(mezzConfigForgeDependency)
	"jarJar"(mezzConfigForgeDependency) {
		isTransitive = false
		jarJar.configure(this) {
			setRange(mezzConfigVersionRange)
			setVersion(mezzConfigVersion)
		}
	}
	compileOnly(mezzConfigGuiApiDependency)
	mezzConfigLocalRuntime(mezzConfigGuiForgeDependency)
	dependencyProjects.forEach {
		compileOnly(it)
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
	testImplementation(
		group = "org.junit.jupiter",
		name = "junit-jupiter",
		version = jUnitVersion
	)
	testRuntimeOnly(
		group = "org.junit.platform",
		name = "junit-platform-launcher"
	)

	// Hack fix for now, force jopt-simple to be exactly 5.0.4 because Mojang ships that version, but some transitive dependencies request 6.0+
	implementation("net.sf.jopt-simple:jopt-simple:5.0.4") {
		version {
			strictly("5.0.4")
		}
	}
	changelogHtml(project(":Changelog"))
	changelogMarkdown(project(":Changelog"))
}

val modShadeClasspath = configurations.named("modShadeClasspath")

// ForgeGradle 7 customizes the standard client run per source set.
val playerSourceSets = listOf("Player01", "Player02").associateWith { playerName ->
	sourceSets.create(playerName.replaceFirstChar(Char::lowercaseChar)) {
		java.setSrcDirs(emptyList<String>())
		resources.setSrcDirs(emptyList<String>())
		runtimeClasspath = sourceSets.main.get().runtimeClasspath
		configurations.named(implementationConfigurationName) {
			extendsFrom(configurations.implementation.get())
		}
	}
}

minecraft {
	mappings("official", minecraftVersion)
	accessTransformers.from(file("src/main/resources/META-INF/accesstransformer.cfg"))

	runs {
		configureEach {
			systemProperty("forge.logging.console.level", "debug")
			extraLibraries(modShadeClasspath.get())
			mods {
				create(modId) {
					source(sourceSets.main.get())
				}
				create("${modId}debug") {
					source(forgeDebugSourceSet)
				}
			}
		}
		create("client") {
			workingDir.set(layout.projectDirectory.dir("run/client/Dev"))
			if (providers.systemProperty("os.name").get().startsWith("Mac")) {
				jvmArgs("-XstartOnFirstThread")
			}
			playerSourceSets.forEach { (playerName, playerSourceSet) ->
				with(playerSourceSet) {
					workingDir.set(layout.projectDirectory.dir("run/client/$playerName"))
					args("--username", playerName)
				}
			}
		}
		create("server") {
			workingDir.set(layout.projectDirectory.dir("run/server"))
			args("nogui")
		}
	}
}

tasks.register("runClientDev") {
	group = "Slime Launcher"
	dependsOn("runClient")
}
playerSourceSets.forEach { (playerName, playerSourceSet) ->
	tasks.register("runClient$playerName") {
		group = "Slime Launcher"
		dependsOn(playerSourceSet.getTaskName("run", "client"))
	}
}
tasks.withType<JavaExec>().configureEach {
	javaLauncher.set(javaToolchains.launcherFor {
		languageVersion.set(JavaLanguageVersion.of(modJavaVersion))
	})
	classpath(forgeDebugSourceSet.output)
}

tasks.named<JavaCompile>(sourceSets.main.get().compileJavaTaskName) {
	dependencyProjects.forEach {
		source(it.sourceSets.main.get().allSource)
	}
}

tasks.named<ProcessResources>(sourceSets.main.get().processResourcesTaskName) {
	dependencyProjects.forEach {
		from(it.sourceSets.main.get().resources)
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

tasks.test {
	useJUnitPlatform()
	include("mezz/jei/test/**")
	exclude("mezz/jei/test/lib/**")
	// Package annotations alone do not constitute a test suite in Gradle 9.
	exclude("**/package-info.class")
	outputs.upToDateWhen { false }
	testLogging {
		events = setOf(TestLogEvent.FAILED)
		exceptionFormat = TestExceptionFormat.FULL
	}
}

tasks.assemble {
	dependsOn(sourcesJarTask)
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
