---
sources:
  - jarhc/src/main/java/org/jarhc/analyzer/JpmsModulesAnalyzer.java
last_reviewed: 2026-06-25
---

# JPMS Modules

Lists the Java Platform Module System (JPMS, see [JEP 261](https://openjdk.org/jeps/261)) module of each artifact on the
classpath, with one row per artifact.

The table contains the following columns:

**Artifact**

The name of the artifact.

**Module name**

The name of the Java module. It is taken from the module descriptor or the
manifest, or derived from the JAR file name using the standard naming algorithm
if neither is available.

**Definition**

Where the module definition comes from:

* `Module-Info`: An explicit module descriptor (`module-info.class`).
* `Manifest`: The `Automatic-Module-Name` attribute in the manifest.
* `Auto-generated`: Derived from the JAR file name, because the artifact has
  neither a module descriptor nor the manifest attribute.

**Automatic**

Whether the module is an automatic module. Shows `No` for a module with an
explicit descriptor, and `Yes` otherwise.

**Requires**

The modules that this module requires, one per line. This is empty for automatic
and auto-generated modules, which do not declare dependencies.

**Exports**

The packages that this module exports, one per line. Shows `[all packages]` for
automatic and auto-generated modules, which export all of their packages.

**Example**

[![JPMS Modules](../assets/images/report-section-jpms-modules.png)](../examples/asm/report.html#JPMSModules){target="_blank" rel="noopener"}

Next: [OSGi Bundles](osgi-bundles.md)
