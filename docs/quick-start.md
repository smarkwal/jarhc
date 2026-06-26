---
description: Get started with JarHC in minutes, download the JAR, run a health check on your classpath, and read your first report.
sources:
  - jarhc/src/main/java/org/jarhc/app/CommandLineParser.java
last_reviewed: 2026-06-25
---

# Quick-start

## Prepare

Check whether Java 11, 17, 21, or 25 is installed:

```shell
java -version
```

Download the file `jarhc-<version>-app.jar` from the [latest release of JarHC](https://github.com/smarkwal/jarhc/releases/latest) and rename it to `jarhc.jar`.

Check whether JarHC starts:

```shell
java -jar jarhc.jar --version
```

## Analyze

Run a JarHC analysis:

```shell
java -jar jarhc.jar \
     --classpath <artifact> \
     --provided <artifact> \
     --runtime <artifact> \
     --sections "-jm,m,ob" \
     --ignore-missing-annotations \
     --title "Title" \
     --output report.html
```

## Compare

Compare two JarHC reports:

```shell
java -jar jarhc.jar \
     --diff report-v1.html report-v2.html \
     --title "Compare V1 and V2" \
     --output report-diff.html
```
