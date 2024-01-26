pluginManagement {
    repositories {
        exclusiveContent {
            forRepository {
                maven {
                    name = "GTNH Maven"
                    setUrl("https://nexus.gtnewhorizons.com/repository/public/")
                }
            }
            filter {
                includeGroup("com.gtnewhorizons")
                includeGroup("com.gtnewhorizons.retrofuturagradle")
            }
        }
        gradlePluginPortal()
    }
}

val minecraftVersion: String by settings
rootProject.name = "jei-${minecraftVersion}"
