/*
 * Copyright 2021 Stephan Markwalder
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

package org.jarhc.test.release;

import static org.jarhc.test.release.utils.ExecResultAssert.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.assertj.core.api.Assertions;
import org.jarhc.test.release.utils.Command;
import org.jarhc.test.release.utils.JavaContainer;
import org.jarhc.test.release.utils.JavaImage;
import org.jarhc.test.release.utils.TestUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.io.TempDir;
import org.testcontainers.containers.Container.ExecResult;

class JarHcTest extends ReleaseTest {

	private static final JavaImage[] JAVA_IMAGES = {
			new JavaImage("amazon", "corretto", "8", "amazoncorretto:8"),
			new JavaImage("amazon", "corretto", "11", "amazoncorretto:11"),
			new JavaImage("amazon", "corretto", "17", "amazoncorretto:17"),
			new JavaImage("eclipse", "temurin", "8", "eclipse-temurin:8-jre"),
			new JavaImage("eclipse", "temurin", "11", "eclipse-temurin:11-jre"),
			new JavaImage("eclipse", "temurin", "17", "eclipse-temurin:17-jre"),
			new JavaImage("ibm", "sdk", "8", "ibmjava:8-jre"),
			new JavaImage("ibm", "semeru", "8", "ibm-semeru-runtimes:open-8-jre"),
			new JavaImage("ibm", "semeru", "11", "ibm-semeru-runtimes:open-11-jre"),
			new JavaImage("ibm", "semeru", "17", "ibm-semeru-runtimes:open-17-jre"),
			new JavaImage("microsoft", "openjdk", "11", "mcr.microsoft.com/openjdk/jdk:11-ubuntu"),
			new JavaImage("microsoft", "openjdk", "17", "mcr.microsoft.com/openjdk/jdk:17-ubuntu"),
			new JavaImage("oracle", "openjdk", "8", "openjdk:8-jre"),
			new JavaImage("oracle", "openjdk", "11", "openjdk:11-jre"),
			new JavaImage("oracle", "openjdk", "17", "openjdk:17-jdk")
	};

	private File resourcesDir;

	private File tempDir;
	private File tempReportsDir;
	private File tempDataDir;

	@TestFactory
	Collection<DynamicContainer> test(@TempDir Path tempDir) {

		List<DynamicContainer> containers = new ArrayList<>();

		// get path to test resources
		resourcesDir = getProjectDirectory("src/releaseTest/resources");

		this.tempDir = tempDir.toFile();

		// make sure that reports directory exists
		tempReportsDir = new File(this.tempDir, "reports");
		createDirectory(tempReportsDir);

		// make sure that data directory exists
		// note: all tests will use the same data directory
		tempDataDir = new File(tempDir.toFile(), "data");
		createDirectory(tempDataDir);
		System.out.println("Data directory: " + tempDataDir.getAbsolutePath());

		// for every Java Docker image
		for (JavaImage javaImage : JAVA_IMAGES) {

			// prepare a collection of tests
			List<DynamicTest> tests = new ArrayList<>();
			tests.add(DynamicTest.dynamicTest("Java Version", () -> runInContainer(javaImage, this::java_version)));
			tests.add(DynamicTest.dynamicTest("JarHC Version", () -> runInContainer(javaImage, this::jarhc_version)));
			tests.add(DynamicTest.dynamicTest("JarHC Help", () -> runInContainer(javaImage, this::jarhc_help)));
			tests.add(DynamicTest.dynamicTest("JarHC Java Runtime", () -> runInContainer(javaImage, this::jarhc_javaRuntime)));
			tests.add(DynamicTest.dynamicTest("JarHC for ASM", () -> runInContainer(javaImage, this::jarhc_forASM)));
			tests.add(DynamicTest.dynamicTest("JarHC for JarHC (fat)", () -> runInContainer(javaImage, this::jarhc_forJarHC_fat)));
			tests.add(DynamicTest.dynamicTest("JarHC for JarHC (artifacts)", () -> runInContainer(javaImage, this::jarhc_forJarHC_artifacts)));
			tests.add(DynamicTest.dynamicTest("JarHC for JarHC (provided)", () -> runInContainer(javaImage, this::jarhc_forJarHC_provided)));

			// add all tests to a test container for grouping
			String imageName = javaImage.getImageName();
			containers.add(DynamicContainer.dynamicContainer(imageName, tests));
		}

		// return test containers
		return containers;
	}

	@FunctionalInterface
	private interface TestMethod {
		void test(JavaContainer container);
	}

	private void runInContainer(JavaImage javaImage, TestMethod testMethod) {

		// run test only if test resources are available
		File reportsDir = new File(resourcesDir, "reports");
		File reportDir = new File(reportsDir, javaImage.getPath());
		assumeTrue(reportDir.isDirectory(), "Test resources found for " + javaImage.getImageName());

		JavaContainer container = createJavaContainer(javaImage, tempReportsDir, tempDataDir);
		try {
			container.start();
			testMethod.test(container);
		} finally {
			container.stop();
		}
	}

	private void java_version(JavaContainer container) {

		// prepare
		JavaImage javaImage = container.getJavaImage();
		String outputPath = javaImage.getReportPath("java-version.txt");
		String expectedOutput = readResource(outputPath);
		Command command = Command.java("-version");

		// test
		ExecResult result = container.exec(command);

		if (TestUtils.createResources()) {
			String actualOutput = result.getStderr();
			if (!actualOutput.equals(expectedOutput)) {
				writeProjectFile("src/releaseTest/resources/" + outputPath, actualOutput);
				return;
			}
		}

		// assert
		assertThat(result).isEqualTo(0, "", expectedOutput);

	}

	private void jarhc_version(JavaContainer container) {

		// prepare
		Command command = Command.jarHc("--version");
		String expectedOutput = String.format("JarHC - JAR Health Check %s\n", getJarHcVersion());

		// test
		ExecResult result = container.exec(command);

		// assert
		assertThat(result).isEqualTo(0, expectedOutput, "");

	}

	private void jarhc_help(JavaContainer container) {

		// prepare
		Command command = Command.jarHc("--help");
		String expectedOutput = readResource("help.txt");

		// test
		ExecResult result = container.exec(command);

		// assert
		assertThat(result).isEqualTo(0, expectedOutput, "");

	}

	private void jarhc_javaRuntime(JavaContainer container) {

		// prepare
		String resourceName = "java-runtime.txt";
		JavaImage javaImage = container.getJavaImage();
		String reportPath = javaImage.getReportPath(resourceName);
		Command command = Command.jarHc(
				"-o", reportPath,
				"-s", "jr", // include only section Java Runtime
				"jarhc.jar" // TODO: find a tiny artifact to scan
		);

		// override JarHC version for reproducible test output
		command.addJavaArguments("-Djarhc.version.override=0.0.1");

		// test
		jarhcTest(container, command, resourceName);

	}

	private void jarhc_forASM(JavaContainer container) {

		// prepare
		String resourceName = "asm.txt";
		JavaImage javaImage = container.getJavaImage();
		String reportPath = javaImage.getReportPath(resourceName);
		Command command = Command.jarHc(
				"-o", reportPath,
				"-s", "-jr", // exclude section Java Runtime
				"org.ow2.asm:asm:9.2"
		);

		// override JarHC version for reproducible test output
		command.addJavaArguments("-Djarhc.version.override=0.0.1");

		// test
		jarhcTest(container, command, resourceName);

	}

	private void jarhc_forJarHC_fat(JavaContainer container) {

		// prepare
		String resourceName = "jarhc-fat.txt";
		JavaImage javaImage = container.getJavaImage();
		String reportPath = javaImage.getReportPath(resourceName);
		Command command = Command.jarHc(
				"-o", reportPath,
				"-s", "-jr", // exclude section Java Runtime
				"jarhc-with-deps.jar"
		);

		// override JarHC version for reproducible test output
		command.addJavaArguments("-Djarhc.version.override=0.0.1");

		// test
		jarhcTest(container, command, resourceName);

	}

	private void jarhc_forJarHC_artifacts(JavaContainer container) {

		// prepare
		String resourceName = "jarhc-artifacts.txt";
		JavaImage javaImage = container.getJavaImage();
		String reportPath = javaImage.getReportPath(resourceName);
		String dependencies = getDependencies("runtimeClasspath");
		Command command = Command.jarHc(
				"-o", reportPath,
				"-s", "-jr", // exclude section Java Runtime
				"jarhc.jar",
				"--classpath", dependencies
		);

		// override JarHC version for reproducible test output
		command.addJavaArguments("-Djarhc.version.override=0.0.1");

		// test
		jarhcTest(container, command, resourceName);

	}

	private void jarhc_forJarHC_provided(JavaContainer container) {

		// prepare
		String resourceName = "jarhc-provided.txt";
		JavaImage javaImage = container.getJavaImage();
		String reportPath = javaImage.getReportPath(resourceName);
		String dependencies = getDependencies("implementation");
		Command command = Command.jarHc(
				"-o", reportPath,
				"-s", "-jr", // exclude section Java Runtime
				"jarhc.jar",
				"--provided", dependencies
		);

		// override JarHC version for reproducible test output
		command.addJavaArguments("-Djarhc.version.override=0.0.1");

		// test
		jarhcTest(container, command, resourceName);

	}

	private void jarhcTest(JavaContainer container, Command command, String resourceName) {

		// assume
		JavaImage javaImage = container.getJavaImage();
		String reportPath = javaImage.getReportPath(resourceName);
		File expectedReport = findExpectedReport(javaImage, resourceName);
		assumeTrue(expectedReport.isFile(), "Report found in test resources: " + reportPath);

		// prepare
		String expectedOutput = readResource("stdout.txt");

		// test
		ExecResult result = container.exec(command);

		// assert
		assertThat(result).isEqualTo(0, expectedOutput, "");

		File actualReport = new File(tempDir, reportPath);
		TestUtils.normalizeReport(actualReport);

		// if test resources should be overwritten ..
		if (TestUtils.createResources()) {
			try {
				if (!FileUtils.contentEquals(actualReport, expectedReport)) {
					FileUtils.copyFile(actualReport, expectedReport);
					return;
				}
			} catch (IOException e) {
				throw new AssertionError("Unexpected I/O error.", e);
			}
		}

		// compare generated report with expected report
		Assertions.assertThat(actualReport).hasSameTextualContentAs(expectedReport, StandardCharsets.UTF_8);

	}

	@NotNull
	private File findExpectedReport(JavaImage javaImage, String resourceName) {

		// try to find report in image-specific test resources
		String reportPath = javaImage.getReportPath(resourceName);
		File expectedReport = new File(resourcesDir, reportPath);
		if (expectedReport.isFile()) {
			return expectedReport;
		}

		// try to find report in Java version-specific test resources
		reportPath = String.format("reports/all/%s/%s", javaImage.getVersion(), resourceName);
		expectedReport = new File(resourcesDir, reportPath);
		if (expectedReport.isFile()) {
			return expectedReport;
		}

		// try to find report in generic test resources
		reportPath = String.format("reports/all/all/%s", resourceName);
		return new File(resourcesDir, reportPath);
	}

}
