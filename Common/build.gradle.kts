import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    java
    id("net.neoforged.moddev")
    `maven-publish`
}

// gradle.properties
val jUnitVersion: String by extra
val minecraftVersion: String by extra
val neoformTimestamp: String by extra
val modId: String by extra
val modJavaVersion: String by extra
val modName: String by extra

val resourceProperties = mapOf("modName" to modName)

val baseArchivesName = "${modId}-${minecraftVersion}-common"
base {
    archivesName.set(baseArchivesName)
}

val dependencyProjects: List<Project> = listOf(
    project(":Core"),
    project(":CommonApi"),
)

dependencyProjects.forEach {
    project.evaluationDependsOn(it.path)
}

neoForge {
    neoFormVersion = "$minecraftVersion-$neoformTimestamp"
    addModdingDependenciesTo(sourceSets.test.get())
}

sourceSets {
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
    dependencyProjects.forEach {
        implementation(it)
    }
    testImplementation(
        group = "org.junit.jupiter",
        name = "junit-jupiter-api",
        version = jUnitVersion
    )
    testRuntimeOnly(
        group = "org.junit.jupiter",
        name = "junit-jupiter-engine",
        version = jUnitVersion
    )
}

tasks.named<Test>("test") {
    useJUnitPlatform()
    include("mezz/jei/test/**")
    exclude("mezz/jei/test/lib/**")
    outputs.upToDateWhen { false }
    testLogging {
        events = setOf(TestLogEvent.FAILED)
        exceptionFormat = TestExceptionFormat.FULL
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

tasks.withType<ProcessResources> {
    // this will ensure that this task is redone when the versions change.
    inputs.properties(resourceProperties)

    filesMatching("pack.mcmeta") {
        expand(resourceProperties)
    }
}

publishing {
    publications {
        register<MavenPublication>("commonJar") {
            artifactId = base.archivesName.get()
            artifact(tasks.jar)
            artifact(tasks.named("sourcesJar"))

            pom.withXml {
                val dependenciesNode = asNode().appendNode("dependencies")
                dependencyProjects.forEach {
                    val dependencyNode = dependenciesNode.appendNode("dependency")
                    dependencyNode.appendNode("groupId", it.group)
                    dependencyNode.appendNode("artifactId", it.base.archivesName.get())
                    dependencyNode.appendNode("version", it.version)
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
