@file:Suppress("UnstableApiUsage")

pluginManagement {
	repositories {
		fun exclusiveMaven(url: String, vararg groupPrefixes: String) =
			exclusiveContent {
				forRepository { maven(url) }
				filter {
					groupPrefixes.forEach(::includeGroupAndSubgroups)
				}
			}
		exclusiveMaven("https://maven.parchmentmc.org", "org.parchmentmc")
		exclusiveMaven("https://maven.fabricmc.net/", "net.fabricmc", "fabric-loom")
		exclusiveMaven("https://maven.neoforged.net/releases", "net.neoforged", "codechicken", "net.covers1624")
		maven("https://repo.spongepowered.org/repository/maven-public/") {
			content {
				includeGroupAndSubgroups("org.spongepowered")
				includeGroupAndSubgroups("net.minecraftforge")
			}
		}
		gradlePluginPortal()
	}
	resolutionStrategy {
		eachPlugin {
			if (requested.id.id == "org.spongepowered.mixin") {
				useModule("org.spongepowered:mixingradle:${requested.version}")
			}
		}
	}
}

val minecraftVersion = providers.gradleProperty("minecraftVersion").get()

rootProject.name = "jei-${minecraftVersion}"
include(
	"Changelog",
	"Common", "CommonApi",
	"Fabric", "FabricApi",
	"NeoForge", "NeoForgeApi",
	"Library",
	"Debug",
	"Gui"
)

gradle.lifecycle.beforeProject {
	if (path != ":") {
		pluginManager.apply("mezz.jei.project")
	}
}
