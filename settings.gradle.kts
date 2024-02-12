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

rootProject.name = rootProject.projectDir.name
