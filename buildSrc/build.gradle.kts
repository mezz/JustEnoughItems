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
	}
}
