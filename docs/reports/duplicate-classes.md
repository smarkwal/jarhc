---
description: The JarHC Duplicate Classes report, classes and resources that exist in more than one JAR file on the classpath.
sources:
  - jarhc/src/main/java/org/jarhc/analyzer/DuplicateClassesAnalyzer.java
last_reviewed: 2026-06-25
---

# Duplicate Classes

Lists Java classes and resources that exist more than once on the classpath, with
one row per duplicated class or resource. The following cases are reported:

* A Java class found in more than one artifact.
* A Java class that shadows a class provided by a parent class loader, such as
  the Java runtime.
* A resource (a file other than a Java class file) found under the same path in
  more than one artifact.

For each entry, the report also shows how similar the duplicates are.

The table contains the following columns:

**Class/Resource**

The fully qualified name of the duplicated class, or the path of the duplicated
resource. Classes are listed first, followed by resources, each sorted by name.

**Sources**

The sources that contain the class or resource, one per line, sorted
alphabetically. For a class, each source shows the artifact name followed by its
class loader in parentheses, for example `commons-io (Classpath)`. A class
provided by a parent class loader, such as the Java runtime, is shown by its
class loader name only. For a resource, only the artifact name is shown.

**Similarity**

How similar the duplicates are.

For classes:

* `Exact copy`: The class files are byte-for-byte identical.
* `Same API`: The class files differ, but their non-private API (classes,
  methods, and fields) is identical.
* `Different API`: The class files expose different APIs. For each further
  source, a line indicates how similar its API is to the first, for example
  `(116/130 = 90% similar)`.

For resources:

* `Exact copy`: The files have identical content.
* `Different content`: The files differ in content.

**Example**

[![Duplicate Classes](../assets/images/report-section-duplicate-classes.png)](../examples/asm/report.html#DuplicateClasses){target="_blank" rel="noopener"}

Next: [Binary Compatibility](binary-compatibility.md)
