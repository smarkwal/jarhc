/*
 * Copyright 2022 Stephan Markwalder
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.github.jk1.license.render.CsvReportRenderer
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    `java-library`
    `java-test-fixtures`
    jacoco
    signing
    `maven-publish`

    // create report with all open-source licenses
    id("com.github.jk1.dependency-license-report") version "2.1"

    // run Sonar analysis
    id("org.sonarqube") version "4.0.0.2929"

    // get current Git branch name
    id("org.ajoberstar.grgit") version "5.0.0"

    // JarHC Gradle plugin
    id("org.jarhc") version "1.0.1"

}

// project settings ------------------------------------------------------------

description = "JarHC Core"

// special settings for IntelliJ IDEA
idea {
    module {
        sourceDirs = mutableSetOf(
            file("src/main/java")
        )
        resourceDirs = mutableSetOf(
            file("src/main/resources")
        )
        testSources.from(
            //file("src/testFixtures/java"), // TODO: mark as test sources causes "Symbol not found" in integration tests
            file("src/test/java"),
            file("src/integrationTest/java"),
        )
        testResources.from(
            file("src/testFixtures/resources"),
            file("src/test/resources"),
            file("src/integrationTest/resources"),
        )
        isDownloadJavadoc = true
        isDownloadSources = true
    }
}

// Preconditions based on which tasks should be executed -----------------------

gradle.taskGraph.whenReady {

    // if sonar task should be executed ...
    if (gradle.taskGraph.hasTask(":sonar")) {
        // environment variable SONAR_TOKEN or property "sonar.login" must be set
        val tokenFound = project.hasProperty("sonar.login") || System.getenv("SONAR_TOKEN") != null
        if (!tokenFound) {
            val error = "Sonar: Token not found.\nPlease set property 'sonar.login' or environment variable 'SONAR_TOKEN'."
            throw GradleException(error)
        }
    }

}

// configuration properties ----------------------------------------------------

// flag to skip unit and integration tests
// command line option: -Pskip.tests
val skipTests: Boolean = project.hasProperty("skip.tests")

// constants -------------------------------------------------------------------

val mainClassName: String = "org.jarhc.Main"

// license report
val licenseReportPath: String = "${buildDir}/reports/licenses"

// test results
val testReportPath: String = "${buildDir}/test-results/test"
val integrationTestReportPath: String = "${buildDir}/test-results/integrationTest"

// JaCoCo coverage report
val jacocoTestReportXml: String = "${buildDir}/reports/jacoco/test/report.xml"

// additional source sets and configurations -----------------------------------

sourceSets {

    create("integrationTest") {
        compileClasspath += sourceSets.main.get().output + sourceSets.testFixtures.get().output
        runtimeClasspath += sourceSets.main.get().output + sourceSets.testFixtures.get().output
    }

}

configurations {

    // exclude commons-logging because it is replaced by jcl-over-slf4j
    // see https://github.com/smarkwal/jarhc/issues/112
    implementation {
        exclude(group = "commons-logging", module = "commons-logging")
    }

}

val integrationTestImplementation: Configuration by configurations.getting {
    extendsFrom(
        configurations.implementation.get(),
        configurations.testFixturesImplementation.get()
    )
}

val integrationTestRuntimeOnly: Configuration by configurations.getting {
    extendsFrom(
        configurations.runtimeOnly.get(),
        configurations.testFixturesRuntimeOnly.get()
    )
}

val includeInJarWithDeps: Configuration by configurations.creating

// dependencies ----------------------------------------------------------------

dependencies {

    // main dependencies
    implementation("org.ow2.asm:asm:9.5")
    implementation("org.json:json:20230227")
    implementation("org.eclipse.aether:aether-impl:1.1.0")
    implementation("org.eclipse.aether:aether-api:1.1.0")
    implementation("org.eclipse.aether:aether-util:1.1.0")
    implementation("org.eclipse.aether:aether-spi:1.1.0")
    implementation("org.eclipse.aether:aether-connector-basic:1.1.0")
    implementation("org.eclipse.aether:aether-transport-file:1.1.0")
    implementation("org.eclipse.aether:aether-transport-http:1.1.0")
    implementation("org.apache.maven:maven-aether-provider:3.3.9")
    implementation("org.slf4j:jul-to-slf4j:2.0.7")
    implementation("org.slf4j:jcl-over-slf4j:2.0.7")
    api("org.slf4j:slf4j-api:2.0.7")

    // fix vulnerabilities in transitive dependencies
    // fix CVE-2018-10237 and CVE-2020-8908
    implementation("com.google.guava:guava:31.1-jre")
    // fix CVE-2015-5262 and CVE-2020-13956
    implementation("org.apache.httpcomponents:httpclient:4.5.14")
    // fix https://github.com/codehaus-plexus/plexus-utils/issues/3
    implementation("org.codehaus.plexus:plexus-utils:3.5.1")
    // fix https://devhub.checkmarx.com/cve-details/Cxeb68d52e-5509/
    implementation("commons-codec:commons-codec:1.15")

    // additional libraries to be added to jar-with-deps
    includeInJarWithDeps("org.slf4j:slf4j-simple:2.0.7")

    // test dependencies (available in unit and integration tests)
    testFixturesApi("org.junit.jupiter:junit-jupiter:5.9.2")
    testFixturesApi("org.mockito:mockito-core:5.3.1")
    testFixturesApi("org.slf4j:slf4j-simple:2.0.7")

}

/// do not add test-fixtures dependencies to POM file
val javaComponent = components["java"] as AdhocComponentWithVariants
javaComponent.withVariantsFromConfiguration(configurations["testFixturesApiElements"]) { skip() }
javaComponent.withVariantsFromConfiguration(configurations["testFixturesRuntimeElements"]) { skip() }

// plugin configurations -------------------------------------------------------

licenseReport {
    outputDir = licenseReportPath
    renderers = arrayOf(
        CsvReportRenderer("licenses.csv")
    )
}

java {

    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }

    // automatically package source code as artifact -sources.jar
    withSourcesJar()

    // automatically package Javadoc as artifact -javadoc.jar
    withJavadocJar()
}

jacoco {
    toolVersion = "0.8.8"
}

sonar {
    // documentation: https://docs.sonarqube.org/latest/analyzing-source-code/scanners/sonarscanner-for-gradle/

    properties {

        // connection to SonarCloud
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.organization", "smarkwal")
        property("sonar.projectKey", "smarkwal_jarhc")

        // Git branch
        property("sonar.branch.name", getGitBranchName())

        // paths to test sources and test classes
        property("sonar.tests", "${projectDir}/src/test/java,${projectDir}/src/integrationTest/java")
        property("sonar.java.test.binaries", "${buildDir}/classes/java/test,${buildDir}/classes/java/integrationTest")

        // include test results
        property("sonar.junit.reportPaths", "${testReportPath},${integrationTestReportPath}")

        // include test coverage results
        property("sonar.java.coveragePlugin", "jacoco")
        property("sonar.coverage.jacoco.xmlReportPaths", jacocoTestReportXml)
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {

            from(components["java"])

            pom {

                name.set("JarHC - JAR Health Check")
                description.set("JarHC is a static analysis tool to help you find your way through \"JAR hell\" or \"classpath hell\".")
                url.set("http://jarhc.org")

                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }

                developers {
                    developer {
                        id.set("smarkwal")
                        name.set("Stephan Markwalder")
                        email.set("stephan@markwalder.net")
                        url.set("https://github.com/smarkwal")
                    }
                }

                scm {
                    connection.set("scm:git:git://github.com/smarkwal/jarhc.git")
                    developerConnection.set("scm:git:ssh://github.com/smarkwal/jarhc.git")
                    url.set("https://github.com/smarkwal/jarhc")
                }

            }
        }
    }
}

signing {
    sign(publishing.publications["maven"])
}

// tasks -----------------------------------------------------------------------

tasks {

    processResources {

        // replace placeholders in resources
        // (see src/main/resources/jarhc.properties)
        expand(
            "version" to project.version
        )
    }

    jar {

        // set Main-Class in MANIFEST.MF
        manifest {
            attributes["Main-Class"] = mainClassName
            attributes["Automatic-Module-Name"] = "org.jarhc"
        }

        // add LICENSE to JAR file
        from("../LICENSE")
    }

    test {
        // no special configuration
    }

    check {
        dependsOn(test, integrationTest)
    }

    jacocoTestReport {

        // run all tests first
        dependsOn(test, integrationTest)

        // get JaCoCo data from all test tasks
        executionData.from(
            "${buildDir}/jacoco/test.exec",
            "${buildDir}/jacoco/integrationTest.exec"
        )

        reports {

            // generate XML report (required for Sonar)
            xml.required.set(true)
            xml.outputLocation.set(file(jacocoTestReportXml))

            // generate HTML report
            html.required.set(true)

            // generate CSV report
            // csv.required.set(true)
        }
    }

    jarhcReport {
        dependsOn(jar)
        classpath.setFrom(
            jar.get().archiveFile,
            configurations.runtimeClasspath
        )
        reportFiles.setFrom(
            file("${rootDir}/docs/jarhc-report.html"),
            file("${rootDir}/docs/jarhc-report.txt")
        )
    }

    build {
        dependsOn(jarhcReport)
    }

    assemble {
        dependsOn(jarWithDeps)
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

        // plexus-utils-3.4.1.jar is a multi-release JAR
        // -> fat/uber JAR is also a multi-release JAR
        attributes["Multi-Release"] = "true"
    }

    // include all files from all runtime dependencies
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })

    // add all additional libraries
    from(includeInJarWithDeps.map { if (it.isDirectory) it else zipTree(it) })

    // add additional resources for jar-with-deps
    from("src/app/resources")

    // include license report
    from(licenseReportPath) {
        into("META-INF/licenses")
    }

    exclude(

        // exclude module-info files
        "**/module-info.class",

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

val integrationTest = task("integrationTest", type = Test::class) {
    group = "verification"
    description = "Runs the integration test suite."

    // use tests in integrationTest source set
    testClassesDirs = sourceSets["integrationTest"].output.classesDirs
    classpath = sourceSets["integrationTest"].runtimeClasspath

    // run tests in parallel in the same JVM (experimental feature of JUnit 5)
    // see https://junit.org/junit5/docs/current/user-guide/#writing-tests-parallel-execution
    systemProperty("junit.jupiter.execution.parallel.enabled", "true")
    // run test classes in parallel, but all test methods of a class by the same thread
    systemProperty("junit.jupiter.execution.parallel.mode.default", "same_thread")
    systemProperty("junit.jupiter.execution.parallel.mode.classes.default", "concurrent")
    // run max 4 test classes in parallel (more can result in an OutOfMemoryError)
    systemProperty("junit.jupiter.execution.parallel.config.strategy", "fixed")
    systemProperty("junit.jupiter.execution.parallel.config.fixed.parallelism", "4")

    // run integration tests after unit tests
    shouldRunAfter(tasks.test)
}

// common settings for all test tasks
tasks.withType<Test> {

    // skip tests if property "skip.tests" is set
    onlyIf { !skipTests }

    // disable up-to-date check -> re-run tests every time
    outputs.upToDateWhen { false }

    // use JUnit 5
    useJUnitPlatform()

    // pass all 'jarhc.*' Gradle properties as system properties to JUnit JVM
    properties.forEach {
        if (it.key.startsWith("jarhc.")) {
            systemProperty(it.key, it.value.toString())
        }
    }

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

tasks.withType<JavaCompile> {
    options.encoding = "ASCII"
}

tasks.sonar {
    // run all tests and generate JaCoCo XML report
    dependsOn(
        tasks.test, integrationTest,
        tasks.jacocoTestReport
    )
}

// disable generation of Gradle module metadata file
tasks.withType<GenerateModuleMetadata> {
    enabled = false
}

// helper functions ------------------------------------------------------------

fun getGitBranchName(): String {
    return grgit.branch.current().name
}
