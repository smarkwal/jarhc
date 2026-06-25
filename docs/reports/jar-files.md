---
sources:
  - jarhc/src/main/java/org/jarhc/analyzer/JarFilesAnalyzer.java
last_reviewed: 2026-06-25
---

# JAR Files

Lists all JAR files found on the classpath, one row per JAR file. For each JAR
file, the report shows its size, contents (Java classes, resources, and
packages), the Java bytecode versions of its classes, its SHA-1 checksum, and
the resolved Maven coordinates. Potential problems, such as split packages or
fat JARs, are reported in the last column.

The table contains the following columns:

**Artifact**

The name of the artifact, or the JAR file name if the artifact name is not known.

**Version**

The version of the artifact. Shows `[unknown]` if the version cannot be
determined.

**Source**

The origin of the JAR file. If Maven coordinates were passed on the command line,
they are shown as a link to Maven Central. Otherwise, the file name of the JAR
file is shown.

**Size**

The file size of the JAR file in a human-readable format.

**Multi-release**

Whether the JAR file is a multi-release JAR. Shows `No`, or `Yes` followed by the
additional Java releases it contains, for example `Yes (Java 17, Java 11)`.

**Java version (classes)**

The Java bytecode versions found in the regular class files, with the number of
classes per version, for example `Java 8 (123), Java 11 (4)`. This helps to
determine the minimum Java version required to run all classes. The `module-info`
and `package-info` classes are excluded. Shows `[no class files]` if the JAR file
contains no classes.

**Resources**

The number of resource files (non-class files) in the JAR file.

**Packages**

The Java packages contained in the JAR file. Packages are grouped by common
parent package to keep the list compact. The following notations are used:

* `org.example.foo`: A single package.
* `org.example.foo (+2 subpackages)`: The package itself plus the given number of
  subpackages.
* `org.example.foo.* (3 subpackages)`: The given number of subpackages under a
  common parent package that contains no classes of its own.

**Checksum (SHA-1)**

The SHA-1 checksum of the JAR file, linked to a search for this checksum on Maven
Central. Shows `[unknown]` if no checksum is available.

**Coordinates**

The Maven coordinates of the artifact whose SHA-1 checksum matches the JAR file,
shown as a link. If several artifacts share the same checksum, all of them are
listed. Shows `[unknown]` if no matching artifact is found, or `[error]` if the
lookup failed, for example because of a network timeout.

**Issues**

Potential problems detected for the JAR file:

* **Split Package**: A package in this JAR file is also present in at least one
  other JAR file on the classpath. The affected package is listed.
* **Fat JAR**: The JAR file contains classes from several unrelated root packages,
  which can indicate that multiple JAR files have been merged into a single "fat"
  or "uber" JAR. The differing root packages are listed.

The last row, labeled `Classpath`, summarizes the totals across all JAR files:
the combined file size, the aggregated Java version counts, the total number of
resources, and the total number of packages.

**Example**

[![JAR Files](../assets/images/report-section-jar-files.png)](../examples/asm/report.html#JARFiles){target="_blank" rel="noopener"}

Next: [Vulnerabilities](vulnerabilities.md)
