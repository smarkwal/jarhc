import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

plugins {
    java
    jacoco
    `maven-publish`
    idea
}

// special settings for IntelliJ IDEA
idea {
    module {
        isDownloadJavadoc = true
        isDownloadSources = true
    }
}

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

group = "org.jarhc"
version = "1.6-SNAPSHOT"
description = "JarHC - JAR Health Check"
java.sourceCompatibility = JavaVersion.VERSION_1_8

var mainClassName: String = "org.jarhc.Main"
var buildTimestamp: String = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ssX").withZone(ZoneId.of("UTC")).format(Instant.now())

java {
    withSourcesJar()
    withJavadocJar()
}

jacoco {
    toolVersion = "0.8.7"
}

publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
    }
}

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
            // TODO: add more project properties
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
        dependsOn(jarWithDeps)
    }

}

val integrationTest = task("integrationTest", type = Test::class) {
    group = "verification"

    // include only integration tests
    filter {
        includeTestsMatching("*IT")
    }
}

// common settings for all test tasks
tasks.withType<Test> {

    // use JUnit 5
    useJUnitPlatform()

    maxHeapSize = "1G"

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

    // get JaCoCo data from integration tests
    //executionData.builtBy(integrationTest)
    executionData.from("build/jacoco/integrationTest.exec")

    classDirectories.from("build/classes/java/main")
    sourceDirectories.from("src/main/java")

    reports {
        html.required.set(true)
        html.outputLocation.dir("build/reports/jacoco/integrationTest/html") // TODO: this does not seem to work
        xml.required.set(true)
        xml.outputLocation.set(file("build/reports/jacoco/integrationTest/integrationTest.xml"))
        csv.required.set(true)
        csv.outputLocation.set(file("build/reports/jacoco/integrationTest/integrationTest.csv"))
    }

    dependsOn(integrationTest)
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

val jarWithDeps = task("jar-with-deps", type = Jar::class) {
    group = "build"

    // append classifier "-with-deps"
    archiveClassifier.set("with-deps")

    // set Main-Class in MANIFEST.MF
    manifest {
        attributes["Main-Class"] = mainClassName
        // TODO: add more project properties
    }

    // include all dependencies
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })

    // exclude module-info and signature files
    exclude("module-info.class", "META-INF/*.RSA", "META-INF/*.SF", "META-INF/*.DSA")

    // exclude duplicates
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE // alternative: WARN

    // TODO: what is this?
    with(tasks.jar.get() as CopySpec)
}

tasks.withType<JavaCompile> {
    options.encoding = "ASCII"
}

// TODO: include project information
//  - project URL = http://jarhc.org
//  - license name = Apache License, Version 2.0
//  - license URL = https://www.apache.org/licenses/LICENSE-2.0
//  - SCM connection = scm:git:https://github.com/smarkwal/jarhc.git
//  - SCM developer connection = scm:git:https://github.com/smarkwal/jarhc.git
//  - SCM URL = https://github.com/smarkwal/jarhc
//  - developer name = Stephan Markwalder
//  - developer email = stephan@markwalder.net
// TODO: download licenses for dependencies (incl. transitive dependencies) and add them to JAR file
// TODO: merge NOTICE files
// TODO: create aggregated test report
// TODO: create JaCoCo reports
// TODO: create source Xref reports (JXR)
// TODO: run dependency-check (incl. transitive dependencies)
// TODO: run Sonar scan
// TODO: run JMH benchmarks
