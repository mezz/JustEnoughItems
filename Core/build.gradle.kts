import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    id("idea")
    id("java")
    id("maven-publish")
}

repositories {
    mavenCentral()
}

// gradle.properties
val jUnitVersion: String by extra
val minecraftVersion: String by extra
val modId: String by extra
val modJavaVersion: String by extra

dependencies {
    implementation("org.jetbrains:annotations:26.0.2")
    implementation("com.google.guava:guava:33.5.0-jre")
    implementation("it.unimi.dsi:fastutil:8.5.18")
    implementation("org.apache.logging.log4j:log4j-api:2.25.2")
    testImplementation("org.junit.jupiter:junit-jupiter:${jUnitVersion}")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
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

tasks.test {
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

val sourcesJarTask = tasks.named<Jar>("sourcesJar")

val baseArchivesName = "${modId}-${minecraftVersion}-core"
base {
    archivesName.set(baseArchivesName)
}

artifacts {
    archives(tasks.jar.get())
    archives(sourcesJarTask.get())
}

publishing {
    publications {
        register<MavenPublication>("coreJar") {
            artifactId = baseArchivesName
            artifact(tasks.jar.get())
            artifact(sourcesJarTask.get())
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

