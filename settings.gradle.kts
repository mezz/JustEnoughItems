pluginManagement {
	repositories {
		fun exclusiveMaven(url: String, filter: Action<InclusiveRepositoryContentDescriptor>) =
			exclusiveContent {
				forRepository { maven(url) }
				filter(filter)
			}
		exclusiveMaven("https://maven.minecraftforge.net") {
			includeGroupByRegex("net\\.minecraftforge.*")
		}
		exclusiveMaven("https://maven.parchmentmc.org") {
			includeGroupByRegex("org\\.parchmentmc.*")
		}
		exclusiveMaven("https://maven.fabricmc.net/") {
			includeGroup("net.fabricmc")
			includeGroup("fabric-loom")
		}
		exclusiveMaven("https://repo.spongepowered.org/repository/maven-public/") {
			includeGroupByRegex("org\\.spongepowered.*")
		}
		gradlePluginPortal()
	}
	resolutionStrategy {
		eachPlugin {
			if (requested.id.id == "net.minecraftforge.gradle") {
				useModule("${requested.id}:ForgeGradle:${requested.version}")
			}
			if (requested.id.id == "org.spongepowered.mixin") {
				useModule("org.spongepowered:mixingradle:${requested.version}")
			}
		}
	}
}

plugins {
	id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}

val minecraftVersion: String by settings

rootProject.name = "jei-${minecraftVersion}"
include(
	"Core",
	"Changelog",
	"Common", "CommonApi",
	"Forge", "ForgeApi",
	"Fabric", "FabricApi",
	"Library",
	"Gui"
)
