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
		exclusiveMaven("https://maven.neoforged.net/releases") {
			includeGroupByRegex("net\\.neoforged.*")
			includeGroup("codechicken")
			includeGroup("net.covers1624")
		}
		exclusiveMaven("https://repo.spongepowered.org/repository/maven-public/") {
			includeGroupByRegex("org\\.spongepowered.*")
		}
		gradlePluginPortal()
	}
	plugins {
		id("net.neoforged.gradle.common") version("7.0.72")
		id("net.neoforged.gradle.userdev") version("7.0.72")
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

val minecraftVersion: String by settings

rootProject.name = "jei-${minecraftVersion}"
include(
	"Core",
	"Changelog",
	"Common", "CommonApi",
	"NeoForge", "NeoForgeApi",
	"Forge", "ForgeApi",
	"Fabric", "FabricApi",
	"Library",
	"Gui"
)
