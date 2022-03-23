buildscript {
    dependencies {
        constraints {
            // Force ASM on buildscript/plugin classpath to 9.2 instead of possibly outdated versions
            classpath("org.ow2.asm:asm:9.2")
        }
    }
}

plugins {
    java
    id("org.spongepowered.gradle.vanilla") version "0.2.1-SNAPSHOT"
}

// gradle.properties
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
