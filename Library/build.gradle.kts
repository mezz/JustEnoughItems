import mezz.jei.gradle.gradleProperty
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
val jUnitVersion = gradleProperty("jUnitVersion")
val minecraftVersion = gradleProperty("minecraftVersion")
val neoformTimestamp = gradleProperty("neoformTimestamp")
val modId = gradleProperty("modId")
val modJavaVersion = gradleProperty("modJavaVersion")
val mixinVersion = gradleProperty("mixinVersion")
val neoformVersionAndTimestamp = "$minecraftVersion-$neoformTimestamp"
val mezzConfigApiDependency: String by rootProject.extra
val mezzConfigNeoForgeDependency: String by rootProject.extra

val baseArchivesName = "${modId}-${minecraftVersion}-lib"
base {
    archivesName.set(baseArchivesName)
}

val dependencyProjects: List<Project> = listOf(
    project(":Common"),

)

dependencyProjects.forEach {
    project.evaluationDependsOn(it.path)
}

val commonApiSourceSet = project(":Common").sourceSets.named("api").get()

sourceSets.configureEach {
    compileClasspath += commonApiSourceSet.output
    runtimeClasspath += commonApiSourceSet.output
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
        version = mixinVersion
    )
    implementation(mezzConfigApiDependency)
    dependencyProjects.forEach {
        implementation(it)
    }
    testImplementation(
        group = "org.junit.jupiter",
        name = "junit-jupiter",
        version = jUnitVersion
    )
    testImplementation(mezzConfigNeoForgeDependency)
    testRuntimeOnly(
        group = "org.junit.platform",
        name = "junit-platform-launcher"
    )
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

            val dependencyInfos = listOf(
                mapOf("groupId" to project.group, "artifactId" to "${modId}-${minecraftVersion}-common-api", "version" to project.version),
                dependencyInfo(mezzConfigApiDependency)
            ) + dependencyProjects.map {
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

fun dependencyInfo(notation: String): Map<String, String> {
    val (groupId, artifactId, version) = notation.split(":")
    return mapOf(
        "groupId" to groupId,
        "artifactId" to artifactId,
        "version" to version
    )
}

idea {
    module {
        for (fileName in listOf("build", "run", "out", "logs")) {
            excludeDirs.add(file(fileName))
        }
    }
}
