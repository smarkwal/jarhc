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

To run the benchmarks:

```shell
./gradlew :jarhc:jmh
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

Scan results can be found in SonarCloud:
https://sonarcloud.io/project/overview?id=smarkwal_jarhc

### Documentation

The documentation is automatically built and published to GitHub Pages:

1. On every push to the `master` branch by the GitHub workflow [Docs Snapshot](https://github.com/smarkwal/jarhc/blob/master/.github/workflows/docs-snapshot.yml).
2. On every release by the GitHub workflow [Docs Release](https://github.com/smarkwal/jarhc/blob/master/.github/workflows/docs-release.yml).

To test the documentation locally before publishing, run:

```shell
docker run --rm -it -p 8000:8000 -v "$PWD":/docs squidfunk/mkdocs-material serve -a 0.0.0.0:8000
```

And then visit http://0.0.0.0:8000/jarhc/ in your browser.

To build and inspect the documentation locally, run:

```shell
docker run --rm -v "$PWD":/docs squidfunk/mkdocs-material build
```

And then open the generated file `site/index.html` in your browser.
