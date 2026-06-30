plugins {
    // Lets Gradle download a matching JDK for the configured toolchain (JDK 21)
    // if one isn't already installed.
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "rainbow-progress-bar"
