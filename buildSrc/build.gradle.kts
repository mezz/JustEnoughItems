plugins {
	`kotlin-dsl`
}

repositories {
	mavenCentral()
}

gradlePlugin {
	plugins {
		create("jeiProject") {
			id = "mezz.jei.project"
			implementationClass = "mezz.jei.gradle.JeiProjectPlugin"
		}
		create("apiCompatibility") {
			id = "mezz.jei.api-compatibility"
			implementationClass = "mezz.jei.gradle.ApiCompatibilityPlugin"
		}
	}
}
