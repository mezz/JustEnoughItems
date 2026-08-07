plugins {
    id("idea")
    id("java")
    id("net.neoforged.moddev")
    id("maven-publish")
}


// gradle.properties
val minecraftVersion = providers.gradleProperty("minecraftVersion").get()
val neoformVersionAndTimestamp = providers.gradleProperty("neoformVersionAndTimestamp").get()
val modId = providers.gradleProperty("modId").get()
val modJavaVersion = providers.gradleProperty("modJavaVersion").get()

val baseArchivesName = "${modId}-${minecraftVersion}-common-api"
base {
    archivesName.set(baseArchivesName)
}

neoForge {
    neoFormVersion = neoformVersionAndTimestamp
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

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(modJavaVersion))
    }
    withSourcesJar()
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    javaToolchains {
        compilerFor {
            languageVersion.set(JavaLanguageVersion.of(modJavaVersion))
        }
    }
}

tasks.withType<Jar> {
    manifest {
        attributes["Fabric-Loom-Remap"] = true
    }
}

publishing {
    publications {
        register<MavenPublication>("commonApiJar") {
            artifactId = base.archivesName.get()
            artifact(tasks.jar)
            artifact(tasks.named("sourcesJar"))
        }
    }
    repositories {
        val deployDir = project.findProperty("DEPLOY_DIR")
        if (deployDir != null) {
            maven(deployDir)
        }
    }
}

idea {
    module {
        for (fileName in listOf("build", "run", "out", "logs")) {
            excludeDirs.add(file(fileName))
        }
    }
}
