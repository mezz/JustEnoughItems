import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    id("idea")
    id("java")
    id("net.neoforged.moddev")
    id("maven-publish")
}

// gradle.properties
val jUnitVersion: String by extra
val minecraftVersion: String by extra
val neoformTimestamp: String by extra
val modId: String by extra
val modJavaVersion: String by extra
val neoformVersionAndTimestamp = "$minecraftVersion-$neoformTimestamp"
val deduplicatingRunnerVersion: String by extra
val mezzConfigApiDependency: String by rootProject.extra

val baseArchivesName = "${modId}-${minecraftVersion}-gui"
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

neoForge {
    neoFormVersion = neoformVersionAndTimestamp
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
    implementation(mezzConfigApiDependency)
    dependencyProjects.forEach {
        implementation(it)
    }
    implementation("net.mezzdev:deduplicating-runner:$deduplicatingRunnerVersion") {
        isTransitive = false
    }
    testCompileOnly(
        group = "org.jetbrains",
        name = "annotations",
        version = "23.0.0"
    )
    testCompileOnly(
        group = "com.google.code.findbugs",
        name = "jsr305",
        version = "3.0.1"
    )
    testImplementation(
        group = "org.junit.jupiter",
        name = "junit-jupiter",
        version = jUnitVersion
    )
    testRuntimeOnly(
        group = "org.junit.platform",
        name = "junit-platform-launcher"
    )
}

tasks.test {
    useJUnitPlatform()
    include("mezz/jei/test/gui/**")
    include("mezz/jei/gui/**")
    exclude("mezz/jei/test/gui/lib/**")
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
        register<MavenPublication>("guiJar") {
            artifactId = baseArchivesName
            artifact(tasks.jar.get())
            artifact(sourcesJarTask.get())

            val dependencyInfos = dependencyProjects.map {
                mapOf(
                    "groupId" to it.group,
                    "artifactId" to it.base.archivesName.get(),
                    "version" to it.version
                )
            } + listOf(
                mapOf(
                    "groupId" to "net.mezzdev",
                    "artifactId" to "deduplicating-runner",
                    "version" to deduplicatingRunnerVersion
                )
            )

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
