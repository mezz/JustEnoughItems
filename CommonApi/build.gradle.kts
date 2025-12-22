plugins {
    id("idea")
    id("java")
    id("net.neoforged.moddev")
    id("maven-publish")
}


// gradle.properties
val mixinVersion: String by extra
val minecraftVersion: String by extra
val neoformVersionAndTimestamp: String by extra
val modId: String by extra
val modJavaVersion: String by extra

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

dependencies {
    compileOnly("org.spongepowered:mixin:${mixinVersion}")
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

