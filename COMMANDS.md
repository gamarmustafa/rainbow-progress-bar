# Commands

Quick reference for working on the Rainbow Progress Bar plugin.

## See it in action (the one you usually want)

```bash
./gradlew runIde
```

Launches a separate **sandbox Android Studio** with the plugin already loaded.
Open a project or trigger a Gradle sync in that window and the progress bars
turn into rainbows. Nothing gets installed into your real IDE — close the
sandbox window when you're done.

> Tip: you can also run this from the **Gradle tool window** (right edge) →
> `rainbow-progress-bar → Tasks → intellij platform → runIde`.

## Build the installable plugin zip

```bash
./gradlew buildPlugin
```

Produces `build/distributions/progress-bar-<branch>.zip` (the version is the
current git branch name, e.g. `progress-bar-main.zip`).
Install it via **Settings → Plugins → ⚙️ → Install Plugin from Disk…**, pick the
zip, then restart.

## Other useful tasks

```bash
./gradlew clean          # delete build/ output
./gradlew verifyPlugin   # check the plugin against IDE compatibility rules
./gradlew tasks          # list all available tasks
```

## Why "Build → Make Project" in the IDE seems to do nothing

That menu only compiles classes into `build/`. It does **not** start a second
IDE or load the plugin anywhere, so there's no visible effect. The plugin only
comes alive inside a running IDE instance — use `runIde` for that.

## Notes

- The build runs on your installed JDK 26 via Gradle 9.6.1; a JDK 21 toolchain
  is auto-provisioned for compilation. No `JAVA_HOME` tweaks needed.
- First run after a clean is slow (downloads/extracts the IDE SDK and toolchain);
  later runs are cached and fast.
- If your Android Studio build differs from the pinned one, see the version note
  in [README.md](README.md).
