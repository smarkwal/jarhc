# Shell scripts for testing

## Local post-build validation

Runs a set of simple tests with JarHC on different Java versions and installations:

* Run JarHC with option `--version`.
* Run JarHC with option `--help`.
* Run JarHC for ASM 9.2 artifact `org.ow2.asm:asm:9.2`.

The tests compare the output of JarHC with expected results stored in the "results" directory.

### Preparation

Build JarHC fat/uber JAR with all dependencies:

```shell
./gradlew clean jar-with-deps
```

List all Java installations in "java.txt".

Example file content:
```
/Library/Java/JavaVirtualMachines/jdk1.8.0_211.jdk/Contents/Home
/Library/Java/JavaVirtualMachines/jdk-11.0.6.jdk/Contents/Home
/Library/Java/JavaVirtualMachines/jdk-17.0.1.jdk/Contents/Home

```

**Attention:**
Make sure that there is an empty line at the end of the file.

### Execution

Run the following script:

`./post-build-validation.sh`

Example output:

```
Test JarHC 1.6
====================================================================
Java: 1.8.0_211 | Oracle Corporation | /Library/Java/JavaVirtualMachines/jdk1.8.0_211.jdk/Contents/Home/jre
PASSED - JarHC --version
PASSED - JarHC --help
PASSED - JarHC for ASM
====================================================================
Java: 11.0.6 | Oracle Corporation | /Library/Java/JavaVirtualMachines/jdk-11.0.6.jdk/Contents/Home
PASSED - JarHC --version
PASSED - JarHC --help
PASSED - JarHC for ASM
====================================================================
Java: 17.0.1 | Oracle Corporation | /Library/Java/JavaVirtualMachines/jdk-17.0.1.jdk/Contents/Home
PASSED - JarHC --version
PASSED - JarHC --help
PASSED - JarHC for ASM
====================================================================
PASSED - Total: 9, Passed: 9, Failed: 0, Errors: 0
```