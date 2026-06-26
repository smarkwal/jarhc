---
description: Run JarHC as part of your Gradle build using the official JarHC Gradle plugin.
last_reviewed: 2026-06-25
---

# Gradle Plugin

JarHC is available as a Gradle plugin. It can be used to analyze the dependencies of a Gradle project.

```kotlin
plugins {
    id("org.jarhc") version "1.2.0"
}
```

The plugin adds a task `jarhcReport` to the project.

The plugin is published on the [Gradle Plugin Portal](https://plugins.gradle.org/plugin/org.jarhc).
For more information, see the [JarHC Gradle Plugin project on GitHub](https://github.com/smarkwal/jarhc-gradle-plugin).
