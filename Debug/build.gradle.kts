import mezz.jei.gradle.gradleProperty

plugins {
	id("idea")
	id("java")
	id("net.neoforged.moddev")
}

// gradle.properties
val minecraftVersion = gradleProperty("minecraftVersion")
val modId = gradleProperty("modId")
val modJavaVersion = gradleProperty("modJavaVersion")
val neoformVersionAndTimestamp = gradleProperty("neoformVersionAndTimestamp")

val baseArchivesName = "${modId}-${minecraftVersion}-debug"
base {
	archivesName.set(baseArchivesName)
}

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
	implementation(project(path = ":Common", configuration = "apiClassesElements"))
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
