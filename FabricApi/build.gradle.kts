repositories {
    maven("https://maven.parchmentmc.org")
}

plugins {
    java
    idea
    id("fabric-loom") version("0.11-SNAPSHOT")
}

// gradle.properties
val curseHomepageUrl: String by extra
val curseProjectId: String by extra
val fabricApiVersion: String by extra
val fabricLoaderVersion: String by extra
val jUnitVersion: String by extra
val parchmentVersionFabric: String by extra
val minecraftVersion: String by extra
val modId: String by extra
val modJavaVersion: String by extra

val baseArchivesName = "${modId}-${minecraftVersion}"
base {
    archivesName.set(baseArchivesName)
}

val dependencyProjects: List<Project> = listOf(
    project(":CommonApi"),
)

dependencyProjects.forEach {
    project.evaluationDependsOn(it.path)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(modJavaVersion))
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    javaToolchains {
        compilerFor {
            languageVersion.set(JavaLanguageVersion.of(modJavaVersion))
        }
    }
}

dependencies {
    minecraft(
        group = "com.mojang",
        name = "minecraft",
        version = minecraftVersion,
    )
    mappings(loom.layered {
        officialMojangMappings()
        parchment("org.parchmentmc.data:parchment-${minecraftVersion}:${parchmentVersionFabric}@zip")
    })
    modImplementation(
        group = "net.fabricmc",
        name = "fabric-loader",
        version = fabricLoaderVersion,
    )
    modImplementation(
        group = "net.fabricmc.fabric-api",
        name = "fabric-api",
        version = fabricApiVersion,
    )
    dependencyProjects.forEach {
        implementation(it)
    }
}
