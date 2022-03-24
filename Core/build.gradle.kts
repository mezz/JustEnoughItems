plugins {
    java
}

repositories {
    mavenCentral()
}

dependencies {
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
}

sourceSets {
    named("main") {
        //The Core has no resources
        resources.setSrcDirs(emptyList<String>())
    }
    named("test") {
        //The test module has no resources
        resources.setSrcDirs(emptyList<String>())
    }
}
