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

plugins {

    idea

    // publish to Sonatype OSSRH and release to Maven Central
    id("io.github.gradle-nexus.publish-plugin") version "1.1.0"

    // Gradle Versions Plugin
    // https://github.com/ben-manes/gradle-versions-plugin
    id("com.github.ben-manes.versions") version "0.44.0"

    // create source-xref artifact
    id("org.kordamp.gradle.source-xref") version "0.48.0"

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
    repositories {
        sonatype()
    }
}

subprojects {

    apply(plugin = "idea")
    apply(plugin = "com.github.ben-manes.versions")

    // Java version check ----------------------------------------------------------

    if (!JavaVersion.current().isJava11Compatible) {
        val error = "Build requires Java 11 and does not run on Java ${JavaVersion.current().majorVersion}."
        throw GradleException(error)
    }

}