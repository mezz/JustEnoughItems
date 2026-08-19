plugins {
    id("net.mezzdev.java-formatting") version("0.4.0")

    // https://maven.fabricmc.net/fabric-loom/fabric-loom.gradle.plugin/maven-metadata.xml
    id("net.fabricmc.fabric-loom") version("1.18.0-alpha.16") apply(false)

    // https://maven.fabricmc.net/net/fabricmc/fabric-loom-companion/net.fabricmc.fabric-loom-companion.gradle.plugin/maven-metadata.xml
    // applying this to all projects allows loom projects to access the required data in a manner that follows Gradle's best practices.
    id("net.fabricmc.fabric-loom-companion") version("1.18.0-alpha.16")

    id("net.mezzdev.modshade") version("0.3.0") apply(false)

    // https://plugins.gradle.org/plugin/me.modmuss50.mod-publish-plugin
    id("me.modmuss50.mod-publish-plugin") version("2.0.1") apply(false)

    id("mezz.jei.api-compatibility")
}

val javaSourceTrees = listOf(
    "Common", "CommonApi",
    "Debug",
    "Fabric", "FabricApi",
    "Gui", "Library",
    "NeoForge", "NeoForgeApi"
).map { moduleName ->
    layout.projectDirectory.dir("$moduleName/src").asFileTree.matching {
        include("*/java/**/*.java")
    }
}

javaFormatting {
    target(javaSourceTrees)
    all()
}

repositories {
    mavenCentral()
}
