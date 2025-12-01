plugins {
    java
    application
    id("org.openjfx.javafxplugin") version "0.0.13"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

application {
    mainModule.set("org.example.presssimulation")
    mainClass.set("org.example.presssimulation.SimulationManager")
}

javafx {
    version = "21.0.6"
    modules = listOf("javafx.controls")
}

dependencies {
    implementation("org.controlsfx:controlsfx:11.2.1")
}
