import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.slf4j.event.Level

plugins {
    id("idea")
    id("java")
    id("java-test-fixtures")
    id("net.neoforged.moddev")
    id("maven-publish")
}

// gradle.properties
val jUnitVersion: String by extra
val minecraftVersion: String by extra
val neoformTimestamp: String by extra
val modId: String by extra
val modJavaVersion: String by extra
val suffixtreeVersion: String by extra

val baseArchivesName = "${modId}-${minecraftVersion}-common"
base {
    archivesName.set(baseArchivesName)
}

val dependencyProjects: List<Project> = listOf(
    project(":CommonApi"),
)

dependencyProjects.forEach {
    project.evaluationDependsOn(it.path)
}

neoForge {
    neoFormVersion = "$minecraftVersion-$neoformTimestamp"
    addModdingDependenciesTo(sourceSets.test.get())

    runs {
        create("vanillaServer") {
            server()
			gameDirectory = file("run/vanillaServer")
			programArguments.addAll("nogui")
			logLevel = Level.INFO
		}
	}
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
    implementation(
        group = "org.apache.logging.log4j",
        name = "log4j-api",
        version = "2.17.0"
    )
    implementation(
        group = "net.mezzdev",
        name = "suffixtree",
        version = suffixtreeVersion
    ) {
        isTransitive = false
    }
    dependencyProjects.forEach {
        implementation(it)
    }
    testFixturesCompileOnly("org.jspecify:jspecify:1.0.0")
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
    include("mezz/jei/test/**")
    include("mezz/jei/common/util/**")
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

publishing {
    publications {
        register<MavenPublication>("commonJar") {
            artifactId = base.archivesName.get()
            artifact(tasks.jar)
            artifact(tasks.named("sourcesJar"))

            val dependencyInfos = dependencyProjects.map {
                mapOf(
                    "groupId" to it.group,
                    "artifactId" to it.base.archivesName.get(),
                    "version" to it.version
                )
            }

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
