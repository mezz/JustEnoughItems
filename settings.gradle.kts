pluginManagement {
	repositories {
		fun exclusiveMaven(url: String, filter: Action<InclusiveRepositoryContentDescriptor>) =
			exclusiveContent {
				forRepository { maven(url) }
				filter(filter)
			}
		exclusiveMaven("https://maven.fabricmc.net/") {
			includeGroup("net.fabricmc")
			includeGroupByRegex("net\\.fabricmc\\..*")
			includeGroup("net.fabricmc.fabric-loom")
		}
		exclusiveMaven("https://maven.neoforged.net/releases") {
			includeGroupByRegex("net\\.neoforged.*")
			includeGroup("codechicken")
			includeGroup("net.covers1624")
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
	"Fabric", "FabricApi",
	"NeoForge", "NeoForgeApi",
	"Library",
	"Gui"
)
