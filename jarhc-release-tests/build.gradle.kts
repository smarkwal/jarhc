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

import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    java
}

// project settings ------------------------------------------------------------

description = "JarHC Release Tests"

// special settings for IntelliJ IDEA
idea {
    module {
        sourceDirs = mutableSetOf(
            file("src/main/java")
        )
        resourceDirs = mutableSetOf(
            file("src/main/resources")
        )
        isDownloadJavadoc = true
        isDownloadSources = true
    }
}

// dependencies ----------------------------------------------------------------

dependencies {

    implementation("org.junit.jupiter:junit-jupiter:5.9.1")
    implementation("org.junit.jupiter:junit-jupiter-params:5.9.1")
    implementation("org.assertj:assertj-core:3.23.1")
    runtimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.1")
    implementation("org.testcontainers:testcontainers:1.17.6")
    implementation("org.testcontainers:junit-jupiter:1.17.6")
    implementation("org.apache.commons:commons-lang3:3.12.0")
    implementation("commons-io:commons-io:2.11.0")
    runtimeOnly("org.slf4j:slf4j-api:2.0.6")
    runtimeOnly("org.slf4j:slf4j-simple:2.0.6")

}

// plugin configurations -------------------------------------------------------

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

// tasks -----------------------------------------------------------------------

tasks {

    register("prepareTests") {
        group = "verification"
        description = "Prepares the release test suite."

        // run release tests after JAR and fat/uber JAR have been built
        dependsOn(":jarhc:jar", ":jarhc:jar-with-deps", ":jarhc:generatePomFileForMavenPublication")

        doLast {

            // write all JarHC dependencies into configurations.properties
            val dependencies = StringBuilder()
            project(":jarhc").configurations.forEach { conf ->
                val artifacts = if (conf.isCanBeResolved) {
                    conf.resolvedConfiguration.resolvedArtifacts.map { it.moduleVersion.id }.joinToString(",") { it.group + ":" + it.name + ":" + it.version }
                } else {
                    conf.dependencies.joinToString(",") { it.group + ":" + it.name + ":" + it.version }
                }
                if (artifacts.isNotEmpty()) {
                    dependencies.append(conf.name).append(" = ").append(artifacts).append("\n")
                }
            }
            buildDir.mkdir() // make sure the build directory exists
            file("$buildDir/configurations.properties").writeText(dependencies.toString())

            // copy VERSION file from main JarHC project
            copy {
                from(file("$rootDir/jarhc/build/resources/main/VERSION"))
                into(file("$buildDir"))
            }

        }
    }

    test {

        // disable up-to-date check -> re-run tests every time
        outputs.upToDateWhen { false }

        // use JUnit 5
        useJUnitPlatform()

        // look for test classes in main source set
        testClassesDirs = sourceSets["main"].output.classesDirs

        // pass all 'jarhc.*' Gradle properties as system properties to JUnit JVM
        project.properties.forEach {
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

        dependsOn("prepareTests")
    }

    register("jarhcTest", type = Exec::class) {
        group = "verification"
        description = "Run JarHC on JarHC."

        // properties
        isIgnoreExitValue = true

        // command
        commandLine(
            "docker", "run",
            "--rm",
            "-v", "$rootDir/jarhc/build/libs/jarhc-$version-with-deps.jar:/jarhc/jarhc.jar",
            "-v", "$rootDir:/src",
            "-w", "/jarhc",
            "eclipse-temurin:11-jre",
            "java", "-jar", "jarhc.jar", "jarhc.jar",
            "--output", "/src/report.html",
            "--ignore-missing-annotations"
        )

        // run test after fat/uber JAR has been built
        dependsOn(":jarhc:jar-with-deps")
    }

}

tasks.withType<JavaCompile> {
    options.encoding = "ASCII"
}