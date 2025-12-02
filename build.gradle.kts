plugins {
    java
    application
    id("org.openjfx.javafxplugin") version "0.0.13"
    id("org.beryx.jlink") version "2.24.1"
}

val companyName = "MyCompany" // название вашей шедевро-компании
val appName = "PressSimulation" // название вашего шедевро-приложения
val icoPath = "src/main/resources/icon.jpg" // путь к иконочке
group = "org.example"
version = "1.0"


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

dependencies {
    implementation("org.yaml:snakeyaml:2.2")
}

javafx {
    version = "21.0.6"
    modules = listOf("javafx.controls")
}

jlink {
    options.set(listOf("--strip-debug", "--compress", "2", "--no-header-files", "--no-man-pages"))

    launcher {
        name = appName

        jvmArgs = listOf(
            "-XX:+UseG1GC",
            "-XX:MaxGCPauseMillis=20",
            "-Xms512m",
            "-Xmx1024m"
        )
    }

    jpackage {
        // папка, где будет находиться ваш будущий msi (можно поменять путь с ../build/installer на любой другой)
        val installerDir = layout.buildDirectory.dir("installer").get().asFile.absolutePath
        installerOptions.addAll(listOf("--verbose", "--dest", installerDir))

        // это команды для установщика под разные системы (Windows, Linux и Mac)
        if (org.gradle.internal.os.OperatingSystem.current().isWindows) {
            installerOptions.addAll(listOf(
                "--win-per-user-install",
                "--win-dir-chooser",
                "--win-menu",
                "--win-shortcut"
            ))
        }
        else if (org.gradle.internal.os.OperatingSystem.current().isLinux) {
            installerOptions.add("--linux-shortcut")
        }
        else if (org.gradle.internal.os.OperatingSystem.current().isMacOsX) {
            installerOptions.addAll(listOf("--mac-package-name", appName))
        }

        val iconPath = file(icoPath).takeIf { it.exists() }
        iconPath?.absolutePath?.also { icon = it }

        imageOptions.addAll(listOf("--vendor", companyName))

        installerOptions.addAll(listOf(
            "--vendor", companyName,
            "--app-version", version.toString()
        ))
    }
}
