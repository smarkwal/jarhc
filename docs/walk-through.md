# Walk-through

### Prepare

Make sure that you have Java 11, 17, or 21 installed:

```
java -version
```

With [OpenJDK 17 from Eclipse Temurin](https://adoptium.net/de/temurin/releases/?version=17), it should print something like this:
```
openjdk 17.0.12 2024-07-16
OpenJDK Runtime Environment Temurin-17.0.12+7 (build 17.0.12+7)
OpenJDK 64-Bit Server VM Temurin-17.0.12+7 (build 17.0.12+7, mixed mode, sharing)
```

### Download and test

Download the file **jarhc-\<version\>-app.jar** from the [latest release of JarHC](https://github.com/smarkwal/jarhc/releases/latest) and rename it to **jarhc.jar**.

Run the following command to test if JarHC can be started:

```
java -jar jarhc.jar --version
```

It should print the current version of JarHC, for example:

```
JarHC - JAR Health Check 3.0.0
```

### Your first analysis

Let's analyze the open-source library **ASM Commons 7.0** which is one artifact of [ASM](https://asm.ow2.io/).

The official Maven artifact coordinates for ASM Commons 7.0 is `org.ow2.asm:asm-commons:7.0`.
Pass this to JarHC:

```
java -jar jarhc.jar org.ow2.asm:asm-commons:7.0
```

The output is text report with over 500 lines, starting like this:

```
JarHC - JAR Health Check 3.0.0
==============================

Load JAR files ...
Scan JAR files ...
Analyze classpath ...
Create report ...

JAR Health Check Report
=======================

JAR Files
---------
List of JAR files found in classpath.

Artifact    | Version | Source                      | Size    | Multi-release | Java version (classes) | Resources | Packages                  | Checksum (SHA-1)                         | Coordinates                 | Issues
------------+---------+-----------------------------+---------+---------------+------------------------+-----------+---------------------------+------------------------------------------+-----------------------------+-------
asm-commons | 7.0     | org.ow2.asm:asm-commons:7.0 | 78.0 KB | No            | Java 5 (32)            | 0         | org.objectweb.asm.commons | 478006d07b7c561ae3a92ddc1829bca81ae0cdd1 | org.ow2.asm:asm-commons:7.0 |
Classpath   | -       | -                           | 78.0 KB | -             | Java 5 (32)            | 0         | 1                         | -                                        | -                           | -

[...]
```

### Save the report as file

Use the option `--output <file>` to store the report in a file.

You can choose to generate an **HTML report**, a **text report**, or a **JSON report**.

* If you give the file a `.html` extension, the report will be in HTML format. 
HTML reports are usually easier to read for humans.
* If you give the file a `.txt` extension, you will get a text report like above.
* If you give the file a `.json` extension, you will get a report in JSON format.

You can also override the default report title "JAR Health Check Report" using the option `--title <text>`. 
Note that you will have to wrap the title in quotes if it contains spaces or other special characters.

So, to generate an HTML report for ASM Commons 7.0:
```
java -jar jarhc.jar org.ow2.asm:asm-commons:7.0 --output asm-commons-7.0.html --title "ASM Commons 7.0"
```

You can look at the result here: [asm-commons-7.0.html](examples/asm-commons-7.0/report.html)

### Add more artifacts

Many of the sections in the above HTML report are rather short. 
Not so the section ["Binary Compatibility"](examples/asm-commons-7.0/report.html#BinaryCompatibility).
This section is full of issues like these:

* `Interface not found: org.objectweb.asm.Opcodes`
* `Superclass not found: org.objectweb.asm.MethodVisitor`
* `Class not found: org.objectweb.asm.ClassVisitor`
* and many more ...

What JarHC is telling you here is that the Java classes in `asm-commons-7.0.jar` contain references to these classes, but these classes are not included in ASM Commons 7.0.

The explanation for this is found in section ["Dependencies"](examples/asm-commons-7.0/report.html#Dependencies):

```
Dependencies
------------
Dependencies as declared in POM file.

JAR file            | Maven coordinates           | Direct dependencies          | Status
--------------------+-----------------------------+------------------------------+------------
asm-commons-7.0.jar | org.ow2.asm:asm-commons:7.0 | org.ow2.asm:asm:7.0          | Unsatisfied
                    |                             | org.ow2.asm:asm-tree:7.0     | Unsatisfied
                    |                             | org.ow2.asm:asm-analysis:7.0 | Unsatisfied
```

ASM Commons has dependencies on three more AMS libraries:

* **ASM 7.0** with coordinates `org.ow2.asm:asm:7.0`
* **ASM Tree 7.0** with coordinates `org.ow2.asm:asm-tree:7.0`
* **ASM Analysis 7.0** with coordinates `org.ow2.asm:asm-analysis:7.0`

The values "Unsatisfied" in column "Status" tell you that these artifacts have not been found on the classpath by JarHC.
Since these artifacts are not present in the analysis, the report lists a lot of issues due to missing classes.

You can fix this by adding these artifacts to the analysis as well. There are two ways to do this:

* Add them to the "classpath", which means that their content is also analyzed by JarHC, and issues found in them will also be included in the report.
* Add them as "provided" artifacts, which means that their content will not be analyzed by JarHC. They will only be used to validate the references in ASM Commons 7.0.

#### Add artifacts to classpath

To add artifacts to the classpath, just add their coordinates to your JarHC command:
```
java -jar jarhc.jar org.ow2.asm:asm-commons:7.0 org.ow2.asm:asm:7.0 org.ow2.asm:asm-tree:7.0 org.ow2.asm:asm-analysis:7.0
```

The [HTML report](examples/asm-7.0/report.html) now contains an empty section "Binary Compatibility" (no issues found).

#### Add artifacts as provided

To add artifacts only as provided libraries, use the option `--provided <artifact>`:
```
java -jar jarhc.jar org.ow2.asm:asm-commons:7.0 --provided org.ow2.asm:asm:7.0 --provided org.ow2.asm:asm-tree:7.0 --provided org.ow2.asm:asm-analysis:7.0
```

You can make the command line a little shorter if you pass all provided artifacts as comma-separated list:
```
java -jar jarhc.jar org.ow2.asm:asm-commons:7.0 --provided org.ow2.asm:asm:7.0,org.ow2.asm:asm-tree:7.0,org.ow2.asm:asm-analysis:7.0
```

The [HTML report](examples/asm-7.0-provided/report.html) looks very similar, except that all the sections only list issues found for ASM Commons 7.0.

#### Classpath vs. provided

To be continued ...
