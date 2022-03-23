plugins {
	id("java")
	id("net.minecraftforge.gradle") version ("5.1.+")
	id("org.parchmentmc.librarian.forgegradle") version ("1.+")
	id("maven-publish")
}

// gradle.properties
val forgeVersion: String by extra
val mappingsChannel: String by extra
val mappingsVersion: String by extra
val minecraftVersion: String by extra
val modId: String by extra
val modJavaVersion: String by extra

base {
	archivesName.set("${modId}-forge-api-${minecraftVersion}")
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
}

minecraft {
	mappings(mappingsChannel, mappingsVersion)

	// All minecraft configurations in the multi-project must be identical, including ATs,
	// because of a ForgeGradle bug https://github.com/MinecraftForge/ForgeGradle/issues/844
	accessTransformer(file("../Forge/src/main/resources/META-INF/accesstransformer.cfg"))

	// no runs are configured for API
}

tasks.named<Jar>("jar") {
	from(sourceSets.main.get().output)
	for (p in dependencyProjects) {
		from(p.sourceSets.main.get().output)
	}

	duplicatesStrategy = DuplicatesStrategy.FAIL
	finalizedBy("reobfJar")
}

val sourcesJar = tasks.register<Jar>("sourcesJar") {
	from(sourceSets.main.get().allJava)
	for (p in dependencyProjects) {
		from(p.sourceSets.main.get().allJava)
	}

	duplicatesStrategy = DuplicatesStrategy.FAIL
	finalizedBy("reobfJar")
	archiveClassifier.set("sources")
}

artifacts {
	archives(tasks.jar.get())
	archives(sourcesJar.get())
}

publishing {
	publications {
		register<MavenPublication>("maven") {
			artifact(tasks.jar.get())
			artifact(sourcesJar.get())
		}
	}
	repositories {
		val deployDir = project.findProperty("DEPLOY_DIR")
		if (deployDir != null) {
			maven(deployDir)
		}
	}
}
