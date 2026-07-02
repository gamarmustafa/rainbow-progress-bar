# 🌈 Sassy Progress Bar

An Android Studio / IntelliJ plugin that replaces the stock progress bars with
animated scrolling rainbows, topped with a marquee of quirky one-liners
("DOWNLOADING MORE RAM", "ALMOST THERE (TOTALLY LYING)", …). The phrases are
editable in *Settings → Appearance & Behavior → Sassy Progress Bar*.

It hooks every `JProgressBar` in the IDE: the status-bar progress, indexing,
Gradle sync, dialogs, etc.

## How it works

Swing picks a progress bar's look from the UI class registered under the
`"ProgressBarUI"` key in `UIManager`. On startup (and after any theme change) the
plugin points that key at [`RainbowProgressBarUI`](src/main/java/com/example/rainbow/RainbowProgressBarUI.java),
which extends the platform's `DarculaProgressBarUI` and overrides
`paintDeterminate` / `paintIndeterminate` to draw a repeating rainbow gradient.
The indeterminate animation reuses Darcula's built-in animator, so the rainbow
scrolls smoothly.

- [`RainbowProgressBars`](src/main/kotlin/com/example/rainbow/RainbowProgressBars.kt) — installs the UI and refreshes existing bars.
- [`RainbowProgressBarActivator`](src/main/kotlin/com/example/rainbow/RainbowProgressBarActivator.kt) — installs on startup and re-installs on Look-and-Feel changes.

## Build

```bash
./gradlew buildPlugin
```

The installable zip lands in `build/distributions/progress-bar-<branch>.zip`
(the version is taken from the current git branch name).

The build requires a **JDK 21** to run Gradle and a matching toolchain to compile
(the Foojay resolver in `settings.gradle.kts` can auto-download the toolchain).
If your default `java` is newer than what Gradle 8.10.2 supports, point Gradle at
a JDK 21:

```bash
JAVA_HOME=/path/to/jdk-21 ./gradlew buildPlugin
```

## Run it in a sandbox IDE

```bash
./gradlew runIde
```

This launches a throwaway Android Studio with the plugin installed. Trigger any
long task (open a project, run a Gradle sync) to watch the rainbow.

## Install into your own Android Studio

The easiest way is straight from the
[JetBrains Marketplace](https://plugins.jetbrains.com/plugin/32573-sassy-progress-bar):
in the IDE, `Settings → Plugins → Marketplace`, search for **Sassy Progress
Bar**, and install.

To install a locally built zip instead:
`Settings → Plugins → ⚙ → Install Plugin from Disk…` and pick the built zip.

## ⚠️ Matching your Android Studio version

`build.gradle.kts` pins the build to a specific Android Studio release:

```kotlin
androidStudio("2024.2.2.13")
```

Change this to the build you actually run (`Help → About → "Build #AI-…"`), or
build against your local install instead:

```kotlin
local("/Applications/Android Studio.app/Contents")
```

`sinceBuild` is set to `242` with no upper bound, so the plugin will load in that
build and newer.
