/*
 * Copyright 2026 Stephan Markwalder
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

plugins {
    java
}

// project settings ------------------------------------------------------------

description = "JarHC Documentation Tasks"

// special settings for IntelliJ IDEA
idea {
    module {
        sourceDirs = mutableSetOf(
            file("src/main/java")
        )
        isDownloadJavadoc = true
        isDownloadSources = true
    }
}

// dependencies ----------------------------------------------------------------

dependencies {

    // headless browser used to render the example HTML report
    implementation(libs.playwright)

}

// plugin configurations -------------------------------------------------------

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

// tasks -----------------------------------------------------------------------

tasks {

    register("generateDocScreenshots", type = JavaExec::class) {
        group = "documentation"
        description = "Generates the report section screenshots for the documentation from the example HTML report."

        mainClass.set("org.jarhc.docs.GenerateReportScreenshots")
        classpath = sourceSets["main"].runtimeClasspath

        // arguments: <example report HTML file> <output directory for images>
        args(
            file("$rootDir/docs/examples/asm/report.html").absolutePath,
            file("$rootDir/docs/assets/images").absolutePath
        )
    }

}

tasks.withType<JavaCompile> {
    options.encoding = "ASCII"
}
