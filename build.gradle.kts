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
import org.gradle.plugins.ide.idea.model.IdeaLanguageLevel
import org.owasp.dependencycheck.reporting.ReportGenerator.Format
import java.util.*

plugins {

    idea

    // publish to Sonatype OSSRH and release to Maven Central
    id("io.github.gradle-nexus.publish-plugin") version "1.3.0"

    // Gradle Versions Plugin
    // https://github.com/ben-manes/gradle-versions-plugin
    id("com.github.ben-manes.versions") version "0.48.0"

    // run OWASP Dependency-Check analysis
    // note: set same version in .github/workflows/dependency-check.yml
    id("org.owasp.dependencycheck") version "8.4.0"

}

allprojects {

    // load user-specific properties -------------------------------------------
    val userPropertiesFile = file("${rootDir}/gradle.user.properties")
    if (userPropertiesFile.exists()) {
        val userProperties = Properties()
        userProperties.load(userPropertiesFile.inputStream())
        userProperties.forEach {
            project.ext.set(it.key.toString(), it.value)
        }
    }

}

subprojects {

    apply(plugin = "idea")
    apply(plugin = "com.github.ben-manes.versions")

    // Java version check ------------------------------------------------------

    if (!JavaVersion.current().isJava11Compatible) {
        val error = "Build requires Java 11 and does not run on Java ${JavaVersion.current().majorVersion}."
        throw GradleException(error)
    }

    // dependencies ------------------------------------------------------------

    repositories {
        maven {
            url = uri("https://repo.maven.apache.org/maven2/")
        }
    }

}

// special settings for IntelliJ IDEA
idea {

    project {
        jdkName = "11"
        languageLevel = IdeaLanguageLevel(JavaVersion.VERSION_11)
        vcs = "Git"
    }

}

nexusPublishing {
    this.repositories {
        sonatype()
    }
}

dependencyCheck {
    // documentation: https://jeremylong.github.io/DependencyCheck/dependency-check-gradle/configuration.html

    // settings
    format = Format.ALL.toString()
    skipTestGroups = false
    outputDirectory = "${layout.buildDirectory.get()}/reports/dependency-check"

    // path to database directory
    data.directory = "${rootDir}/dependency-check"

    // disable .NET Assembly Analyzer (fix for unexpected build exception)
    analyzers.assemblyEnabled = false

    // exclude test resources from analysis
    scanSet = listOf()

    // suppressed findings
    suppressionFile = "${projectDir}/suppression.xml"
}

tasks {
    register("clean") {
        group = "build"
        doLast {
            // delete build directory in root project
            delete(layout.buildDirectory.get())
        }
    }
}
