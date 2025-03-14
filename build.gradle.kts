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
import java.util.*

plugins {

    idea

    // publish to Sonatype OSSRH and release to Maven Central
    id("io.github.gradle-nexus.publish-plugin") version "2.0.0"

    // Gradle Versions Plugin
    // https://github.com/ben-manes/gradle-versions-plugin
    id("com.github.ben-manes.versions") version "0.52.0"

}

buildscript {
    dependencies {
        // fix CVE-2023-3635 in Okio < 3.4.0
        // (indirect dependency of Gradle Versions Plugin 0.51.0)
        // check dependencies with ./gradlew buildEnvironment | grep okio
        classpath("com.squareup.okio:okio:3.10.2")
        classpath("com.squareup.okio:okio-jvm:3.10.2")
    }
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

    if (!JavaVersion.current().isCompatibleWith(JavaVersion.VERSION_17)) {
        val error = "Build requires Java 17 and does not run on Java ${JavaVersion.current().majorVersion}."
        throw GradleException(error)
    }

    // dependencies ------------------------------------------------------------

    repositories {
        maven {
            url = uri("https://repo.maven.apache.org/maven2/")
        }
    }

    tasks {
        dependencyUpdates {
            rejectVersionIf {
                isUnstableVersion(candidate)
            }
        }
    }

}

// special settings for IntelliJ IDEA
idea {

    project {
        jdkName = "17"
        languageLevel = IdeaLanguageLevel(JavaVersion.VERSION_11)
        vcs = "Git"
    }

}

nexusPublishing {
    this.repositories {
        sonatype()
    }
}

tasks {

    register("clean") {
        group = "build"
        doLast {
            // delete build directory in root project
            delete(layout.buildDirectory.get())
        }
    }

    dependencyUpdates {
        gradleReleaseChannel = "current"
        rejectVersionIf {
            isUnstableVersion(candidate)
        }
    }

}

fun isUnstableVersion(candidate: ModuleComponentIdentifier): Boolean {
    return candidate.version.contains("-M") // ignore milestone version
            || candidate.version.contains("-rc") // ignore release candidate versions
            || candidate.version.contains("-alpha") // ignore alpha versions
}
