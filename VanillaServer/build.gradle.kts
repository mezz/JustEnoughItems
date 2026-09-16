import mezz.jei.gradle.gradleProperty
import org.slf4j.event.Level

plugins {
	java
	id("net.neoforged.moddev")
}

java {
	toolchain {
		languageVersion.set(JavaLanguageVersion.of(gradleProperty("modJavaVersion")))
	}
}

// Keep the vanilla launch independent of Common's Fabric Loom setup.
neoForge {
	neoFormVersion = gradleProperty("neoformVersionAndTimestamp")
	runs {
		create("vanillaServer") {
			server()
			gameDirectory = file("run/vanillaServer")
			programArguments.add("nogui")
			logLevel = Level.INFO
			disableIdeRun()
		}
	}
}
