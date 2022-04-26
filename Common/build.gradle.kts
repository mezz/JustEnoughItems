plugins {
    java
    id("org.spongepowered.gradle.vanilla") version "0.2.1-SNAPSHOT"
}

// gradle.properties
val minecraftVersion: String by extra
val jUnitVersion: String by extra

val dependencyProjects: List<Project> = listOf(
    project(":Core"),
    project(":CommonApi"),
)

dependencyProjects.forEach {
    project.evaluationDependsOn(it.path)
}

minecraft {
    version(minecraftVersion)
    // no runs are configured for Common
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
}
