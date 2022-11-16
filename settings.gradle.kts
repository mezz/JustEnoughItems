pluginManagement {
	repositories {
		maven("https://maven.minecraftforge.net")
		maven("https://maven.parchmentmc.org")
		maven("https://maven.blamejared.com")
		gradlePluginPortal()
		maven("https://maven.fabricmc.net/") {
			name = "Fabric"
		}
		maven("https://repo.spongepowered.org/repository/maven-public/") {
			name = "Sponge Snapshots"
		}
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
	"Library", "LibraryApi",
	"Gui", "GuiApi"
)
