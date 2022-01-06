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
import static org.jarhc.test.release.utils.TestUtils.createResources;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.jarhc.test.release.utils.Command;
import org.jarhc.test.release.utils.JavaContainer;
import org.jarhc.test.release.utils.JavaImage;
import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.testcontainers.containers.Container.ExecResult;

class JarHcTest extends ReleaseTest {

	private static final JavaImage[] JAVA_IMAGES = {
			new JavaImage("8", "eclipse-temurin:8-jre"),
			new JavaImage("11", "eclipse-temurin:11-jre"),
			new JavaImage("17", "eclipse-temurin:17-jre"),
			// TODO: IBM images?
	};

	@TestFactory
	Collection<DynamicContainer> test() {

		List<DynamicContainer> containers = new ArrayList<>();

		// for every Java Docker image
		for (JavaImage javaImage : JAVA_IMAGES) {

			// prepare a collection of tests
			List<DynamicTest> tests = new ArrayList<>();
			tests.add(DynamicTest.dynamicTest("Java Version", () -> runInContainer(javaImage, this::javaVersion)));
			tests.add(DynamicTest.dynamicTest("JarHC Version", () -> runInContainer(javaImage, this::jarhcVersion)));
			tests.add(DynamicTest.dynamicTest("JarHC Help", () -> runInContainer(javaImage, this::jarhcHelp)));
			tests.add(DynamicTest.dynamicTest("JarHC for ASM", () -> runInContainer(javaImage, this::jarhcASM)));
			tests.add(DynamicTest.dynamicTest("JarHC for JarHC (fat)", () -> runInContainer(javaImage, this::jarhcJarHC_fat)));
			tests.add(DynamicTest.dynamicTest("JarHC for JarHC (artifacts)", () -> runInContainer(javaImage, this::jarhcJarHC_artifacts)));
			tests.add(DynamicTest.dynamicTest("JarHC for JarHC (provided)", () -> runInContainer(javaImage, this::jarhcJarHC_provided)));

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
		JavaContainer container = createJavaContainer(javaImage);
		try {
			container.start();
			testMethod.test(container);
		} finally {
			container.stop();
		}
	}

	private void javaVersion(JavaContainer container) {

		// prepare
		Command command = Command.java("-version");

		// test
		ExecResult result = container.exec(command);

		// assert
		// TODO: check actual Java version
		Assertions.assertThat(result.getExitCode()).isEqualTo(0);
		Assertions.assertThat(result.getStdout()).isEmpty();
		Assertions.assertThat(result.getStderr()).contains("Temurin");

	}

	private void jarhcVersion(JavaContainer container) {

		// prepare
		Command command = Command.jarHc("--version");
		String expectedOutput = String.format("JarHC - JAR Health Check %s\n", getJarHcVersion());

		// test
		ExecResult result = container.exec(command);

		// assert
		assertThat(result).isEqualTo(0, expectedOutput, "");

	}

	private void jarhcHelp(JavaContainer container) {

		// prepare
		Command command = Command.jarHc("--help");

		// test
		jarhcTest(container, command, "help.txt");

	}

	private void jarhcASM(JavaContainer container) {

		// prepare
		Command command = Command.jarHc("org.ow2.asm:asm:9.2");

		// override JarHC version for reproducible test output
		command.addJavaArguments("-Djarhc.version.override=0.0.1");

		// test
		jarhcTest(container, command, "asm-{java.version}.txt");

	}

	private void jarhcJarHC_fat(JavaContainer container) {

		// prepare
		Command command = Command.jarHc("jarhc-with-deps.jar");

		// override JarHC version for reproducible test output
		command.addJavaArguments("-Djarhc.version.override=0.0.1");

		// test
		jarhcTest(container, command, "jarhc-fat-{java.version}.txt");

	}

	private void jarhcJarHC_artifacts(JavaContainer container) {

		// prepare
		String dependencies = getDependencies("runtimeClasspath");
		Command command = Command.jarHc("jarhc.jar", "--classpath", dependencies);

		// override JarHC version for reproducible test output
		command.addJavaArguments("-Djarhc.version.override=0.0.1");

		// test
		jarhcTest(container, command, "jarhc-artifacts-{java.version}.txt");

	}

	private void jarhcJarHC_provided(JavaContainer container) {

		// prepare
		String dependencies = getDependencies("implementation");
		Command command = Command.jarHc("jarhc.jar", "--provided", dependencies);

		// override JarHC version for reproducible test output
		command.addJavaArguments("-Djarhc.version.override=0.0.1");

		// test
		jarhcTest(container, command, "jarhc-provided-{java.version}.txt");

	}

	private void jarhcTest(JavaContainer container, Command command, String resource) {

		// replace Java version placeholder in resource name (if present)
		resource = resource.replace("{java.version}", container.getJavaImage().getJavaVersion());

		// prepare
		String expectedOutput = readResource(resource);

		// test
		ExecResult result = container.exec(command);

		// update test resource?
		if (createResources()) {
			String actualOutput = result.getStdout();
			if (!actualOutput.equals(expectedOutput)) {
				String resourceFile = String.format("src/releaseTest/resources/%s", resource);
				writeProjectFile(resourceFile, actualOutput);
			}
		}

		// assert
		assertThat(result).isEqualTo(0, expectedOutput, "");
	}

}
