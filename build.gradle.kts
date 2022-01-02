import com.github.jk1.license.render.CsvReportRenderer
import com.github.jk1.license.render.InventoryHtmlReportRenderer
import com.github.jk1.license.render.InventoryMarkdownReportRenderer
import com.github.jk1.license.render.XmlReportRenderer
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.gradle.plugins.ide.idea.model.IdeaLanguageLevel
import org.owasp.dependencycheck.reporting.ReportGenerator.Format
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

plugins {
    java
    jacoco
    id("com.github.jk1.dependency-license-report") version "2.0"
    id("org.ajoberstar.grgit") version "4.1.1"
    id("org.kordamp.gradle.source-xref") version "0.47.0"
    id("org.sonarqube") version "3.3"
    id("org.owasp.dependencycheck") version "6.5.1"
    id("com.dorongold.task-tree") version "2.1.0"
    `maven-publish`
    idea
}

// project settings ------------------------------------------------------------

group = "org.jarhc"
version = "1.6-SNAPSHOT"
description = "JarHC - JAR Health Check"

// Java version check ----------------------------------------------------------

// NOTE: Gradle build must run on Java 11, but code must be compiled for Java 8
if (!JavaVersion.current().isJava11Compatible) {
    val error = "Build requires Java 11 and does not run on Java ${JavaVersion.current().majorVersion}."
    throw GradleException(error)
}

// Preconditions based on which tasks should be executed -----------------------

gradle.taskGraph.whenReady {

    // if sonarqube task should be executed ...
    if (gradle.taskGraph.hasTask(":sonarqube")) {
        // environment variable SONAR_TOKEN or property "sonar.login" must be set
        val tokenFound = project.hasProperty("sonar.login") || System.getenv("SONAR_TOKEN") != null
        if (!tokenFound) {
            val error = "SonarQube: Token not found.\nPlease set property 'sonar.login' or environment variable 'SONAR_TOKEN'."
            throw GradleException(error)
        }
    }

}

// configuration properties ----------------------------------------------------

// flag to skip unit and integration tests
// command line option: -Pskip.tests
val skipTests: Boolean = project.hasProperty("skip.tests")

// select JMH benchmarks
// to run a single benchmark: -Pbenchmarks=DefaultJavaRuntimeBenchmark
val benchmarks: String = (project.properties["benchmarks"] ?: ".*") as String

// constants -------------------------------------------------------------------

val mainClassName: String = "org.jarhc.Main"
val buildTimestamp: String = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ssX").withZone(ZoneId.of("UTC")).format(Instant.now())
val licenseReportPath: String = "${buildDir}/reports/licenses"

// test results
val testReportPath: String = "${buildDir}/test-results/test"
val unitTestReportPath: String = "${buildDir}/test-results/unitTest"
val integrationTestReportPath: String = "${buildDir}/test-results/integrationTest"

// JaCoCo coverage report
val jacocoTestReportXml: String = "${buildDir}/reports/jacoco/test/report.xml"

// additional source sets and configurations -----------------------------------

sourceSets {

    create("unitTest") {
        compileClasspath += sourceSets.main.get().output + sourceSets.test.get().output
        runtimeClasspath += sourceSets.main.get().output + sourceSets.test.get().output
    }

    create("integrationTest") {
        compileClasspath += sourceSets.main.get().output + sourceSets.test.get().output
        runtimeClasspath += sourceSets.main.get().output + sourceSets.test.get().output
    }

    create("releaseTest") {
        // independent source set
    }

}

val unitTestImplementation: Configuration by configurations.getting {
    extendsFrom(
        configurations.implementation.get(),
        configurations.testImplementation.get()
    )
}

val unitTestRuntimeOnly: Configuration by configurations.getting {
    extendsFrom(
        configurations.runtimeOnly.get(),
        configurations.testRuntimeOnly.get()
    )
}

val unitTestAnnotationProcessor: Configuration by configurations.getting {
    extendsFrom(
        configurations.annotationProcessor.get(),
        configurations.testAnnotationProcessor.get()
    )
}

val integrationTestImplementation: Configuration by configurations.getting {
    extendsFrom(
        configurations.implementation.get(),
        configurations.testImplementation.get()
    )
}

val integrationTestRuntimeOnly: Configuration by configurations.getting {
    extendsFrom(
        configurations.runtimeOnly.get(),
        configurations.testRuntimeOnly.get()
    )
}

val releaseTestImplementation: Configuration by configurations.getting {
    // independent configuration
}

val releaseTestRuntimeOnly: Configuration by configurations.getting {
    // independent configuration
}

// dependencies ----------------------------------------------------------------

repositories {
    mavenLocal()
    maven {
        url = uri("https://repo.maven.apache.org/maven2/")
    }
}

dependencies {

    // primary dependencies
    implementation("org.ow2.asm:asm:9.2")
    implementation("org.json:json:20211205")
    implementation("org.eclipse.aether:aether-impl:1.1.0")
    implementation("org.eclipse.aether:aether-connector-basic:1.1.0")
    implementation("org.eclipse.aether:aether-transport-http:1.1.0")
    implementation("org.apache.maven:maven-aether-provider:3.3.9")
    implementation("org.slf4j:slf4j-api:1.7.32")
    runtimeOnly("org.slf4j:slf4j-simple:1.7.32")

    // fix vulnerabilities in transitive dependencies
    // fix CVE-2018-10237 and CVE-2020-8908
    implementation("com.google.guava:guava:31.0.1-jre")
    // fix CVE-2015-5262 and CVE-2020-13956
    implementation("org.apache.httpcomponents:httpclient:4.5.13")
    // fix https://github.com/codehaus-plexus/plexus-utils/issues/3
    implementation("org.codehaus.plexus:plexus-utils:3.0.24")

    // test dependencies (available in unit and integration tests)
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.2")
    testImplementation("org.mockito:mockito-core:4.2.0")

    // unit test dependencies
    // TODO: move JMH benchmarks in a separate source set?
    unitTestImplementation("org.openjdk.jmh:jmh-core:1.34")
    unitTestAnnotationProcessor("org.openjdk.jmh:jmh-generator-annprocess:1.34")

    // integration test dependencies
    // currently: none

    // release test dependencies
    releaseTestImplementation("org.junit.jupiter:junit-jupiter:5.8.2")
    releaseTestImplementation("org.junit.jupiter:junit-jupiter-params:5.8.2")
    releaseTestImplementation("org.assertj:assertj-core:3.21.0")
    releaseTestRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.2")
    releaseTestImplementation("org.testcontainers:testcontainers:1.16.2")
    releaseTestImplementation("org.testcontainers:junit-jupiter:1.16.2")
    releaseTestRuntimeOnly("org.slf4j:slf4j-api:1.7.32")
    releaseTestRuntimeOnly("org.slf4j:slf4j-simple:1.7.32")

}

// plugin configurations -------------------------------------------------------

// special settings for IntelliJ IDEA
idea {

    // NOTE: Gradle build must run on Java 11, but code must be compiled for Java 8
    project {
        jdkName = "11"
        languageLevel = IdeaLanguageLevel(JavaVersion.VERSION_1_8)
    }

    module {
        isDownloadJavadoc = true
        isDownloadSources = true
    }
}

licenseReport {
    outputDir = licenseReportPath
    renderers = arrayOf(
        InventoryHtmlReportRenderer("licenses.html"),
        XmlReportRenderer("licenses.xml"),
        CsvReportRenderer("licenses.csv"),
        InventoryMarkdownReportRenderer("licenses.md")
    )
}

java {

    // NOTE: Gradle build must run on Java 11, but code must be compiled for Java 8
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }

    // automatically package source code as artifact -sources.jar
    withSourcesJar()

    // automatically package Javadoc as artifact -javadoc.jar
    withJavadocJar()
}

jacoco {
    toolVersion = "0.8.7"
}

sonarqube {
    // documentation: https://docs.sonarqube.org/latest/analysis/scan/sonarscanner-for-gradle/

    properties {

        // connection to SonarCloud
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.organization", "smarkwal")
        property("sonar.projectKey", "smarkwal_jarhc")

        // Git branch
        property("sonar.branch.name", getGitBranchName())

        // include test results
        property("sonar.junit.reportPaths", "${testReportPath},${unitTestReportPath},${integrationTestReportPath}")

        // include test coverage results
        property("sonar.java.coveragePlugin", "jacoco")
        property("sonar.coverage.jacoco.xmlReportPaths", jacocoTestReportXml)
    }
}

dependencyCheck {
    // documentation: https://jeremylong.github.io/DependencyCheck/dependency-check-gradle/configuration.html

    // settings
    format = Format.ALL
    skipTestGroups = false
    outputDirectory = "${buildDir}/reports/dependency-check"

    // path to database directory
    data.directory = "${projectDir}/dependency-check"

    // disable .NET Assembly Analyzer (fix for unexpected build exception)
    analyzers.assemblyEnabled = false
}

config {
    docs {
        sourceXref {
            // documentation: https://kordamp.org/kordamp-gradle-plugins/#_org_kordamp_gradle_sourcexref
            windowTitle = "JarHC $version"
            docTitle = "JarHC $version"
        }
    }
}

publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
    }
}

// tasks -----------------------------------------------------------------------

tasks {

    processResources {

        // replace placeholders in resources
        // (see src/main/resources/jarhc.properties)
        expand(
            "version" to project.version,
            "timestamp" to buildTimestamp
        )
    }

    jar {

        // set Main-Class in MANIFEST.MF
        manifest {
            attributes["Main-Class"] = mainClassName
        }

        // add LICENSE to JAR file
        from("LICENSE")
    }

    test {
        // no special configuration
    }

    check {
        dependsOn(unitTest, integrationTest)
    }

    jacocoTestReport {

        // run all tests first
        dependsOn(test, unitTest, integrationTest)

        // get JaCoCo data from all test tasks
        executionData.from(
            "${buildDir}/jacoco/test.exec",
            "${buildDir}/jacoco/unitTest.exec",
            "${buildDir}/jacoco/integrationTest.exec"
        )

        reports {

            // generate XML report (required for SonarQube)
            xml.required.set(true)
            xml.outputLocation.set(file(jacocoTestReportXml))

            // generate HTML report
            html.required.set(true)

            // generate CSV report
            // csv.required.set(true)
        }
    }

    assemble {
        dependsOn(jarWithDeps, libsZip, testJar, testLibsZip, "sourceXrefJar")
    }

}

val jarWithDeps = task("jar-with-deps", type = Jar::class) {
    group = "build"
    description = "Assembles a fat/uber jar archive with all runtime dependencies."

    // make sure that license report has been generated
    dependsOn(tasks.generateLicenseReport)

    // append classifier "-with-deps"
    archiveClassifier.set("with-deps")

    // set Main-Class in MANIFEST.MF
    manifest {
        attributes["Main-Class"] = mainClassName
    }

    // include all files from all runtime dependencies
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })

    // include license report
    from(licenseReportPath) {
        into("META-INF/licenses")
    }

    exclude(

        // exclude module-info files
        "module-info.class",

        // exclude license files
        "META-INF/LICENSE", "META-INF/LICENSE.txt",
        "META-INF/NOTICE", "META-INF/NOTICE.txt",
        "META-INF/DEPENDENCIES",

        // exclude signature files
        "META-INF/*.RSA", "META-INF/*.SF", "META-INF/*.DSA"
    )

    // exclude duplicates
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE // alternative: WARN

    with(tasks.jar.get() as CopySpec)
}

val testJar = task("testJar", type = Jar::class) {
    group = "build"
    description = "Assembles a jar archive containing the test classes."

    // compile all test classes first
    dependsOn(
        tasks.testClasses,
        tasks["unitTestClasses"],
        tasks["integrationTestClasses"]
    )

    // append classifier "-tests"
    archiveClassifier.set("tests")

    // include compiled unit test classes and integration test classes
    from(
        sourceSets.test.get().output,
        sourceSets["unitTest"].output,
        sourceSets["integrationTest"].output
    )
}

val libsZip = task("libsZip", type = Zip::class) {
    group = "build"
    description = "Assembles a zip archive containing the runtime dependencies."

    // append classifier "-tests"
    archiveClassifier.set("libs")

    // create archive in "libs" folder (instead of "distributions")
    destinationDirectory.set(file("${buildDir}/libs"))

    // include all runtime dependencies
    from(configurations.runtimeClasspath)
}

val testLibsZip = task("testLibsZip", type = Zip::class) {
    group = "build"
    description = "Assembles a zip archive containing the test dependencies."

    // append classifier "-tests"
    archiveClassifier.set("test-libs")

    // create archive in "libs" folder (instead of "distributions")
    destinationDirectory.set(file("${buildDir}/libs"))

    // include all test dependencies and integration test dependencies
    from(
        configurations.testRuntimeClasspath,
        configurations["unitTestRuntimeClasspath"],
        configurations["integrationTestRuntimeClasspath"]
    )
}

val unitTest = task("unitTest", type = Test::class) {
    group = "verification"
    description = "Runs the unit test suite."

    // use tests in unitTest source set
    testClassesDirs = sourceSets["unitTest"].output.classesDirs
    classpath = sourceSets["unitTest"].runtimeClasspath

    // run tests in parallel
    // (currently, unitTest task is fastest with a single JVM and thread)
    // maxParallelForks = 4

    // run unit tests after "core" tests
    shouldRunAfter(tasks.test)
}

val integrationTest = task("integrationTest", type = Test::class) {
    group = "verification"
    description = "Runs the integration test suite."

    // use tests in integrationTest source set
    testClassesDirs = sourceSets["integrationTest"].output.classesDirs
    classpath = sourceSets["integrationTest"].runtimeClasspath

    // run tests in parallel in the same JVM (experimental feature or JUnit 5)
    // see https://junit.org/junit5/docs/current/user-guide/#writing-tests-parallel-execution
    systemProperty("junit.jupiter.execution.parallel.enabled", "true")
    // run test classes in parallel, but all test methods of a class by the same thread
    systemProperty("junit.jupiter.execution.parallel.mode.default", "same_thread")
    systemProperty("junit.jupiter.execution.parallel.mode.classes.default", "concurrent")
    // run max 4 test classes in parallel (more can result in an OutOfMemoryError)
    systemProperty("junit.jupiter.execution.parallel.config.strategy", "fixed")
    systemProperty("junit.jupiter.execution.parallel.config.fixed.parallelism", "4")

    // run integration tests after unit tests
    shouldRunAfter(unitTest)
}

val releaseTest = task("releaseTest", type = Test::class) {
    group = "verification"
    description = "Runs the release test suite."

    // use tests in releaseTest source set
    testClassesDirs = sourceSets["releaseTest"].output.classesDirs
    classpath = sourceSets["releaseTest"].runtimeClasspath

    // run release tests after fat/uber JAR has been built
    dependsOn(jarWithDeps)
}

// common settings for all test tasks
tasks.withType<Test> {

    // skip tests if property "skip.tests" is set
    onlyIf { !skipTests }

    // use JUnit 5
    useJUnitPlatform()

    // settings
    maxHeapSize = "1G"

    // test task output
    testLogging {
        events = mutableSetOf(
            // TestLogEvent.STARTED,
            // TestLogEvent.PASSED,
            TestLogEvent.FAILED,
            TestLogEvent.SKIPPED,
            TestLogEvent.STANDARD_OUT,
            TestLogEvent.STANDARD_ERROR
        )
        showStandardStreams = true
        exceptionFormat = TestExceptionFormat.SHORT
        showExceptions = true
        showCauses = true
        showStackTraces = true
    }

}

val runBenchmarks = task("runBenchmarks", type = JavaExec::class) {
    group = "verification"
    description = "Runs JMH benchmarks."

    // settings
    // documentation: https://github.com/guozheng/jmh-tutorial/blob/master/README.md
    classpath = unitTest.classpath
    mainClass.set("org.openjdk.jmh.Main")
    args = listOf(benchmarks, "-jvmArgs", "-Xms1G -Xmx2G")
}

tasks.withType<JavaCompile> {
    options.encoding = "ASCII"
}

tasks.sonarqube {
    // run all tests and generate JaCoCo XML report
    dependsOn(
        tasks.test, unitTest, integrationTest,
        tasks.jacocoTestReport
    )
}

// helper functions ------------------------------------------------------------

fun getGitBranchName(): String {
    return grgit.branch.current().name
}
