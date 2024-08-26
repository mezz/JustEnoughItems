pluginManagement {
	repositories {
		fun exclusiveMaven(url: String, filter: Action<InclusiveRepositoryContentDescriptor>) =
			exclusiveContent {
				forRepository { maven(url) }
				filter(filter)
			}
		maven("https://maven.minecraftforge.net") {
			content {
				includeGroupByRegex("net\\.minecraftforge.*")
			}
		}
		exclusiveMaven("https://maven.parchmentmc.org") {
			includeGroupByRegex("org\\.parchmentmc.*")
		}
		exclusiveMaven("https://maven.fabricmc.net/") {
			includeGroup("net.fabricmc")
			includeGroup("fabric-loom")
		}
		maven("https://repo.spongepowered.org/repository/maven-public/") {
			content {
				includeGroupByRegex("org\\.spongepowered.*")
				includeGroupByRegex("net\\.minecraftforge.*")
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
	"Core",
	"Changelog",
	"Common", "CommonApi",
	"Forge", "ForgeApi",
	"Fabric", "FabricApi",
	"Library",
	"Gui"
)
