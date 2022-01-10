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
import org.jarhc.test.release.utils.AbstractTestRunner;
import org.jarhc.test.release.utils.Command;
import org.jarhc.test.release.utils.DockerTestRunner;
import org.jarhc.test.release.utils.JavaImage;
import org.jarhc.test.release.utils.LocalTestRunner;
import org.jarhc.test.release.utils.TestResult;
import org.jarhc.test.release.utils.TestUtils;
import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.io.TempDir;

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

	@TestFactory
	Collection<DynamicContainer> test(@TempDir Path tempDir) {

		// make sure that data directory exists
		// note: all runners and tests will use the same data directory
		File dataDir = new File(tempDir.toFile(), "data");
		createDirectory(dataDir);
		System.out.println("Data directory: " + dataDir.getAbsolutePath());

		// add a local test runner first
		List<AbstractTestRunner> runners = new ArrayList<>();
		{
			File workDir = new File(tempDir.toFile(), "local");
			LocalTestRunner runner = new LocalTestRunner(workDir, dataDir);
			runners.add(runner);
		}

		// add a Docker-based test runner for every Java image
		String imageNameFilter = System.getProperty("jarhc.test.docker.filter", "eclipse-temurin");
		System.out.println("Docker image name filter: " + imageNameFilter);

		for (JavaImage javaImage : JAVA_IMAGES) {
			String imageName = javaImage.getImageName();

			if (imageNameFilter.equals("all") || imageName.contains(imageNameFilter) || imageName.matches(imageNameFilter)) {
				System.out.println("ENABLED ... " + imageName);

				File workDir = new File(tempDir.toFile(), javaImage.getPath());
				DockerTestRunner runner = new DockerTestRunner(javaImage, workDir, dataDir);
				runners.add(runner);

			} else {
				System.out.println("SKIP ...... " + imageName);
			}
		}

		List<DynamicContainer> containers = new ArrayList<>();

		File jarFile = getProjectFile("build/libs/jarhc-" + getJarHcVersion() + ".jar");
		File jarWithDepsFile = getProjectFile("build/libs/jarhc-" + getJarHcVersion() + "-with-deps.jar");

		for (AbstractTestRunner runner : runners) {

			runner.installFile(jarFile, "jarhc.jar");
			runner.installFile(jarWithDepsFile, "jarhc-with-deps.jar");

			// prepare a collection of tests
			List<DynamicTest> tests = new ArrayList<>();
			tests.add(DynamicTest.dynamicTest("Java Version", () -> run(runner, this::java_version)));
			tests.add(DynamicTest.dynamicTest("JarHC Version", () -> run(runner, this::jarhc_version)));
			tests.add(DynamicTest.dynamicTest("JarHC Help", () -> run(runner, this::jarhc_help)));
			tests.add(DynamicTest.dynamicTest("JarHC Java Runtime", () -> run(runner, this::jarhc_javaRuntime)));
			tests.add(DynamicTest.dynamicTest("JarHC for ASM", () -> run(runner, this::jarhc_forASM)));
			tests.add(DynamicTest.dynamicTest("JarHC for JarHC (fat)", () -> run(runner, this::jarhc_forJarHC_fat)));
			tests.add(DynamicTest.dynamicTest("JarHC for JarHC (artifacts)", () -> run(runner, this::jarhc_forJarHC_artifacts)));
			tests.add(DynamicTest.dynamicTest("JarHC for JarHC (provided)", () -> run(runner, this::jarhc_forJarHC_provided)));

			// add all tests to a test container for grouping
			containers.add(DynamicContainer.dynamicContainer(runner.getName(), tests));
		}

		// return test containers
		return containers;
	}

	@FunctionalInterface
	private interface TestMethod {
		void test(AbstractTestRunner runner);
	}

	private void run(AbstractTestRunner runner, TestMethod testMethod) {
		testMethod.test(runner);
	}

	private void java_version(AbstractTestRunner runner) {

		// prepare
		String resourcePath = runner.findResourcePath("java-version.txt");
		String expectedOutput = readResource(resourcePath);
		Command command = Command.java("-version");

		// test
		TestResult result = runner.execute(command);

		if (TestUtils.createResources()) {
			String actualOutput = result.getStdErr();
			if (!actualOutput.equals(expectedOutput)) {
				writeProjectFile("src/releaseTest/resources/" + resourcePath, actualOutput);
				return;
			}
		}

		// assert
		result.assertEquals(0, "", expectedOutput);

	}

	private void jarhc_version(AbstractTestRunner runner) {

		// prepare
		Command command = Command.jarHc("--version");
		String expectedOutput = String.format("JarHC - JAR Health Check %s\n", getJarHcVersion());

		// test
		TestResult result = runner.execute(command);

		// assert
		result.assertEquals(0, expectedOutput, "");

	}

	private void jarhc_help(AbstractTestRunner runner) {

		// prepare
		Command command = Command.jarHc("--help");
		String expectedOutput = readResource("help.txt");

		// test
		TestResult result = runner.execute(command);

		// assert
		result.assertEquals(0, expectedOutput, "");

	}

	private void jarhc_javaRuntime(AbstractTestRunner runner) {

		// prepare
		String resourceName = "java-runtime.txt";
		String outputPath = runner.getOutputPath(resourceName);
		Command command = Command.jarHc(
				"-o", outputPath,
				"-s", "jr", // include only section Java Runtime
				"jarhc.jar" // TODO: find a tiny artifact to scan
		);

		// override JarHC version for reproducible test output
		command.addJavaArguments("-Djarhc.version.override=0.0.1");

		testJarHC(runner, command, resourceName);

	}

	private void jarhc_forASM(AbstractTestRunner runner) {

		// prepare
		String resourceName = "asm.txt";
		String outputPath = runner.getOutputPath(resourceName);
		Command command = Command.jarHc(
				"-o", outputPath,
				"-s", "-jr", // exclude section Java Runtime
				"org.ow2.asm:asm:9.2"
		);

		// override JarHC version for reproducible test output
		command.addJavaArguments("-Djarhc.version.override=0.0.1");

		testJarHC(runner, command, resourceName);

	}

	private void jarhc_forJarHC_fat(AbstractTestRunner runner) {

		// prepare
		String resourceName = "jarhc-fat.txt";
		String outputPath = runner.getOutputPath(resourceName);
		Command command = Command.jarHc(
				"-o", outputPath,
				"-s", "-jr", // exclude section Java Runtime
				"jarhc-with-deps.jar"
		);

		// override JarHC version for reproducible test output
		command.addJavaArguments("-Djarhc.version.override=0.0.1");

		testJarHC(runner, command, resourceName);

	}

	private void jarhc_forJarHC_artifacts(AbstractTestRunner runner) {

		// prepare
		String resourceName = "jarhc-artifacts.txt";
		String outputPath = runner.getOutputPath(resourceName);
		String dependencies = getDependencies("runtimeClasspath");
		Command command = Command.jarHc(
				"-o", outputPath,
				"-s", "-jr", // exclude section Java Runtime
				"jarhc.jar",
				"--classpath", dependencies
		);

		// override JarHC version for reproducible test output
		command.addJavaArguments("-Djarhc.version.override=0.0.1");

		testJarHC(runner, command, resourceName);

	}

	private void jarhc_forJarHC_provided(AbstractTestRunner runner) {

		// prepare
		String resourceName = "jarhc-provided.txt";
		String outputPath = runner.getOutputPath(resourceName);
		String dependencies = getDependencies("implementation");
		Command command = Command.jarHc(
				"-o", outputPath,
				"-s", "-jr", // exclude section Java Runtime
				"jarhc.jar",
				"--provided", dependencies
		);

		// override JarHC version for reproducible test output
		command.addJavaArguments("-Djarhc.version.override=0.0.1");

		testJarHC(runner, command, resourceName);

	}

	private void testJarHC(AbstractTestRunner runner, Command command, String resourceName) {

		// assume
		String resourcePath = runner.findResourcePath(resourceName);
		File resourcesDir = getProjectDirectory("src/releaseTest/resources");
		File resourceFile = new File(resourcesDir, resourcePath);
		assumeTrue(resourceFile.isFile(), "Report found in test resources: " + resourcePath);

		// prepare
		String expectedOutput = readResource("stdout.txt");

		// test
		TestResult result = runner.execute(command);

		// assert
		result.assertEquals(0, expectedOutput, "");

		File outputFile = runner.getOutputFile(resourceName);
		TestUtils.normalizeReport(outputFile);

		// if test resources should be overwritten ..
		if (TestUtils.createResources()) {
			try {
				if (!FileUtils.contentEquals(outputFile, resourceFile)) {
					FileUtils.copyFile(outputFile, resourceFile);
					return;
				}
			} catch (IOException e) {
				throw new AssertionError("Unexpected I/O error.", e);
			}
		}

		// compare generated report with expected report
		Assertions.assertThat(outputFile).hasSameTextualContentAs(resourceFile, StandardCharsets.UTF_8);

	}

}
