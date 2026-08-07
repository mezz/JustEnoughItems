plugins {
	id("idea")
	id("java")
	id("net.neoforged.moddev")
}

// gradle.properties
val minecraftVersion: String by extra
val modId: String by extra
val modJavaVersion: String by extra
val neoformVersionAndTimestamp: String by extra

val baseArchivesName = "${modId}-${minecraftVersion}-debug"
base {
	archivesName.set(baseArchivesName)
}

val dependencyProjectPaths = listOf(":CommonApi")

neoForge {
	neoFormVersion = neoformVersionAndTimestamp
}

sourceSets {
	named("test") {
		//The test module has no resources
		resources.setSrcDirs(emptyList<String>())
	}
}

dependencies {
	dependencyProjectPaths.forEach {
		implementation(project(it))
	}
}

java {
	toolchain {
		languageVersion.set(JavaLanguageVersion.of(modJavaVersion))
	}
}

tasks.withType<JavaCompile> {
	options.encoding = "UTF-8"
	javaToolchains {
		compilerFor {
			languageVersion.set(JavaLanguageVersion.of(modJavaVersion))
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
