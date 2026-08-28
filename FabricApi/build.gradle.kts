import groovy.util.Node
import mezz.jei.gradle.gradleProperty
import net.fabricmc.loom.task.RemapJarTask
import net.fabricmc.loom.task.RemapSourcesJarTask
import org.gradle.api.publish.tasks.GenerateModuleMetadata

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
val fabricApiVersion = gradleProperty("fabricApiVersion")
val fabricLoaderVersion = gradleProperty("fabricLoaderVersion")
val parchmentVersionFabric = gradleProperty("parchmentVersionFabric")
val parchmentMinecraftVersion = gradleProperty("parchmentMinecraftVersion")
val minecraftVersion = gradleProperty("minecraftVersion")
val modId = gradleProperty("modId")
val modJavaVersion = gradleProperty("modJavaVersion")
val modGroup = gradleProperty("modGroup")

val baseArchivesName = "${modId}-${minecraftVersion}-fabric-api"
base {
    archivesName.set(baseArchivesName)
}

val commonApiProjectPath = ":CommonApi"
val commonApiIntermediaryBaseArchivesName = provider {
    "${modId}-${minecraftVersion}-common-api-intermediary"
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(modJavaVersion))
    }
    withSourcesJar()
}

val commonApiJar = configurations.create("commonApiJar") {
    isCanBeConsumed = false
    isCanBeResolved = true
    attributes {
        attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.LIBRARY))
        attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_RUNTIME))
        attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements.JAR))
    }
}

val commonApiSourcesJar = configurations.create("commonApiSourcesJar") {
    isCanBeConsumed = false
    isCanBeResolved = true
    attributes {
        attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.DOCUMENTATION))
        attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_RUNTIME))
        attribute(DocsType.DOCS_TYPE_ATTRIBUTE, objects.named(DocsType.SOURCES))
    }
}

val commonApiIntermediaryJar = tasks.register<RemapJarTask>("commonApiIntermediaryJar") {
    inputFile.set(layout.file(commonApiJar.elements.map { it.single().asFile }))
    archiveBaseName.set(commonApiIntermediaryBaseArchivesName)
    group = modGroup
}

val commonApiIntermediarySourcesJar = tasks.register<RemapSourcesJarTask>("commonApiIntermediarySourcesJar") {
    inputFile.set(layout.file(commonApiSourcesJar.elements.map { it.single().asFile }))
    archiveBaseName.set(commonApiIntermediaryBaseArchivesName)
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
    minecraft("com.mojang:minecraft:${minecraftVersion}")
    @Suppress("UnstableApiUsage")
    mappings(loom.layered {
        officialMojangMappings()
        parchment("org.parchmentmc.data:parchment-${parchmentMinecraftVersion}:${parchmentVersionFabric}@zip")
    })
    modImplementation("net.fabricmc:fabric-loader:${fabricLoaderVersion}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${fabricApiVersion}")
    implementation(project(commonApiProjectPath))
    commonApiJar(project(commonApiProjectPath)) {
        isTransitive = false
    }
    commonApiSourcesJar(project(commonApiProjectPath)) {
        isTransitive = false
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
    dependsOn(
        tasks.remapJar,
        tasks.remapSourcesJar,
        commonApiIntermediaryJar,
        commonApiIntermediarySourcesJar
    )
}

tasks.withType<GenerateModuleMetadata>().configureEach {
    if (name == "generateMetadataFileForFabricApiPublication") {
        enabled = false
    }
}

publishing {
    publications {
        register<MavenPublication>("commonApiIntermediary") {
            artifactId = commonApiIntermediaryBaseArchivesName.get()
            artifact(commonApiIntermediaryJar)
            artifact(commonApiIntermediarySourcesJar)
        }
        register<MavenPublication>("fabricApi") {
            artifactId = baseArchivesName
            @Suppress("UnstableApiUsage")
            loom.disableDeprecatedPomGeneration(this)
            from(components["java"])
            setArtifacts(listOf(tasks.remapJar, tasks.remapSourcesJar))

            val dependencyInfo = mapOf(
                "groupId" to modGroup,
                "artifactId" to commonApiIntermediaryBaseArchivesName.get(),
                "version" to project.version
            )

            pom.withXml {
                val projectNode = asNode()
                projectNode.children()
                    .filterIsInstance<Node>()
                    .filter { it.name().toString().endsWith("dependencies") }
                    .forEach { projectNode.remove(it) }
                val dependenciesNode = projectNode.appendNode("dependencies")
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
