plugins {
	id("java")
	id("maven-publish")
	id("net.neoforged.moddev")
}

// gradle.properties
val neoforgeVersion: String by extra
val minecraftVersion: String by extra
val modGroup: String by extra
val modId: String by extra
val modJavaVersion: String by extra

val baseArchivesName = "${modId}-${minecraftVersion}-neoforge-api"
base {
	archivesName.set(baseArchivesName)
}

val dependencyProjects: List<Project> = listOf(
	project(":CommonApi"),
)

dependencyProjects.forEach {
	project.evaluationDependsOn(it.path)
}

sourceSets {
	main {
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
	dependencyProjects.forEach {
		implementation(it)
	}
}

neoForge {
	version = neoforgeVersion
	accessTransformers.from(file("../NeoForge/src/main/resources/META-INF/accesstransformer.cfg"))
}

val sourcesJar = tasks.named<Jar>("sourcesJar")

artifacts {
	archives(tasks.jar.get())
	archives(sourcesJar.get())
}

publishing {
	publications {
		register<MavenPublication>("neoforgeApi") {
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
