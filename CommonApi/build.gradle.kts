plugins {
    id("java")
    id("org.spongepowered.gradle.vanilla") version "0.2.1-SNAPSHOT"
}

// gradle.properties
val specificationVersion: String by extra
val modName: String by extra
val minecraftVersion: String by extra

minecraft {
    version(minecraftVersion)
    // no runs are configured for Common API
}

sourceSets {
    named("main") {
        //The API has no resources
        resources.setSrcDirs(emptyList<String>())
    }
    named("test") {
        //The test module has no resources
        resources.setSrcDirs(emptyList<String>())
    }
}

dependencies {
    compileOnly(
        group = "org.spongepowered",
        name = "mixin",
        version = "0.8.5"
    )
}
