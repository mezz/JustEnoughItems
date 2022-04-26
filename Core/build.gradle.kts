plugins {
    java
}

repositories {
    mavenCentral()
}

// gradle.properties
val jUnitVersion: String by extra

dependencies {
    implementation(
        group = "com.google.guava",
        name = "guava",
        version = "31.1-jre"
    )
    implementation(
        group = "org.jetbrains",
        name = "annotations",
        version = "23.0.0"
    )
    implementation(
        group = "it.unimi.dsi",
        name = "fastutil",
        version = "8.5.6"
    )
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

sourceSets {
    named("main") {
        //The Core has no resources
        resources.setSrcDirs(emptyList<String>())
    }
    named("test") {
        //The test module has no resources
        resources.setSrcDirs(emptyList<String>())
    }
}

tasks.named<Test>("test") {
    useJUnitPlatform()
    include("mezz/jei/test/**")
    exclude("mezz/jei/test/lib/**")
    outputs.upToDateWhen { false }
}
