import me.modmuss50.mpp.PublishModTask
import net.minecraftforge.gradle.common.tasks.DownloadMavenArtifact
import net.minecraftforge.gradle.common.tasks.JarExec
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
	id("java")
	id("idea")
	id("eclipse")
	id("maven-publish")
	id("net.minecraftforge.gradle")
	id("org.parchmentmc.librarian.forgegradle")
	id("me.modmuss50.mod-publish-plugin")
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

dependencyProjects.forEach {
	project.evaluationDependsOn(it.path)
}
project.evaluationDependsOn(":Changelog")

java {
	toolchain {
		languageVersion.set(JavaLanguageVersion.of(modJavaVersion))
	}
	withSourcesJar()
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

minecraft {
	mappings("parchment", parchmentVersionForge)

	copyIdeResources.set(true)

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

tasks.jar {
	from(sourceSets.main.get().output)
	for (p in dependencyProjects) {
		from(p.sourceSets.main.get().output)
	}

	duplicatesStrategy = DuplicatesStrategy.EXCLUDE
	finalizedBy("reobfJar")
}

val sourcesJarTask = tasks.named<Jar>("sourcesJar") {
	from(sourceSets.main.get().allJava)
	for (p in dependencyProjects) {
		from(p.sourceSets.main.get().allJava)
	}
	duplicatesStrategy = DuplicatesStrategy.EXCLUDE
	archiveClassifier.set("sources")
}

publishMods {
	file.set(tasks.jar.get().archiveFile)
	changelog.set(provider { file("../Changelog/changelog.md").readText() })
	type = BETA
	modLoaders.add("forge")
	displayName.set("${project.version} for Forge $minecraftVersion")
	version.set(project.version.toString())

	curseforge {
		projectId = curseProjectId
		accessToken.set(curseforgeApikey ?: "0")
		changelog.set(provider { file("../Changelog/changelog.html").readText() })
		changelogType = "html"
		minecraftVersionRange {
			start = minecraftVersionRangeStart
			end = minecraftVersion
		}
		javaVersions.add(JavaVersion.toVersion(modJavaVersion))
	}

	modrinth {
		projectId = modrinthId
		accessToken = modrinthToken
		minecraftVersionRange {
			start = minecraftVersionRangeStart
			end = minecraftVersion
		}
	}
}
tasks.withType<PublishModTask> {
	dependsOn(tasks.jar, ":Changelog:makeChangelog", ":Changelog:makeMarkdownChangelog")
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

artifacts {
	archives(tasks.jar.get())
	archives(sourcesJarTask.get())
}

publishing {
	publications {
		register<MavenPublication>("forgeJar") {
			artifactId = baseArchivesName
			artifact(tasks.jar.get())
			artifact(sourcesJarTask.get())

			pom.withXml {
				val dependenciesNode = asNode().appendNode("dependencies")
				dependencyProjects.forEach {
					val dependencyNode = dependenciesNode.appendNode("dependency")
					dependencyNode.appendNode("groupId", it.group)
					dependencyNode.appendNode("artifactId", it.base.archivesName.get())
					dependencyNode.appendNode("version", it.version)
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

idea {
	module {
		for (fileName in listOf("run", "out", "logs")) {
			excludeDirs.add(file(fileName))
		}
	}
}

tasks.withType<DownloadMavenArtifact> {
	notCompatibleWithConfigurationCache("uses Task.project at execution time")
}

tasks.withType<JarExec> {
	notCompatibleWithConfigurationCache("uses external process at execution time")
}
