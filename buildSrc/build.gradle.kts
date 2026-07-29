plugins {
	`kotlin-dsl`
}

repositories {
	mavenCentral()
}

gradlePlugin {
	plugins {
		create("apiCompatibility") {
			id = "mezz.jei.api-compatibility"
			implementationClass = "mezz.jei.gradle.ApiCompatibilityPlugin"
		}
	}
}
