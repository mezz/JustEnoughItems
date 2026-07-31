import org.slf4j.event.Level

plugins {
	java
	idea
	eclipse
	`maven-publish`
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
val modGroup: String by extra
val modId: String by extra
val modJavaVersion: String by extra
val lwjglVersionMacArm64: String by extra
val bakedSubstringIndexVersion: String by extra
val suffixtreeVersion: String by extra
val parchmentVersionForge: String by extra

val isAppleSilicon = System.getProperty("os.name").startsWith("Mac") &&
	System.getProperty("os.arch") in setOf("aarch64", "arm64")

if (isAppleSilicon) {
	configurations.configureEach {
		resolutionStrategy.eachDependency {
			if (requested.group == "org.lwjgl") {
				useVersion(lwjglVersionMacArm64)
				because("Minecraft 1.18's LWJGL 3.2.1 has no Apple Silicon natives")
			}
		}
	}
}

val forgeArtifactVersion = "${minecraftVersion}-${forgeVersion}"
val parchmentMinecraftVersion = minecraftVersion

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
project.evaluationDependsOn(":Changelog")

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

fun Configuration.singleFileContents(): Provider<String> =
	incoming
		.files
		.elements
		.map { elements -> elements.single() }
		.map { it.asFile.readText() }

val appleSiliconLwjglNatives = rootProject.configurations.named("appleSiliconLwjglNatives")
val appleSiliconLwjglNativeDirectory = layout.buildDirectory.dir("lwjglNatives/macosArm64")
val extractAppleSiliconLwjglNatives = tasks.register<Sync>("extractAppleSiliconLwjglNatives") {
	from({ appleSiliconLwjglNatives.get().map { zipTree(it) } })
	include("**/*.dylib")
	eachFile {
		path = name
	}
	includeEmptyDirs = false
	duplicatesStrategy = DuplicatesStrategy.EXCLUDE
	into(appleSiliconLwjglNativeDirectory)
}

dependencies {
	dependencyProjects.forEach {
		compileOnly(it)
		testImplementation(it)
		add(gameTestSourceSet.implementationConfigurationName, it)
	}
	changelogHtml(project(":Changelog"))
	modShadeImplementation("net.mezzdev:baked-substring-index:${bakedSubstringIndexVersion}") {
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
		name = "junit-platform-launcher",
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
		setEnabledSourceSets(setOf(sourceSets.main.get(), sourceSets.test.get(), gameTestSourceSet))
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
			if (isAppleSilicon) {
				taskBefore(extractAppleSiliconLwjglNatives)
				systemProperty("org.lwjgl.librarypath", appleSiliconLwjglNativeDirectory.get().asFile.absolutePath)
			}
			systemProperty("forge.logging.console.level", "debug")
			gameDirectory = file("run/client/Dev")
			logLevel = Level.DEBUG
		}
		create("clientPlayer01") {
			client()
			if (isAppleSilicon) {
				taskBefore(extractAppleSiliconLwjglNatives)
				systemProperty("org.lwjgl.librarypath", appleSiliconLwjglNativeDirectory.get().asFile.absolutePath)
			}
			systemProperty("forge.logging.console.level", "debug")
			gameDirectory = file("run/client/Player01")
			programArguments.addAll("--username", "Player01")
			logLevel = Level.DEBUG
		}
		create("clientPlayer02") {
			client()
			if (isAppleSilicon) {
				taskBefore(extractAppleSiliconLwjglNatives)
				systemProperty("org.lwjgl.librarypath", appleSiliconLwjglNativeDirectory.get().asFile.absolutePath)
			}
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

val shadedJar = modShade.shadeJar()
val shadedSourcesJar = modShade.shadeSourcesJar()
val reobfJarTask = tasks.named<AbstractArchiveTask>("reobfJar")
publishMods {
	file.set(shadedJar.flatMap { it.archiveFile })
	changelog.set(changelogHtml.singleFileContents())
	type = BETA
	modLoaders.add("forge")
	displayName.set("${project.version} for Forge $minecraftVersion")
	version.set(project.version.toString())

	curseforge {
		projectId = curseProjectId
		projectSlug = curseHomepageUrl.substringAfterLast("/")
		accessToken.set((project.findProperty("curseforge_apikey") as String?) ?: "0")
		changelog.set(changelogHtml.singleFileContents())
		changelogType = "html"
		minecraftVersionRange {
			start = minecraftVersion
			end = minecraftVersion
		}
		javaVersions.add(JavaVersion.toVersion(modJavaVersion))
		client = true
		server = true
		dryRun = project.findProperty("curseforge_apikey") == null
	}
}
tasks.register("publishCurseForge") {
	dependsOn(tasks.named("publishCurseforge"))
}

tasks.named<Test>("test") {
	useJUnitPlatform()
	include("mezz/jei/gui/config/**")
	include("mezz/jei/gui/input/focus/**")
	include("mezz/jei/test/**")
	exclude("mezz/jei/test/lib/**")
	outputs.upToDateWhen { false }
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
		for (fileName in listOf("build", "run", "out", "logs")) {
			excludeDirs.add(file(fileName))
		}
	}
}
