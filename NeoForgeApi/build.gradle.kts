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
	// We don't need the AT, but this allows MDG to share the recompiled Minecraft artifacts with the NeoForge project.
	setAccessTransformers("../NeoForge/src/main/resources/META-INF/accesstransformer.cfg")
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

			val dependencyInfos = dependencyProjects.map {
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
