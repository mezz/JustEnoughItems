plugins {
	id("java")
	id("idea")
	id("eclipse")
	id("maven-publish")
	id("net.minecraftforge.accesstransformers")
	id("net.minecraftforge.gradle")
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

minecraft.mavenizer(repositories)
repositories {
	// Mojang provides patched LWJGL natives that are absent from Maven Central.
	maven("https://libraries.minecraft.net")
	mavenCentral()
	maven("https://maven.minecraftforge.net")
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
	// Keep the API's Parchment metadata separate from Forge's official-mappings run metadata.
	implementation(minecraft.dependency("forgeApi", "net.minecraftforge:forge:${minecraftVersion}-${forgeVersion}"))
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
	accessTransformers.from(file("../Forge/src/main/resources/META-INF/accesstransformer.cfg"))
}

val sourcesJar = tasks.named<Jar>("sourcesJar")

tasks.assemble {
	dependsOn(sourcesJar)
}

publishing {
	publications {
		register<MavenPublication>("forgeApi") {
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

idea {
	module {
		for (fileName in listOf("build", "run", "out", "logs")) {
			excludeDirs.add(file(fileName))
		}
	}
}
