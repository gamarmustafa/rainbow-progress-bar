plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.4.0"
    id("org.jetbrains.intellij.platform") version "2.10.0"
}

group = "com.example.rainbow"

// Version resolution:
//  - Release builds pass an explicit semver via `-PpluginVersion=1.0.0` (or the
//    PLUGIN_VERSION env var) -> artifact is progress-bar-1.0.0.zip.
//  - Local/dev builds fall back to the current git branch name
//    -> artifact is progress-bar-<branch>.zip (e.g. progress-bar-main.zip).
// providers.exec is configuration-cache friendly; "/" in branch names is
// replaced so the result is filename-safe.
val gitBranch: Provider<String> = providers.exec {
    commandLine("git", "rev-parse", "--abbrev-ref", "HEAD")
    isIgnoreExitValue = true
}.standardOutput.asText.map { it.trim().replace('/', '-') }.map { it.ifEmpty { "dev" } }

val releaseVersion: Provider<String> = providers.gradleProperty("pluginVersion")
    .orElse(providers.environmentVariable("PLUGIN_VERSION"))

version = releaseVersion.orElse(gitBranch).getOrElse("dev")

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        // Build against Android Studio. Change this to the build of Android Studio
        // you actually have installed (Help > About > "Build #AI-...").
        // You can list available versions or, more reliably, point at your local
        // install instead, e.g.:
        //   local("/Applications/Android Studio.app/Contents")
        androidStudio("2024.2.2.13")

        // If you'd rather build against IntelliJ IDEA Community (the plugin still
        // installs in Android Studio as long as the build range matches), use:
        //   intellijIdeaCommunity("2024.2.4")

        pluginVerifier()
        zipSigner()
    }
}

intellijPlatform {
    // Skip booting a headless IDE to index searchable options — it's the
    // slowest part of the build. The settings page is still reachable by its
    // display name in the Settings search; only its inner labels won't be.
    buildSearchableOptions = false

    pluginConfiguration {
        ideaVersion {
            sinceBuild = "242"
            untilBuild = provider { null } // no upper bound
        }
    }
}

kotlin {
    jvmToolchain(21)
}
