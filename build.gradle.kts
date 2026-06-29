plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.0.21"
    id("org.jetbrains.intellij.platform") version "2.2.1"
}

group = "com.example.rainbow"
version = "1.0.0"

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
