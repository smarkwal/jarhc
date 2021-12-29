import com.github.jk1.license.render.CsvReportRenderer
import com.github.jk1.license.render.InventoryHtmlReportRenderer
import com.github.jk1.license.render.InventoryMarkdownReportRenderer
import com.github.jk1.license.render.XmlReportRenderer
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.owasp.dependencycheck.reporting.ReportGenerator.Format
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

plugins {
    java
    jacoco
    id("com.github.jk1.dependency-license-report") version "2.0"
    id("org.ajoberstar.grgit") version "4.1.1"
    id("org.owasp.dependencycheck") version "6.5.1"
    `maven-publish`
    idea
}

// project settings ------------------------------------------------------------

group = "org.jarhc"
version = "1.6-SNAPSHOT"
description = "JarHC - JAR Health Check"

// constants -------------------------------------------------------------------

val mainClassName: String = "org.jarhc.Main"
val buildTimestamp: String = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ssX").withZone(ZoneId.of("UTC")).format(Instant.now())
val licenseReportPath: String = "${buildDir}/reports/licenses"

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
    implementation("org.slf4j:slf4j-simple:1.7.32")

    // fix vulnerabilities in transitive dependencies
    // fix CVE-2018-10237 and CVE-2020-8908
    implementation("com.google.guava:guava:31.0.1-jre")
    // fix CVE-2015-5262 and CVE-2020-13956
    implementation("org.apache.httpcomponents:httpclient:4.5.13")
    // fix https://github.com/codehaus-plexus/plexus-utils/issues/3
    implementation("org.codehaus.plexus:plexus-utils:3.0.24")

    // test dependencies
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.2")
    testImplementation("org.mockito:mockito-core:4.2.0")
    testImplementation("org.openjdk.jmh:jmh-core:1.34")
    testImplementation("org.openjdk.jmh:jmh-generator-annprocess:1.34")

}

// plugin configurations -------------------------------------------------------

// special settings for IntelliJ IDEA
idea {
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

    // set Java version to 1.8
    // note: targetCompatibility defaults to sourceCompatibility
    sourceCompatibility = JavaVersion.VERSION_1_8

    // automatically package source code as artifact -sources.jar
    withSourcesJar()

    // automatically package Javadoc as artifact -javadoc.jar
    withJavadocJar()
}

jacoco {
    toolVersion = "0.8.7"
}

dependencyCheck {
    // documentation: https://jeremylong.github.io/DependencyCheck/dependency-check-gradle/configuration.html

    // settings
    format = Format.ALL
    skipTestGroups = false
    outputDirectory = "${buildDir}/reports/dependency-check"

    // path to database directory
    // TODO: support caching when running in GitHub action
    data.directory = "${projectDir}/dependency-check"

    // disable .NET Assembly Analyzer (fix for unexpected build exception)
    analyzers.assemblyEnabled = false
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
        // exclude integration tests
        filter {
            excludeTestsMatching("*IT")
        }
    }

    check {
        dependsOn(integrationTest)
    }

    build {
        dependsOn(jarWithDeps, testJar, libsZip, testLibsZip)
    }

}

// create JAR with test classes
val testJar = task("testJar", type = Jar::class) {
    group = "build"

    // compile test classes first
    dependsOn(tasks.testClasses)

    // append classifier "-tests"
    archiveClassifier.set("tests")

    // include compiled test classes
    from(sourceSets.test.get().output)
}

// create ZIP with runtime dependencies
val libsZip = task("libsZip", type = Zip::class) {
    group = "build"

    // append classifier "-tests"
    archiveClassifier.set("libs")

    // create archive in "libs" folder (instead of "distributions")
    destinationDirectory.set(file("${buildDir}/libs"))

    // include all runtime dependencies
    from(configurations.runtimeClasspath)
}

// create ZIP with test dependencies
val testLibsZip = task("testLibsZip", type = Zip::class) {
    group = "build"

    // append classifier "-tests"
    archiveClassifier.set("test-libs")

    // create archive in "libs" folder (instead of "distributions")
    destinationDirectory.set(file("${buildDir}/libs"))

    // include all test dependencies
    from(configurations.testRuntimeClasspath)
}

val integrationTest = task("integrationTest", type = Test::class) {
    group = "verification"

    // include only integration tests
    filter {
        includeTestsMatching("*IT")
    }

    // run integration tests after unit tests
    mustRunAfter(tasks.test)
}

// common settings for all test tasks
tasks.withType<Test> {

    // skip tests if property "skip.tests" is set
    // command line: -Pskip.tests
    onlyIf { !project.hasProperty("skip.tests") }

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

        // TODO: print test suite summary
    }

}

val jacocoIntegrationTestReport = task("jacocoIntegrationTestReport", type = JacocoReport::class) {
    group = "verification"

    // make sure that integration tests have been executed
    dependsOn(integrationTest)

    // get JaCoCo data from integration tests
    //executionData.builtBy(integrationTest)
    executionData.from("${buildDir}/jacoco/integrationTest.exec")

    // set paths to source and class files
    sourceDirectories.from("${projectDir}/src/main/java")
    classDirectories.from("${buildDir}/classes/java/main")

    // configure reports
    reports {
        html.required.set(true)
        html.outputLocation.dir("${buildDir}/reports/jacoco/integrationTest/html") // TODO: this does not seem to work
        xml.required.set(true)
        xml.outputLocation.set(file("${buildDir}/reports/jacoco/integrationTest/integrationTest.xml"))
        csv.required.set(true)
        csv.outputLocation.set(file("${buildDir}/reports/jacoco/integrationTest/integrationTest.csv"))
    }
}

// common settings for all JaCoCo report tasks
tasks.withType<JacocoReport> {

    // generate HTML, XML and CSV report
    reports {
        html.required.set(true)
        xml.required.set(true)
        csv.required.set(true)
    }

}

// task to build "with-deps" fat/uber JAR file
val jarWithDeps = task("jar-with-deps", type = Jar::class) {
    group = "build"

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

    // TODO: what is this?
    with(tasks.jar.get() as CopySpec)
}

tasks.withType<JavaCompile> {
    options.encoding = "ASCII"
}

// helper functions ------------------------------------------------------------

fun getGitBranchName(): String {
    return grgit.branch.current().name
}

// TODO ------------------------------------------------------------------------

// Work in progress:
// TODO: create JaCoCo reports

// Task list:
// TODO: define task inputs and outputs
// TODO: configure Gradle cache
// TODO: include project information
//  - project URL = http://jarhc.org
//  - license name = Apache License, Version 2.0
//  - license URL = https://www.apache.org/licenses/LICENSE-2.0
//  - SCM connection = scm:git:https://github.com/smarkwal/jarhc.git
//  - SCM developer connection = scm:git:https://github.com/smarkwal/jarhc.git
//  - SCM URL = https://github.com/smarkwal/jarhc
//  - developer name = Stephan Markwalder
//  - developer email = stephan@markwalder.net
// TODO: create aggregated test report
// TODO: create source Xref reports (JXR)
// TODO: run dependency-check (incl. transitive dependencies)
// TODO: run Sonar scan
// TODO: run JMH benchmarks
// TODO: create artifact with all reports?
// TODO: add post-build validation
//  - with-deps JAR can be launched
//  - with-deps JAR does not contain Java 9+ classes
//  - run JarHC on JarHC?
//  - check for presence of certain files (license, ...)
// TODO: add task to clean JarHC cache