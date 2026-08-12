plugins {
    java
    idea
    id("org.spongepowered.gradle.vanilla")
    `maven-publish`
}

repositories {
    mavenCentral()
}

// gradle.properties
val jUnitVersion: String by extra
val minecraftVersion: String by extra
val modId: String by extra
val modJavaVersion: String by extra
val isAppleSilicon = System.getProperty("os.name").startsWith("Mac") &&
    System.getProperty("os.arch") in setOf("aarch64", "arm64")

val baseArchivesName = "${modId}-${minecraftVersion}-lib"
base {
    archivesName.set(baseArchivesName)
}

val dependencyProjects: List<Project> = listOf(
    project(":Common"),
    project(":CommonApi"),
)

dependencyProjects.forEach {
    project.evaluationDependsOn(it.path)
}

minecraft {
    version(minecraftVersion)
    // no runs are configured for Library
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
        name = "junit-jupiter",
        version = jUnitVersion
    )
    testRuntimeOnly(
        group = "org.junit.platform",
        name = "junit-platform-launcher",
        version = jUnitVersion
    )
    if (isAppleSilicon) {
        testRuntimeOnly(files(rootProject.configurations.named("appleSiliconLwjglTestRuntime")))
    }
}

configurations.named("testRuntimeClasspath") {
    if (isAppleSilicon) {
        exclude(group = "org.lwjgl", module = "lwjgl")
    } else {
        extendsFrom(configurations.named("minecraftNatives").get())
    }
}

tasks.named<Test>("test") {
    useJUnitPlatform()
    systemProperty("org.lwjgl.system.SharedLibraryExtractPath", temporaryDir.resolve("lwjgl"))
    include("mezz/jei/test/**")
    include("mezz/jei/library/**")
    exclude("mezz/jei/test/lib/**")
    outputs.upToDateWhen { false }
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

idea {
    module {
        for (fileName in listOf("build", "run", "out", "logs")) {
            excludeDirs.add(file(fileName))
        }
    }
}
