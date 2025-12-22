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

val commonApi: Project = project(":CommonApi")

project.evaluationDependsOn(commonApi.path)

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
    minecraft(
        group = "com.mojang",
        name = "minecraft",
        version = minecraftVersion,
    )
    implementation(
        group = "net.fabricmc",
        name = "fabric-loader",
        version = fabricLoaderVersion,
    )
    implementation(
        group = "net.fabricmc.fabric-api",
        name = "fabric-api",
        version = fabricApiVersion,
    )
    implementation(
        group = "com.google.code.findbugs",
        name = "jsr305",
        version = "3.0.1"
    )
    implementation(commonApi)
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

artifacts {
    archives(tasks.jar)
    archives(tasks.named("sourcesJar"))
}

publishing {
    publications {
        register<MavenPublication>("fabricApi") {
            artifactId = baseArchivesName
            @Suppress("UnstableApiUsage")
            loom.disableDeprecatedPomGeneration(this)
            artifact(tasks.jar)
            artifact(tasks.named("sourcesJar"))

            val dependencyInfo = mapOf(
                "groupId" to commonApi.tasks.jar.get().group,
                "artifactId" to commonApi.tasks.jar.get().archiveBaseName.get(),
                "version" to commonApi.version
            )

            pom.withXml {
                val dependenciesNode = asNode().appendNode("dependencies")
                val dependencyNode = dependenciesNode.appendNode("dependency")
                dependencyInfo.forEach { (key, value) ->
                    dependencyNode.appendNode(key, value)
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
