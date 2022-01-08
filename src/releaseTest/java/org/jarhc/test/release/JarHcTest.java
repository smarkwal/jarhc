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
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.jarhc.test.release.utils.Command;
import org.jarhc.test.release.utils.JavaContainer;
import org.jarhc.test.release.utils.JavaImage;
import org.jarhc.test.release.utils.TestUtils;
import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.io.TempDir;
import org.testcontainers.containers.Container.ExecResult;

class JarHcTest extends ReleaseTest {

	private static final JavaImage[] JAVA_IMAGES = {
			new JavaImage("8", "eclipse-temurin:8-jre"),
			new JavaImage("11", "eclipse-temurin:11-jre"),
			new JavaImage("17", "eclipse-temurin:17-jre"),
			//new JavaImage("8-ibm", "ibmjava:8-jre"),
			new JavaImage("8-ibm", "ibm-semeru-runtimes:open-8-jre"),
			new JavaImage("11-ibm", "ibm-semeru-runtimes:open-11-jre"),
			new JavaImage("17-ibm", "ibm-semeru-runtimes:open-17-jre")
	};

	private File resourcesDir;
	private File outputDir;
	private File reportsDir;
	private File dataDir;

	@TestFactory
	Collection<DynamicContainer> test(@TempDir Path tempDir) {

		List<DynamicContainer> containers = new ArrayList<>();

		// get path to test resources
		resourcesDir = getProjectDirectory("src/releaseTest/resources");

		// update test resource?
		if (TestUtils.createResources()) {
			// let tests directly overwrite resources
			outputDir = resourcesDir;
		} else {
			outputDir = tempDir.toFile();
		}

		// make sure that reports directory exists
		reportsDir = new File(outputDir, "reports");
		createDirectory(reportsDir);

		// make sure that data directory exists
		// note: all tests will use the same data directory
		dataDir = new File(tempDir.toFile(), "data");
		createDirectory(dataDir);
		System.out.println("Data directory: " + dataDir.getAbsolutePath());

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
		JavaContainer container = createJavaContainer(javaImage, reportsDir, dataDir);
		try {
			container.start();
			testMethod.test(container);
		} finally {
			container.stop();
		}
	}

	private void java_version(JavaContainer container) {

		// prepare
		String javaVersion = container.getJavaImage().getJavaVersion();
		String outputPath = "reports/" + javaVersion + "/java-version.txt";
		String expectedOutput = readResource(outputPath);
		Command command = Command.java("-version");

		// test
		ExecResult result = container.exec(command);

		if (TestUtils.createResources()) {
			String actualOutput = result.getStderr();
			if (!actualOutput.equals(expectedOutput)) {
				writeProjectFile("src/releaseTest/resources/" + outputPath, actualOutput);
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
		String javaVersion = container.getJavaImage().getJavaVersion();
		String reportPath = "reports/" + javaVersion + "/java-runtime.txt";
		Command command = Command.jarHc(
				"-o", reportPath,
				"-s", "jr", // include only section Java Runtime
				"jarhc.jar" // TODO: find a tiny artifact to scan
		);

		// override JarHC version for reproducible test output
		command.addJavaArguments("-Djarhc.version.override=0.0.1");

		// test
		jarhcTest(container, command, reportPath);

	}

	private void jarhc_forASM(JavaContainer container) {

		// prepare
		String javaVersion = container.getJavaImage().getJavaVersion();
		String reportPath = "reports/" + javaVersion + "/asm.txt";
		Command command = Command.jarHc(
				"-o", reportPath,
				"-s", "-jr", // exclude section Java Runtime
				"org.ow2.asm:asm:9.2"
		);

		// override JarHC version for reproducible test output
		command.addJavaArguments("-Djarhc.version.override=0.0.1");

		// test
		jarhcTest(container, command, reportPath);

	}

	private void jarhc_forJarHC_fat(JavaContainer container) {

		// prepare
		String javaVersion = container.getJavaImage().getJavaVersion();
		String reportPath = "reports/" + javaVersion + "/jarhc-fat.txt";
		Command command = Command.jarHc(
				"-o", reportPath,
				"-s", "-jr", // exclude section Java Runtime
				"jarhc-with-deps.jar"
		);

		// override JarHC version for reproducible test output
		command.addJavaArguments("-Djarhc.version.override=0.0.1");

		// test
		jarhcTest(container, command, reportPath);

	}

	private void jarhc_forJarHC_artifacts(JavaContainer container) {

		// prepare
		String javaVersion = container.getJavaImage().getJavaVersion();
		String reportPath = "reports/" + javaVersion + "/jarhc-artifacts.txt";
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
		jarhcTest(container, command, reportPath);

	}

	private void jarhc_forJarHC_provided(JavaContainer container) {

		// prepare
		String javaVersion = container.getJavaImage().getJavaVersion();
		String reportPath = "reports/" + javaVersion + "/jarhc-provided.txt";
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
		jarhcTest(container, command, reportPath);

	}

	private void jarhcTest(JavaContainer container, Command command, String reportPath) {

		// make sure that report directory exists
		File reportFile = new File(outputDir, reportPath);
		File reportDir = reportFile.getParentFile();
		createDirectory(reportDir);

		// prepare
		String expectedOutput = readResource("stdout.txt");

		// test
		ExecResult result = container.exec(command);

		// assert
		assertThat(result).isEqualTo(0, expectedOutput, "");

		// if test resources have not been overwritten ..
		if (!TestUtils.createResources()) {
			// compare generated report with expected report
			File actualReport = new File(outputDir, reportPath);
			File expectedReport = new File(resourcesDir, reportPath);
			Assertions.assertThat(actualReport).hasSameTextualContentAs(expectedReport, StandardCharsets.UTF_8);
		}

	}

	private void createDirectory(File directory) {
		if (!directory.exists()) {
			boolean created = directory.mkdirs();
			assumeTrue(created, "Directory has been created: " + directory.getAbsolutePath());
		}
	}

}
