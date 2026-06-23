# Duplicate Classes

* Java classes found in more than one JAR file of the classpath.
* Java classes found in classpath shadowing provided or runtime Java classes (JRE/JDK).
* Resources (files other than Java class files) found in more than one JAR file under the exact same path and name.

Also checks how "similar" duplicate and shadowed Java classes are (exact copy, same API, or different API).

[![Duplicate Classes](../assets/images/report-section-duplicate-classes.png)](../examples/asm/report.html#DuplicateClasses){target="_blank" rel="noopener"}

Next: [Binary Compatibility](binary-compatibility.md)
