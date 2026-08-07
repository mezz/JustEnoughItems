plugins {
	id("idea")
	id("java")
	id("maven-publish")
	id("net.neoforged.moddev")
}

// gradle.properties
val neoforgeVersion = providers.gradleProperty("neoforgeVersion").get()
val minecraftVersion = providers.gradleProperty("minecraftVersion").get()
val modGroup = providers.gradleProperty("modGroup").get()
val modId = providers.gradleProperty("modId").get()
val modJavaVersion = providers.gradleProperty("modJavaVersion").get()

val baseArchivesName = "${modId}-${minecraftVersion}-neoforge-api"
base {
	archivesName.set(baseArchivesName)
}

val dependencyProjectPaths = listOf(":CommonApi")

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
	dependencyProjectPaths.forEach {
		implementation(project(it))
	}
}

neoForge {
	version = neoforgeVersion
	// We don't need the AT, but this allows MDG to share the recompiled Minecraft artifacts with the NeoForge project.
	setAccessTransformers("../NeoForge/src/main/resources/META-INF/accesstransformer.cfg")
}

val sourcesJar = tasks.named<Jar>("sourcesJar")

tasks.assemble {
	dependsOn(sourcesJar)
}

publishing {
	publications {
		register<MavenPublication>("neoforgeApi") {
			artifactId = baseArchivesName
			artifact(tasks.jar)
			artifact(sourcesJar)

			val dependencyInfos = listOf("common-api").map {
				mapOf(
					"groupId" to modGroup,
					"artifactId" to "${modId}-${minecraftVersion}-$it",
					"version" to project.version
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

idea {
	module {
		for (fileName in listOf("build", "run", "out", "logs")) {
			excludeDirs.add(file(fileName))
		}
	}
}
