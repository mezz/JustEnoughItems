plugins {
    id("net.mezzdev.java-formatting") version("0.2.1")

    // https://maven.fabricmc.net/fabric-loom/fabric-loom.gradle.plugin/maven-metadata.xml
    id("net.fabricmc.fabric-loom") version("1.17.18") apply(false)

    // https://maven.fabricmc.net/net/fabricmc/fabric-loom-companion/net.fabricmc.fabric-loom-companion.gradle.plugin/maven-metadata.xml
    // applying this to all projects allows loom projects to access the required data in a manner that follows Gradle's best practices.
    id("net.fabricmc.fabric-loom-companion") version("1.17.18")

    // https://projects.neoforged.net/neoforged/moddevgradle
    id("net.neoforged.moddev") version("2.0.143") apply(false)

    id("net.mezzdev.modshade") version("0.3.0") apply(false)

    // https://plugins.gradle.org/plugin/me.modmuss50.mod-publish-plugin
    id("me.modmuss50.mod-publish-plugin") version("2.0.1") apply(false)

    id("mezz.jei.api-compatibility")
}

javaFormatting {
    all()
}

repositories {
    mavenCentral()
}
