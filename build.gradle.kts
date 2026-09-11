import org.gradle.language.base.plugins.LifecycleBasePlugin

plugins {
    id("net.mezzdev.java-formatting") version("0.4.0")

    // https://maven.fabricmc.net/fabric-loom/fabric-loom.gradle.plugin/maven-metadata.xml
    id("net.fabricmc.fabric-loom") version("1.17.20") apply(false)

    // https://maven.fabricmc.net/net/fabricmc/fabric-loom-companion/net.fabricmc.fabric-loom-companion.gradle.plugin/maven-metadata.xml
    // applying this to all projects allows loom projects to access the required data in a manner that follows Gradle's best practices.
    id("net.fabricmc.fabric-loom-companion") version("1.17.20")

    // https://projects.neoforged.net/neoforged/moddevgradle
    id("net.neoforged.moddev") version("2.0.144") apply(false)

    id("net.mezzdev.modshade") version("0.6.0") apply(false)

    // https://plugins.gradle.org/plugin/me.modmuss50.mod-publish-plugin
    id("me.modmuss50.mod-publish-plugin") version("2.2.0") apply(false)

    id("net.neoforged.jarcompatibilitychecker") version("0.1.19") apply(false)
}

javaFormatting {
    all()
}

repositories {
    mavenCentral()
}

val apiProjectPaths = listOf(":Common", ":Fabric", ":NeoForge")

val checkApiCompatibility = tasks.register("checkApiCompatibility") {
    group = LifecycleBasePlugin.VERIFICATION_GROUP
    description = "Checks all published JEI API jars for compatibility with the latest published API jars in the same major version."
    dependsOn(apiProjectPaths.map { "$it:checkJarCompatibility" })
}

tasks.named(LifecycleBasePlugin.CHECK_TASK_NAME) {
    dependsOn(checkApiCompatibility)
}
