# Usage

JarHC is available as command line application:

```
java -jar jarhc-app.jar [options] <artifact> [<artifact>]*
```

### Arguments

`<artifact>` is an absolute or relative path to a JAR file, a WAR file, a JMOD file, a directory with JAR, WAR, and JMOD files,
Maven artifact coordinates of the form `"<GroupID>:<ArtifactID>:<Version>"`, or the name of an artifact collection.
In case of a directory, all files found in that directory and any subdirectories (recursive) are included in the analysis.
If `<artifact>` is a path to a WAR file, JarHC includes all JAR files from `/WEB-INF/lib` folder in the analysis.

### Options

#### Help

```
--help
--version
```

Print usage information or version information, and then exit.

#### Debug and trace output

```
--debug
--trace
```

Enable verbose output.

#### Report file path

```
--output <file>
```

Report file path. If this option is not present, the report is printed to STDOUT.
This option can be specified multiple times to generate multiple reports with different formats.
JarHC tries to guess the format based on the filename extension:

* `*.html` -> HTML report
* `*.txt` -> text report
* `*-list.txt` -> list report
* `*.json` -> JSON report

Example: `--output report.html --output report.txt --output report-list.txt --output report.json`

#### Report title

```
--title <title>
```

Report title.

Default: "JAR Health Check Report".

Example: `--title "MyApp 1.0"`

#### Report sections

```
--sections <sections>
```

List of sections to include in the report.

Default: [none] (include all sections).

Example: `--sections "jf,d"`

If the list of sections is prefixed with '-' the given sections are excluded. Example: `--sections "-jm,m,ob,jr"`

Sections:

* `jf` - JAR Files
* `d` - Dependencies
* `dc` - Duplicate Classes
* `bc` - Binary Compatibility
* `bl` - Blacklist
* `jm` - JAR Manifests
* `m` - JPMS Modules
* `ob` - OSGi Bundles
* `jr` - Java Runtime

#### Skip empty sections

```
--skip-empty
```

Empty sections will not be included in the report.

Example:
If an analysis shows that there are no duplicate classes and resources, the section "Duplicate Classes" is not included in the report.

This option mainly applies to the sections listing issues:

* Duplicate Classes
* Binary Compatibility
* Blacklist

Other sections are never empty.

#### Release

```
--release <number>
```

Specify the Java release used when loading classes and resources from multi-release JAR files.

The value for this option must be a Java major version number greater or equal to 8.

Default: Major version of Java runtime used to run JarHC.

Example: `--release 11`

#### Classpath

```
--classpath <artifact>[,<artifact>]*
```

Instead of passing Java artifacts as arguments, you can also use the option "--classpath".
As for arguments, this option supports passing JAR files, WAR files, JMOD files, Maven artifact coordinates, or artifact collections.
This option can also be used multiple times to add multiple Java artifacts.

Example: `--classpath myapp-1.0.jar,mylib-1.0.jar,libs`

#### Provided and runtime classpath

```
--provided <artifact>[,<artifact>]*
--runtime <artifact>[,<artifact>]*
```

Specify additional paths to JAR files or directories with JAR files handled as "provided" or "runtime" (JDK/JRE) libraries. Those JAR files are not analyzed, but references to them are validated.

The value for these options can be a single JAR file, a single directory, or a comma-separated list of JAR files and/or directories. These options can also be used multiple times to add multiple JAR files or directories.

Example: `--provided servlet-api-3.0.jar,jsp-api-3.0.jar --runtime $JAVA_HOME/jre/lib`

#### Class loading strategy

```
--strategy ParentLast|ParentFirst
```

Define the strategy when searching for classes on the classpath.

With "ParentFirst", JarHC will first try to find the class on the runtime classpath, then on the provided classpath, and finally on the normal classpath.

With "ParentLast", JarHC will first try to find the class on the normal classpath, then on the provided classpath, and finally on the runtime classpath.

Default: "ParentLast"

Example: `--strategy ParentFirst`

#### Isolated scan

```
--isolated-scan
```

Scan all artifacts on the classpath in isolation. Artifacts cannot "see" each other.
This mode is useful when JAR files are self-contained and do not depend on each other, for example when scanning a set of plugins of a framework.
The framework artifacts are usually specified as "provided".

#### Custom Maven repository

```
--repository-url <url>
--repository-username <username>
--repository-password <password>
```

Specify a custom Maven repository URL. Username and password are optional.
These values can also be set in a JarHC Properties file (see below).

#### Ignore missing annotations

```
--ignore-missing-annotations
```

Binary Compatibility will not report an issue if no class definition is found for an annotation.

This can be used to suppress warnings if libraries make heavy use of annotations for code analysis like @NonNull or @SuppressFBWarnings.

#### Ignore exact copy

```
--ignore-exact-copy
```

Duplicate Classes section does not list classes and resources which are identical.

#### Path to cache directory

```
--data <path>
```

Specify the path to a local data directory used to cache information about artifacts.
The special value "TEMP" can be used to create a temporary data directory.
If this option is not present, JarHC will check if environment variable `JARHC_DATA` is set and use its value.

Default: "~/.jarhc"

Example: `--data /tmp/jarhc` or `export JARHC_DATA=/tmp/jarhc`

If the directory does not exist, it is automatically created.

### Load options from a file

```
--options <path>
```

Load additional options from a file.
The file must contain one option per line.
Empty lines and lines starting with '#' are ignored.
Use '${...}' to reference environment variables.

Example options file:

```
--title MyApp 1.0
--classpath myapp-1.0.jar,mylib-1.0.jar,libs
--release 8
--runtime ${JDK_8_HOME}/jre/lib
--output report.html
```

### JarHC Properties files

Some JarHC options can be set in a JarHC Properties file. JarHC automatically loads properties from the following paths:

* `./jarhc.properties`
* `~/.jarhc/jarhc.properties`

Supported properties:

* `repository.url`
* `repository.username`
* `repository.password`
* `collection.<name>` (custom artifact collections, see below)

Example file:

```properties
repository.url=https://repo.example.com/maven
repository.username=read-only
repository.password=secret
```

### Artifact collections

An artifact collection is a named list for one or more Java artifacts.
It is a kind of "alias" for a frequently used set of artifacts.

Custom collections can be defined in JarHC Properties files:

```properties
collection.<name>=<artifact>[,<artifact>]*
```

Example file:

```properties
collection.servlet-6.0=jakarta.servlet:jakarta.servlet-api:6.0.0
collection.slf4j=org.slf4j:slf4j-api:2.0.17,org.slf4j:slf4j-simple:2.0.17
collection.jdk-8=/opt/jdk-8/jre/lib/rt.jar
```

Such collections can then be used as arguments to JarHC:

```shell
java -jar jarhc-app.jar --classpath servlet-6.0 --provided slf4j --runtime jdk-8 [...]
```

Every collection name will be replaced with the list of artifacts in that collection.

### Java System Properties

A set of low-level options can be set using Java System Properties.

#### HTTP client for deps.dev API

URL pattern with `%s` as placeholder for URL-encoded base64 checksum:

```properties
jarhc.search.url=https://api.deps.dev/v3/query?hash.type=SHA1&hash.value=%s
```

Timeout in seconds:

```properties
jarhc.search.timeout=30
```

HTTP request header "User-Agent":

```properties
jarhc.search.headers.User-Agent=Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:129.0) Gecko/20100101 Firefox/129.0
```

The above default User-Agent value is from a Firefox browser on Ubuntu Linux in September 2024.

Additional HTTP request headers:

```properties
jarhc.search.headers.<NAME>=<VALUE>
```
