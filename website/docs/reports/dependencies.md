---
description: The JarHC Dependencies report, direct and transitive dependencies between JAR files, and any missing or unsatisfied dependencies.
sources:
  - jarhc/src/main/java/org/jarhc/analyzer/DependenciesAnalyzer.java
last_reviewed: 2026-06-25
---

# Dependencies

For each artifact on the classpath, this section shows which other artifacts it
uses and which artifacts use it, its Maven coordinates, the available updates,
and its declared direct dependencies. There is one row per artifact. The "Uses"
and "Used by" relationships are based on the actual usage of classes in the
bytecode, not on declared dependencies.

The table contains the following columns:

**Artifact**

The name of the artifact, or the JAR file name if the artifact name is not known.

**Uses**

The other artifacts on the classpath whose classes are used by this artifact, one
per line. References to classes within the same artifact are ignored. Shows
`[none]` if this artifact does not use any other artifact on the classpath.

**Used by**

The other artifacts on the classpath that use classes from this artifact, one per
line. Shows `[none]` if no other artifact on the classpath uses it.

**Maven coordinates**

The Maven coordinates of the artifact, shown as a link. Shows `[unknown]` if no
coordinates could be determined, or `[error]` if the lookup failed, for example
because of a network timeout.

**Updates**

Newer stable versions of the artifact that are available, grouped by minor
version with one line per minor version and each version shown as a link. Long
lists are abbreviated with a `[...]` marker. Shows `[none]` if the artifact is
already up to date, `[unknown]` if no version information is available, or
`[error]` if the lookup failed.

**Direct dependencies**

The direct dependencies declared in the artifact's POM file, excluding test
dependencies. Each dependency is shown as a link, followed by its status on the
classpath:

* `[OK]`: A matching artifact is present on the classpath. Additional details may
  follow: `(version x.y.z)` if the version on the classpath differs from the
  declared version, the actual coordinates in parentheses if the dependency is
  satisfied by an artifact with a different group or artifact ID, or the name of
  the responsible class loader in square brackets if it is not the default
  classpath.
* `[not found]`: No matching artifact is present on the classpath.

Shows `[none]` if the artifact declares no direct dependencies, `[unknown]` if its
coordinates could not be determined, or `[error]` if the dependencies could not be
resolved.

**Example**

[![Dependencies](../assets/images/report-section-dependencies.png)](../examples/asm/report.html#Dependencies){target="_blank" rel="noopener"}

Next: [Duplicate Classes](duplicate-classes.md)
