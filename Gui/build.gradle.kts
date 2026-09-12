import mezz.jei.gradle.dependencyInfo
import mezz.jei.gradle.mezzConfigDependency
import mezz.jei.gradle.mezzConfigGuiDependency
import mezz.jei.gradle.gradleProperty
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    id("idea")
    id("java")
    id("net.neoforged.moddev")
    id("maven-publish")
}

val mezzConfigApiDependency = mezzConfigDependency("config-api")
val mezzConfigGuiApiDependency = mezzConfigGuiDependency("config-gui-api")

// gradle.properties
val jUnitVersion = gradleProperty("jUnitVersion")
val minecraftVersion = gradleProperty("minecraftVersion")
val neoformVersionAndTimestamp = gradleProperty("neoformVersionAndTimestamp")
val modGroup = gradleProperty("modGroup")
val modId = gradleProperty("modId")
val modJavaVersion = gradleProperty("modJavaVersion")

val baseArchivesName = "${modId}-${minecraftVersion}-gui"
base {
    archivesName.set(baseArchivesName)
}

val dependencyProjectPaths = listOf(":Common")

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
    implementation(mezzConfigApiDependency)
    compileOnly(mezzConfigGuiApiDependency)
    implementation(project(path = ":Common", configuration = "apiClassesElements"))
    dependencyProjectPaths.forEach {
        implementation(project(it))
    }
    testImplementation(mezzConfigGuiApiDependency)
    testCompileOnly("org.jspecify:jspecify:1.0.0")
    testImplementation("org.junit.jupiter:junit-jupiter:${jUnitVersion}")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
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

            val dependencyInfos = listOf(
                dependencyInfo(mezzConfigApiDependency),
                dependencyInfo(mezzConfigGuiApiDependency) + ("optional" to "true"),
                dependencyInfo("$modGroup:${modId}-${minecraftVersion}-common:${project.version}"),
                dependencyInfo("$modGroup:${modId}-${minecraftVersion}-common-api:${project.version}")
            )
            pom.withXml {
                val dependenciesNode = asNode().appendNode("dependencies")
                dependencyInfos.forEach { dependency ->
                    val dependencyNode = dependenciesNode.appendNode("dependency")
                    dependency.forEach { (key, value) -> dependencyNode.appendNode(key, value) }
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
