---
description: About the JarHC project, goals, license, source code, how to contribute, and how to build it from source.
sources:
  - jarhc/build.gradle.kts
last_reviewed: 2026-06-28
---

# Project

## Requirements

* JarHC requires at least Java 11.
* JarHC has been tested with Java 11, Java 17, Java 21, and Java 25.

Note: JarHC is able to analyze Java classes compiled for Java 1.0 to Java 25, independent of which Java version is used to run JarHC.

## License

JarHC is released under the [Apache License version 2](http://www.apache.org/licenses/).

## Dependencies

### [ASM](https://asm.ow2.io/)

ASM is an all-purpose Java bytecode manipulation and analysis framework.

License: [3-Clause BSD License](https://asm.ow2.io/license.html).

### [org.json / JSON In Java](https://github.com/stleary/JSON-java)

The JSON-Java package is a reference implementation that demonstrates how to parse JSON documents into Java objects and how to generate new JSON documents from the Java classes.

License: [The JSON License](http://json.org/license.html).

### [Maven Artifact Resolver](https://maven.apache.org/resolver/)

Apache Maven Artifact Resolver is a library for working with artifact repositories and dependency resolution.

License: [Apache License version 2](http://www.apache.org/licenses/).

### [SLF4J](https://www.slf4j.org)

The Simple Logging Facade for Java (SLF4J) serves as a simple facade or abstraction for various logging frameworks (e.g. java.util.logging, logback, log4j) allowing the end user to plug in the desired logging framework at deployment time.

License: [MIT License](https://www.slf4j.org/license.html).

### [Open Source Insights](https://deps.dev/) (API)

Open Source Insights is a service developed and hosted by Google to help developers better understand the structure, construction, and security of open source software packages.

## Developers

Stephan Markwalder - [@smarkwal](https://twitter.com/smarkwal)

## Ideas

See GitHub issues:

* [Features](https://github.com/smarkwal/jarhc/issues?q=is%3Aopen+is%3Aissue+label%3Afeature)
* [Bugs](https://github.com/smarkwal/jarhc/issues?q=is%3Aopen+is%3Aissue+label%3Abug)
* [Tech Debt](https://github.com/smarkwal/jarhc/issues?q=is%3Aissue+is%3Aopen+label%3Atechdebt)
* [Documentation](https://github.com/smarkwal/jarhc/issues?q=is%3Aissue+is%3Aopen+label%3Adoc)

## Development

### Build

Building JarHC with Gradle requires at least Java 17.

Check if Java is installed and which version is used by default:

```shell
java -version
```

If needed, set `JAVA_HOME` to an installation of Java 17 or greater:

```shell
JAVA_HOME=/opt/java/jdk-17
```

Then, run a full build with Gradle Wrapper:

```shell
./gradlew :jarhc:build
```

To run a build without tests:

```shell
./gradlew :jarhc:build -Pskip.tests
```

To run the release tests:

```shell
./gradlew :jarhc-release-tests:test
```

### Sonar analysis

Run a full build first (see above).

Next, set your [Sonar token for SonarCloud](https://sonarcloud.io/account/security/).

```shell
export SONAR_TOKEN=your-token-here
```

Finally, run the Sonar analysis.

```shell
./gradlew sonar
```

Scan results can be found in [SonarCloud](https://sonarcloud.io/project/overview?id=smarkwal_jarhc).

### Locking dependencies

#### Gradle

To lock the Gradle dependencies of all projects (root project and subprojects) to
their current versions, run:

```shell
./gradlew updateGradleLockfiles --write-locks
```

The `updateGradleLockfiles` task resolves the dependencies and buildscript
classpaths of every project, so new subprojects are covered automatically without
having to be listed. The `--write-locks` flag is required; the task fails fast if
it is missing.

See [Gradle documentation](https://docs.gradle.org/current/userguide/dependency_locking.html) for more information.

#### Python (documentation)

The documentation toolchain pins its Python dependencies in a hash-pinned lockfile.
The top-level dependencies are declared in `requirements.in`; the fully resolved
lockfile `requirements.txt` (every transitive dependency at an exact version plus
a SHA-256 hash) is generated from it with `pip-compile` (pip-tools).

After editing `requirements.in` — or to refresh the pinned versions — regenerate
the lockfile with a Python container (no local Python installation required):

```shell
docker run --rm -v "$PWD/website":/work -w /work python:3.12-slim \
  sh -c "pip install -q pip-tools && \
         pip-compile --generate-hashes --output-file=requirements.txt requirements.in"
```

Python is pinned to 3.12 to match the version used by the documentation workflows.
Dependabot recognises the `pip-compile` lockfile and regenerates it (with updated
hashes) when it bumps a dependency, so routine updates do not need this command.

#### GitHub Actions

The GitHub Actions used in the workflows are pinned to a specific commit SHA
rather than a version tag, so that a moved or re-pointed tag cannot change which
code runs. The corresponding version is noted in a comment on the same line:

```yaml
uses: actions/checkout@<commit-sha> # v7.0.0
```

These pins are kept up to date by Dependabot (see [`.github/dependabot.yml`](https://github.com/smarkwal/jarhc/blob/main/.github/dependabot.yml)).

### Documentation

The documentation is automatically built and published to GitHub Pages:

1. On every push to the `main` branch by the GitHub workflow [Docs Snapshot](https://github.com/smarkwal/jarhc/blob/main/.github/workflows/docs-snapshot.yml).
2. On every release by the GitHub workflow [Docs Release](https://github.com/smarkwal/jarhc/blob/main/.github/workflows/docs-release.yml).

To test the documentation locally before publishing, run:

```shell
docker run --rm -it -p 8000:8000 -v "$PWD/website":/docs squidfunk/mkdocs-material serve -a 0.0.0.0:8000
```

And then visit [http://localhost:8000](http://localhost:8000) in your browser.

To build and inspect the documentation locally, run:

```shell
docker run --rm -v "$PWD/website":/docs squidfunk/mkdocs-material build --strict
```

And then open the generated file `website/site/index.html` in your browser.
