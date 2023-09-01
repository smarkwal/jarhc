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

plugins {
    java
}

// project settings ------------------------------------------------------------

description = "JarHC Benchmarks"

// special settings for IntelliJ IDEA
idea {
    module {
        sourceDirs = mutableSetOf(
            file("src/main/java")
        )
        resourceDirs = mutableSetOf(
            file("src/main/resources")
        )
    }
}

// configuration properties ----------------------------------------------------

// select JMH benchmarks
// to run a single benchmark: -Pbenchmarks=DefaultJavaRuntimeBenchmark
val benchmarks: String = (project.properties["benchmarks"] ?: ".*") as String

// dependencies ----------------------------------------------------------------

dependencies {

    implementation(project(":jarhc"))
    implementation(testFixtures(project(":jarhc")))

    // JMH
    implementation("org.openjdk.jmh:jmh-core:1.37")
    annotationProcessor("org.openjdk.jmh:jmh-generator-annprocess:1.36")

}

// plugin configurations -------------------------------------------------------

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

// tasks -----------------------------------------------------------------------

val runBenchmarks = task("runBenchmarks", type = JavaExec::class) {
    group = "verification"
    description = "Runs JMH benchmarks."

    // TODO: use Java 11 toolchain

    // settings
    // documentation: https://github.com/guozheng/jmh-tutorial/blob/master/README.md
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("org.openjdk.jmh.Main")
    args = listOf(benchmarks, "-jvmArgs", "-Xms1G -Xmx2G")
}

tasks.withType<JavaCompile> {
    options.encoding = "ASCII"
}
