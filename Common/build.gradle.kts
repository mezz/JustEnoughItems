import mezz.jei.gradle.gradleProperty
import net.neoforged.jarcompatibilitychecker.core.NonExtendableApiCheckMode
import net.neoforged.jarcompatibilitychecker.gradle.CompatibilityTask
import org.gradle.api.publish.maven.internal.publication.MavenPublicationInternal
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.gradle.language.base.plugins.LifecycleBasePlugin
import org.slf4j.event.Level

plugins {
    id("idea")
    id("java")
    id("java-test-fixtures")
    id("net.neoforged.moddev")
    id("net.neoforged.jarcompatibilitychecker")
    id("maven-publish")
}

// gradle.properties
val jUnitVersion = gradleProperty("jUnitVersion")
val minecraftVersion = gradleProperty("minecraftVersion")
val neoformVersionAndTimestamp = gradleProperty("neoformVersionAndTimestamp")
val modGroup = gradleProperty("modGroup")
val modId = gradleProperty("modId")
val modJavaVersion = gradleProperty("modJavaVersion")
val bakedSubstringIndexVersion = gradleProperty("bakedSubstringIndexVersion")
val suffixtreeVersion = gradleProperty("suffixtreeVersion")

val baseArchivesName = "${modId}-${minecraftVersion}-common"
val apiArchivesName = "${modId}-${minecraftVersion}-common-api"
base {
    archivesName.set(baseArchivesName)
}

val generatedJeiGuiColorsResources = layout.buildDirectory.dir("generated/resources/jeiGuiColors")

val apiSourceSet = sourceSets.create("api") {
    resources.setSrcDirs(emptyList<String>())
    output.setResourcesDir(layout.buildDirectory.dir("classes/java/api"))
}

configurations.create("apiClassesElements") {
    isCanBeConsumed = true
    isCanBeResolved = false
    outgoing.capability("${project.group}:$apiArchivesName:${project.version}")
    outgoing.artifact(layout.buildDirectory.dir("classes/java/api")) {
        builtBy(tasks.named(apiSourceSet.classesTaskName))
        type = "directory"
    }
}

neoForge {
    neoFormVersion = neoformVersionAndTimestamp
    addModdingDependenciesTo(apiSourceSet)
    addModdingDependenciesTo(sourceSets.test.get())

    runs {
        create("vanillaServer") {
            server()
            gameDirectory = file("run/vanillaServer")
            programArguments.addAll("nogui")
            logLevel = Level.INFO
            disableIdeRun()
        }
    }
}

val datagenSourceSet = sourceSets.create("datagen") {
    compileClasspath += sourceSets.main.get().output.classesDirs
    compileClasspath += sourceSets.main.get().compileClasspath
    runtimeClasspath += output
    runtimeClasspath += compileClasspath
}

val generateJeiGuiColors = tasks.register<JavaExec>("generateJeiGuiColors") {
    dependsOn(tasks.named(datagenSourceSet.classesTaskName))
    mainClass.set("mezz.jei.common.gui.JeiGuiColorsDataGenerator")
    classpath = datagenSourceSet.runtimeClasspath
    args(generatedJeiGuiColorsResources.get().asFile.absolutePath)
    outputs.dir(generatedJeiGuiColorsResources)
}

sourceSets {
    named("main") {
        resources.srcDir(generateJeiGuiColors)
    }
    named("test") {
        //The test module has no resources
        resources.setSrcDirs(emptyList<String>())
    }
}

dependencies {
    implementation(apiSourceSet.output)
    implementation("org.jetbrains:annotations:26.0.2")
    implementation("com.google.guava:guava:33.5.0-jre")
    implementation("it.unimi.dsi:fastutil:8.5.18")
    implementation("org.apache.logging.log4j:log4j-api:2.25.2")
    implementation("net.mezzdev:baked-substring-index:${bakedSubstringIndexVersion}") {
        isTransitive = false
    }
    implementation("net.mezzdev:suffixtree:${suffixtreeVersion}") {
        isTransitive = false
    }
    testFixturesCompileOnly("org.jspecify:jspecify:1.0.0")
    testImplementation("org.junit.jupiter:junit-jupiter:${jUnitVersion}")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
    include("mezz/jei/common/gui/**")
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

val apiJarTask = tasks.register<Jar>("apiJar") {
    archiveBaseName.set(apiArchivesName)
    from(apiSourceSet.output)
    manifest.attributes["Implementation-Title"] = "jar"
}

val apiSourcesJarTask = tasks.register<Jar>("apiSourcesJar") {
    archiveBaseName.set(apiArchivesName)
    archiveClassifier.set("sources")
    from(apiSourceSet.allSource)
    manifest.attributes["Implementation-Title"] = "sourcesJar"
}

configurations.create("apiSourcesElements") {
    isCanBeConsumed = true
    isCanBeResolved = false
    outgoing.capability("${project.group}:$apiArchivesName:${project.version}")
    outgoing.artifact(apiSourcesJarTask)
}

tasks.assemble {
    dependsOn(apiJarTask, apiSourcesJarTask)
}

tasks.named<CompatibilityTask>("checkJarCompatibility") {
    group = LifecycleBasePlugin.VERIFICATION_GROUP
    description = "Checks the Common API against the latest published API jar in the same major version."
    mavens.set(listOf("https://maven.blamejared.com"))
    // The plugin defaults auxiliary libraries to the main compile classpath.
    // This API check intentionally runs without them, avoiding the full Minecraft classpath.
    libraries.setFrom(emptyList<Any>())
    nonExtendableApiCheckMode.set(NonExtendableApiCheckMode.SKIP)
    fail.set(true)
    inputJar.set(apiJarTask.flatMap { it.archiveFile })
    artifact.set("${project.group}:$apiArchivesName")
}

publishing {
    publications {
        register<MavenPublication>("commonApiJar") {
            // Project dependencies should resolve to the main publication's coordinates.
            (this as MavenPublicationInternal).isAlias = true
            artifactId = apiArchivesName
            artifact(apiJarTask)
            artifact(apiSourcesJarTask)
        }
        register<MavenPublication>("commonJar") {
            artifactId = base.archivesName.get()
            artifact(tasks.jar)
            artifact(tasks.named("sourcesJar"))

            val dependencyInfos = listOf("common-api").map {
                mapOf(
                    "groupId" to modGroup,
                    "artifactId" to "${modId}-${minecraftVersion}-$it",
                    "version" to project.version
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
