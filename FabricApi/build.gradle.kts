import net.fabricmc.loom.task.RemapJarTask

plugins {
    java
    idea
    `maven-publish`
    id("fabric-loom")
}

repositories {
    fun exclusiveMaven(url: String, filter: Action<InclusiveRepositoryContentDescriptor>) =
        exclusiveContent {
            forRepository { maven(url) }
            filter(filter)
        }
    exclusiveMaven("https://maven.parchmentmc.org") {
        includeGroupByRegex("org\\.parchmentmc.*")
    }
}

// gradle.properties
val fabricApiVersion: String by extra
val fabricLoaderVersion: String by extra
val parchmentVersionFabric: String by extra
val parchmentMinecraftVersion: String by extra
val minecraftVersion: String by extra
val modId: String by extra
val modJavaVersion: String by extra
val modGroup: String by extra

val baseArchivesName = "${modId}-${minecraftVersion}-fabric-api"
base {
    archivesName.set(baseArchivesName)
}

val commonApi = project(":CommonApi")

project.evaluationDependsOn(commonApi.path)

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(modJavaVersion))
    }
    withSourcesJar()
}

val commonApiIntermediaryJar = tasks.create<RemapJarTask>("commonApiIntermediaryJar") {
    val commonJar = commonApi.tasks.jar.get().archiveFile
    val commonApiBaseArchivesName = commonApi.base.archivesName;
    inputFile.set(commonJar)
    archiveBaseName.set(provider { commonApiBaseArchivesName.get() + "-intermediary" })
    archiveClassifier.set("jar")
    group = modGroup
}

val commonApiIntermediarySourcesJar = tasks.create<RemapJarTask>("commonApiIntermediarySourcesJar") {
    val commonSourcesJar = commonApi.tasks.named<Jar>("sourcesJar").get().archiveFile;
    val commonApiBaseArchivesName = commonApi.base.archivesName;
    inputFile.set(commonSourcesJar)
    archiveBaseName.set(provider { commonApiBaseArchivesName.get() + "-intermediary" })
    archiveClassifier.set("sources")
    group = modGroup
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
    @Suppress("UnstableApiUsage")
    mappings(loom.layered {
        officialMojangMappings()
        parchment("org.parchmentmc.data:parchment-${parchmentMinecraftVersion}:${parchmentVersionFabric}@zip")
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
    archives(tasks.remapJar)
    archives(tasks.remapSourcesJar)
    archives(commonApiIntermediaryJar)
    archives(commonApiIntermediarySourcesJar)
}

publishing {
    publications {
        register<MavenPublication>("commonApiIntermediary") {
            artifactId = commonApiIntermediaryJar.archiveBaseName.get()
            @Suppress("UnstableApiUsage")
            loom.disableDeprecatedPomGeneration(this)
            artifact(commonApiIntermediaryJar)
            artifact(commonApiIntermediarySourcesJar)
        }
        register<MavenPublication>("fabricApi") {
            artifactId = baseArchivesName
            @Suppress("UnstableApiUsage")
            loom.disableDeprecatedPomGeneration(this)
            artifact(tasks.remapJar)
            artifact(tasks.remapSourcesJar)

            val dependencyInfo = mapOf(
                "groupId" to commonApiIntermediaryJar.group,
                "artifactId" to commonApiIntermediaryJar.archiveBaseName.get(),
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
