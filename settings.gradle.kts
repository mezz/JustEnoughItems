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
		maven("https://maven.minecraftforge.net") {
			content { includeGroupAndSubgroups("net.minecraftforge") }
		}
		exclusiveMaven("https://maven.parchmentmc.org", "org.parchmentmc")
		exclusiveContent {
			forRepository { maven("https://maven.blamejared.com/") }
			filter {
				includeGroup("net.mezzdev.java-formatting")
				includeModule("net.mezzdev.gradle", "JavaFormatting")
			}
		}
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

plugins {
	id("org.gradle.toolchains.foojay-resolver-convention") version("1.0.0")
}

val minecraftVersion: String by settings

rootProject.name = "jei-${minecraftVersion}"
include(
	"Changelog",
	"Common",
	"NeoForge",
	"Forge",
	"Fabric",
	"Library",
	"Debug",
	"Gui"
)
