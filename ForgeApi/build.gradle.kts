plugins {
	id("java")
	id("net.minecraftforge.gradle") version ("5.1.+")
	id("org.parchmentmc.librarian.forgegradle") version ("1.+")
}

// gradle.properties
val forgeVersion: String by extra
val mappingsChannel: String by extra
val mappingsVersion: String by extra
val minecraftVersion: String by extra
val modJavaVersion: String by extra

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
	compileOnly(project(":CommonApi"))
}

minecraft {
	mappings(mappingsChannel, mappingsVersion)
	// no runs are configured for API
}
