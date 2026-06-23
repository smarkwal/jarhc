# Blacklist

Reports use of dangerous, unsafe, unstable, or deprecated classes and methods:

* `sun.misc.Unsafe`
* `System.exit(...)`, `Runtime.exit(...)`, or `Runtime.halt(...)`
* `System.load(...)`, `System.loadLibrary(...)`, `Runtime.load(...)`, or `Runtime.loadLibrary(...)`
* `Runtime.exec(...)`
* `@Deprecated`, `@VisibleForTesting`, `@Beta`, `@DoNotCall`,

Checks for executable files bundled as resources:

* `*.dll`
* `*.exe`
* `*.so`
* `*.bat`
* `*.sh`

[![Blacklist](../assets/images/report-section-blacklist.png)](../examples/asm/report.html#Blacklist){target="_blank" rel="noopener"}

Next: [JAR Manifests](jar-manifests.md)
