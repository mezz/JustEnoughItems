plugins {
    java
    idea
    `maven-publish`
    id("net.fabricmc.fabric-loom")
}

// gradle.properties
val fabricApiVersion: String by extra
val fabricLoaderVersion: String by extra
val minecraftVersion: String by extra
val modId: String by extra
val modJavaVersion: String by extra
val modGroup: String by extra

val baseArchivesName = "${modId}-${minecraftVersion}-fabric-api"
base {
    archivesName.set(baseArchivesName)
}

val dependencyProjectPaths = listOf(":CommonApi")

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

dependencies {
    minecraft("com.mojang:minecraft:${minecraftVersion}")
    implementation("net.fabricmc:fabric-loader:${fabricLoaderVersion}")
    implementation("net.fabricmc.fabric-api:fabric-api:${fabricApiVersion}")
    dependencyProjectPaths.forEach {
        implementation(project(it))
    }
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

tasks.assemble {
    dependsOn(tasks.named("sourcesJar"))
}

publishing {
    publications {
        register<MavenPublication>("fabricApi") {
            artifactId = baseArchivesName
            @Suppress("UnstableApiUsage")
            loom.disableDeprecatedPomGeneration(this)
            artifact(tasks.jar)
            artifact(tasks.named("sourcesJar"))

            val dependencyInfos = listOf("common-api").map {
                mapOf(
                    "groupId" to modGroup,
                    "artifactId" to "${modId}-${minecraftVersion}-$it",
                    "version" to project.version
                )
            }

            pom.withXml {
                val dependenciesNode = asNode().appendNode("dependencies")
                dependencyInfos.forEach {
                    val dependencyNode = dependenciesNode.appendNode("dependency")
                    it.forEach { (key, value) ->
                        dependencyNode.appendNode(key, value)
                    }
                }
            }
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
