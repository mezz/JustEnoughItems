plugins {
	id("java")
	id("maven-publish")
	id("net.neoforged.moddev.legacyforge")
}

// gradle.properties
val forgeVersion: String by extra
val minecraftVersion: String by extra
val modGroup: String by extra
val modId: String by extra
val modJavaVersion: String by extra
val parchmentMinecraftVersion: String by extra
val parchmentVersionForge: String by extra

val forgeArtifactVersion = "${minecraftVersion}-${forgeVersion}"

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
	dependencyProjects.forEach {
		implementation(it)
	}
}

legacyForge {
	validateAccessTransformers = true

	setAccessTransformers("../Forge/src/main/resources/META-INF/accesstransformer.cfg")

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

	// no runs are configured for API
}

val sourcesJar = tasks.named<Jar>("sourcesJar")
val reobfJarTask = tasks.named<AbstractArchiveTask>("reobfJar")

artifacts {
	archives(reobfJarTask)
	archives(sourcesJar.get())
}

publishing {
	publications {
		register<MavenPublication>("forgeApi") {
			artifactId = baseArchivesName
			artifact(reobfJarTask)
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
