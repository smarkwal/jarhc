---
sources:
  - jarhc/src/main/java/org/jarhc/analyzer/OSGiBundlesAnalyzer.java
last_reviewed: 2026-06-25
---

# OSGi Bundles

Lists the OSGi bundle metadata of each artifact that is an OSGi bundle, with one
row per bundle. The metadata is read from the OSGi headers in the
`META-INF/MANIFEST.MF` file. Artifacts that are not OSGi bundles are not listed.

The table contains the following columns:

**Artifact**

The name of the artifact.

**Name**

The bundle name (`Bundle-Name`). If the bundle also has a symbolic name
(`Bundle-SymbolicName`) that differs from the bundle name, it is shown on a
separate line labeled `Symbolic Name`. If only one of the two is present, that
value is shown.

**Version**

The bundle version (`Bundle-Version`).

**Description**

The bundle description (`Bundle-Description`), followed by its vendor
(`Bundle-Vendor`), license (`Bundle-License`), and documentation URL
(`Bundle-DocURL`) where present.

**Import Package**

The packages that the bundle imports (`Import-Package`). Packages imported
dynamically (`DynamicImport-Package`) are listed below, under a `Dynamic:`
heading.

**Export Package**

The packages that the bundle exports (`Export-Package`).

**Capabilities**

The capabilities that the bundle requires (`Require-Capability`), under a
`Required:` heading, and the capabilities it provides (`Provide-Capability`),
under a `Provided:` heading.

**Others**

Further bundle headers where present: the activator (`Bundle-Activator`),
activation policy (`Bundle-ActivationPolicy`), manifest version
(`Bundle-ManifestVersion`), private packages (`Private-Package`), included
resources (`Include-Resource`), and required execution environment
(`Bundle-RequiredExecutionEnvironment`).

The report currently shows only a subset of the available OSGi headers, chosen to
keep the table at a reasonable size.

**Example**

[![OSGi Bundles](../assets/images/report-section-osgi-bundles.png)](../examples/asm/report.html#OSGiBundles){target="_blank" rel="noopener"}

Next: [Java Runtime](java-runtime.md)
