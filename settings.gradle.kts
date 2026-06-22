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
		exclusiveMaven("https://maven.minecraftforge.net", "net.minecraftforge")
		exclusiveMaven("https://maven.parchmentmc.org", "org.parchmentmc")
		maven("https://maven.blamejared.com")
		exclusiveMaven("https://maven.fabricmc.net/", "net.fabricmc", "fabric-loom")
		maven("https://repo.spongepowered.org/repository/maven-public/") {
			name = "Sponge Snapshots"
			content {
				includeGroupAndSubgroups("org.spongepowered")
				includeGroupAndSubgroups("net.minecraftforge")
			}
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

val minecraftVersion: String by settings

rootProject.name = "jei-${minecraftVersion}"
include(
	"Changelog",
	"Common", "CommonApi",
	"Debug",
	"Forge", "ForgeApi",
	"Fabric", "FabricApi",
	"Library",
	"Gui"
)
