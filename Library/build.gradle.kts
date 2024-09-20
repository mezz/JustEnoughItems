import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    id("java")
    id("idea")
    id("eclipse")
    id("maven-publish")
    id("net.neoforged.moddev")
}

// gradle.properties
val jUnitVersion: String by extra
val minecraftVersion: String by extra
val neoformTimestamp: String by extra
val modId: String by extra
val modJavaVersion: String by extra

val baseArchivesName = "${modId}-${minecraftVersion}-lib"
base {
    archivesName.set(baseArchivesName)
}

val dependencyProjects: List<Project> = listOf(
    project(":Core"),
    project(":Common"),
    project(":CommonApi"),
)

dependencyProjects.forEach {
    project.evaluationDependsOn(it.path)
}

neoForge {
    neoFormVersion = "$minecraftVersion-$neoformTimestamp"
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

val sourcesJarTask = tasks.named<Jar>("sourcesJar")

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
        register<MavenPublication>("libraryJar") {
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
