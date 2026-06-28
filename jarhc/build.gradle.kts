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

plugins {
    `java-library`
    jacoco

    // publish to Maven Central via the Central Portal
    // (applies maven-publish and signing automatically)
    alias(libs.plugins.central.publish)

    // create report with all open-source licenses
    alias(libs.plugins.license.report)

    // run Sonar analysis
    alias(libs.plugins.sonarqube)

    // JarHC Gradle plugin
    alias(libs.plugins.jarhc)

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
            file("src/test/java")
        )
        testResources.from(
            file("src/test/resources")
        )
        isDownloadJavadoc = true
        isDownloadSources = true
    }
}

// Preconditions based on which tasks should be executed -----------------------

gradle.taskGraph.whenReady {

    // if sonar task should be executed ...
    if (gradle.taskGraph.hasTask(":sonar")) {
        // environment variable SONAR_TOKEN or system property "sonar.token" must be set
        val tokenFound = System.getProperties().containsKey("sonar.token") || System.getenv("SONAR_TOKEN") != null
        if (!tokenFound) {
            val error = "Sonar: Token not found.\nPlease set system property 'sonar.token' or environment variable 'SONAR_TOKEN'."
            throw GradleException(error)
        }
    }

}

// configuration properties ----------------------------------------------------

// flag to skip tests
// command line option: -Pskip.tests
val skipTests: Boolean = project.hasProperty("skip.tests")

// constants -------------------------------------------------------------------

val mainClassName: String = "org.jarhc.Main"

// license report
val licenseReportPath: String = "${layout.buildDirectory.get()}/reports/licenses"

// JaCoCo coverage report
val jacocoTestReportXml: String = "${layout.buildDirectory.get()}/reports/jacoco/test/report.xml"

// additional source sets and configurations -----------------------------------

configurations {

    // exclude commons-logging because it is replaced by jcl-over-slf4j
    // see https://github.com/smarkwal/jarhc/issues/112
    implementation {
        exclude(group = "commons-logging", module = "commons-logging")
    }

}

val includeInJarApp = configurations.create("includeInJarApp")

// dependencies ----------------------------------------------------------------

dependencies {

    // main dependencies
    implementation(libs.asm)
    implementation(libs.json)
    implementation(libs.maven.resolver.supplier)
    implementation(libs.slf4j.jul)
    implementation(libs.slf4j.jcl)
    api(libs.slf4j.api)

    // additional libraries to be added to jar-app
    includeInJarApp(libs.slf4j.simple)

    // test dependencies
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.assertj.core)
    testImplementation(libs.mockito.core)
    testRuntimeOnly(libs.junit.platform.launcher)
    testRuntimeOnly(libs.slf4j.simple)

    // fix CVE-2025-67030
    implementation(libs.plexus.utils)

}

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

    // Note: the -sources.jar and -javadoc.jar artifacts are configured by the
    // Maven Central publish plugin (mavenPublishing block below).
}

val sonarBranchName: String = getGitBranchName()
val sonarBranchTarget: String = getGitBranchTarget(sonarBranchName)

sonar {
    // documentation: https://docs.sonarqube.org/latest/analyzing-source-code/scanners/sonarscanner-for-gradle/

    properties {

        // connection to SonarCloud
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.organization", "smarkwal")
        property("sonar.projectKey", "smarkwal_jarhc")

        // Git branch
        property("sonar.branch.name", sonarBranchName)

        // target Git branch for merge
        if (sonarBranchTarget != sonarBranchName) {
            // https://docs.sonarsource.com/sonarqube-cloud/enriching/branch-analysis-setup/
            property("sonar.branch.target", sonarBranchTarget)
            // https://docs.sonarsource.com/sonarqube-server/latest/analyzing-source-code/analysis-parameters/
            property("sonar.newCode.referenceBranch", sonarBranchTarget)
        }

        // paths to test sources and test classes
        property("sonar.tests", "${projectDir}/src/test/java")
        property("sonar.java.test.binaries", "${layout.buildDirectory.get()}/classes/java/test")

        // include test results
        property("sonar.junit.reportPaths", "${layout.buildDirectory.get()}/test-results/test")

        // include test coverage results
        property("sonar.java.coveragePlugin", "jacoco")
        property("sonar.coverage.jacoco.xmlReportPaths", jacocoTestReportXml)
    }
}

mavenPublishing {

    // Upload to the Central Portal and create a deployment. The deployment is
    // NOT released automatically: review and publish it manually at
    // https://central.sonatype.com/publishing
    // Use 'publishAndReleaseToMavenCentral' instead to also release in one step.
    publishToMavenCentral()

    // sign all artifacts (uses the 'signing.*' properties)
    signAllPublications()

    // Maven coordinates of the published artifact
    coordinates("org.jarhc", "jarhc", project.version.toString())

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

// tasks -----------------------------------------------------------------------

tasks {

    processResources {

        // declare the version as a task input so that the task is no longer
        // up-to-date when the version changes
        inputs.property("version", project.version)

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
        dependsOn(test)
    }

    jacocoTestReport {

        // run all tests first
        dependsOn(test)

        // get JaCoCo data from all test tasks
        executionData.from(
            "${layout.buildDirectory.get()}/jacoco/test.exec"
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
            file("${rootDir}/website/docs/jarhc-report.html"),
            file("${rootDir}/website/docs/jarhc-report.txt")
        )
        sortRows.set(true)
        ignoreMissingAnnotations.set(true)
    }

    assemble {
        dependsOn(jarApp)
    }

}

val jarApp = tasks.register<Jar>("jar-app") {
    group = "build"
    description = "Assembles a fat/uber jar archive with all runtime dependencies."

    // make sure that license report has been generated
    dependsOn(tasks.generateLicenseReport)

    // append classifier "-app"
    archiveClassifier.set("app")

    // set Main-Class in MANIFEST.MF
    manifest {
        attributes["Main-Class"] = mainClassName

        // plexus-utils-3.6.1.jar is a multi-release JAR
        // -> fat/uber JAR is also a multi-release JAR
        attributes["Multi-Release"] = "true"
    }

    // include all files from all runtime dependencies
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })

    // add all additional libraries
    from(includeInJarApp.map { if (it.isDirectory) it else zipTree(it) })

    // add additional resources for jar-app
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

// common settings for all test tasks
tasks.withType<Test> {

    // skip tests if property "skip.tests" is set
    onlyIf { !skipTests }

    // disable up-to-date check -> re-run tests every time
    outputs.upToDateWhen { false }

    // use JUnit 5
    useJUnitPlatform()

    // pass all 'jarhc.*' Gradle properties as system properties to JUnit JVM
    providers.gradlePropertiesPrefixedBy("jarhc.").get().forEach { (name, value) ->
        systemProperty(name, value)
    }

    // do not allow requests to non-local repositories in tests
    systemProperty("jarhc.test.maven.local-only", "true")

    // set locale to English (US)
    systemProperty("user.language", "en")
    systemProperty("user.country", "US")

    // set time zone to UTC
    systemProperty("user.timezone", "UTC")

    // set file encoding to UTF-8
    systemProperty("file.encoding", "UTF-8")

    // use Linux/Unix line separator
    systemProperty("line.separator", "\n")

    // settings
    maxHeapSize = "1G"

    // test task output
    testLogging {
        showStandardStreams = true
    }

}

tasks.withType<JavaCompile> {
    options.encoding = "ASCII"
}

tasks.sonar {
    // run all tests and generate JaCoCo XML report
    dependsOn(
        tasks.test,
        tasks.jacocoTestReport
    )
}

// disable generation of Gradle module metadata file
tasks.withType<GenerateModuleMetadata> {
    enabled = false
}

// helper functions ------------------------------------------------------------

fun getGitBranchName(): String {
    val file = project.rootDir.resolve(".git/HEAD")
    if (file.isFile) {
        val content = file.readText(Charsets.UTF_8).trim()
        if (content.startsWith("ref: refs/heads/")) {
            return content.substring("ref: refs/heads/".length)
        }
        return content
    }
    throw GradleException("Git branch name not found.")
}

fun getGitBranchTarget(branchName: String): String {
    return "main"
}
