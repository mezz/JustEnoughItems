plugins {
	java
	idea
	id("org.spongepowered.gradle.vanilla")
}

// gradle.properties
val minecraftVersion: String by extra
val modId: String by extra
val modJavaVersion: String by extra

val baseArchivesName = "${modId}-${minecraftVersion}-debug"
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
	// no runs are configured for Debug
}

dependencies {
	dependencyProjects.forEach {
		implementation(it)
	}
}

java {
	toolchain {
		languageVersion.set(JavaLanguageVersion.of(modJavaVersion))
	}
}

tasks.withType<JavaCompile> {
	options.encoding = "UTF-8"
	javaToolchains {
		compilerFor {
			languageVersion.set(JavaLanguageVersion.of(modJavaVersion))
		}
	}
}
val copyModMetadataToClasses = tasks.register<Copy>("copyModMetadataToClasses") {
	// ModDevGradle exposes classes and resources as separate mod roots on this branch.
	from(layout.buildDirectory.dir("resources/main/META-INF")) {
		include("mods.toml")
		into("META-INF")
	}
	from(layout.buildDirectory.dir("resources/main")) {
		include("pack.mcmeta")
	}
	into(layout.buildDirectory.dir("classes/java/main"))
	dependsOn(
		tasks.named(sourceSets.main.get().compileJavaTaskName),
		tasks.named(sourceSets.main.get().processResourcesTaskName)
	)
}

tasks.named(sourceSets.main.get().classesTaskName) {
	dependsOn(copyModMetadataToClasses)
}

tasks.named<Jar>("jar") {
	duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

idea {
	module {
		for (fileName in listOf("build", "run", "out", "logs")) {
			excludeDirs.add(file(fileName))
		}
	}
}
