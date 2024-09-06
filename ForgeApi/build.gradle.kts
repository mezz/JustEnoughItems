import net.minecraftforge.gradle.common.tasks.DownloadMavenArtifact

plugins {
	id("java")
	id("idea")
	id("eclipse")
	id("maven-publish")
	id("net.minecraftforge.gradle")
	id("org.parchmentmc.librarian.forgegradle")
}

// gradle.properties
val forgeVersion: String by extra
val minecraftVersion: String by extra
val modGroup: String by extra
val modId: String by extra
val modJavaVersion: String by extra
val parchmentVersionForge: String by extra

val baseArchivesName = "${modId}-${minecraftVersion}-forge-api"
base {
	archivesName.set(baseArchivesName)
}

val dependencyProjects: List<Project> = listOf(
	project(":CommonApi"),
)

dependencyProjects.forEach {
	project.evaluationDependsOn(it.path)
}

// Hack fix: FG can't resolve deps like lwjgl-freetype-3.3.3-natives-macos-patch.jar without this
repositories {
	maven("https://libraries.minecraft.net")
}

sourceSets {
	named("main") {
		resources {
			//The API has no resources
			setSrcDirs(emptyList<String>())
		}
	}
	named("test") {
		resources {
			//The test module has no resources
			setSrcDirs(emptyList<String>())
		}
	}
}

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

	// Hack fix for now, force jopt-simple to be exactly 5.0.4 because Mojang ships that version, but some transitive dependencies request 6.0+
	implementation("net.sf.jopt-simple:jopt-simple:5.0.4") {
		version {
			strictly("5.0.4")
		}
	}
}

minecraft {
	mappings("parchment", parchmentVersionForge)

	// we now use Official mappings at runtime
	reobf = false

	copyIdeResources.set(true)

	// All minecraft configurations in the multi-project must be identical, including ATs,
	// because of a ForgeGradle bug https://github.com/MinecraftForge/ForgeGradle/issues/844
	accessTransformer(file("../Forge/src/main/resources/META-INF/accesstransformer.cfg"))

	// no runs are configured for API
}

val sourcesJar = tasks.named<Jar>("sourcesJar")

artifacts {
	archives(tasks.jar.get())
	archives(sourcesJar.get())
}

publishing {
	publications {
		register<MavenPublication>("forgeApi") {
			artifactId = baseArchivesName
			artifact(tasks.jar)
			artifact(sourcesJar)

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

tasks.withType<DownloadMavenArtifact> {
	notCompatibleWithConfigurationCache("uses Task.project at execution time")
}
