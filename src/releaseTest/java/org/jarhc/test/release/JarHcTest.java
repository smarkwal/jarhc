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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import org.assertj.core.api.Assertions;
import org.jarhc.test.release.utils.Command;
import org.jarhc.test.release.utils.JavaContainer;
import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.testcontainers.containers.Container.ExecResult;

class JarHcTest extends ReleaseTest {

	private static final String[] JAVA_IMAGE_NAMES = {
			"eclipse-temurin:8-jre",
			"eclipse-temurin:11-jre",
			"eclipse-temurin:17-jre",
			"eclipse-temurin:latest"
			// TODO: IBM images?
	};

	@TestFactory
	Collection<DynamicContainer> test() {

		List<DynamicContainer> containers = new ArrayList<>();

		// for every Java Docker image
		for (String javaImageName : JAVA_IMAGE_NAMES) {

			// prepare a collection of tests
			List<DynamicTest> tests = new ArrayList<>();
			tests.add(DynamicTest.dynamicTest("Java Version", () -> runInContainer(javaImageName, this::javaVersion)));
			tests.add(DynamicTest.dynamicTest("JarHC Version", () -> runInContainer(javaImageName, this::jarhcVersion)));
			tests.add(DynamicTest.dynamicTest("JarHC Help", () -> runInContainer(javaImageName, this::jarhcHelp)));
			tests.add(DynamicTest.dynamicTest("JarHC for ASM", () -> runInContainer(javaImageName, this::jarhcASM)));
			tests.add(DynamicTest.dynamicTest("JarHC for JarHC", () -> runInContainer(javaImageName, this::jarhcJarHC)));

			// add all tests to a test container for grouping
			containers.add(DynamicContainer.dynamicContainer(javaImageName, tests));
		}

		// return test containers
		return containers;
	}

	private void runInContainer(String javaImageName, Consumer<JavaContainer> consumer) {
		JavaContainer container = createJavaContainer(javaImageName);
		try {
			container.start();
			consumer.accept(container);
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
		String output = String.format("JarHC - JAR Health Check %s\n", getJarHcVersion());

		// test
		ExecResult result = container.exec(command);

		// assert
		assertThat(result).isEqualTo(0, output, "");

	}

	private void jarhcHelp(JavaContainer container) {

		// prepare
		Command command = Command.jarHc("--help");
		String output = readProjectFile("src/main/resources/usage.txt");

		// test
		ExecResult result = container.exec(command);

		// assert
		Assertions.assertThat(result.getExitCode()).isEqualTo(0);
		Assertions.assertThat(result.getStdout()).startsWith(output);
		Assertions.assertThat(result.getStderr()).isEmpty();

	}

	private void jarhcASM(JavaContainer container) {

		// prepare
		Command command = Command.jarHc("-s", "-jr", "org.ow2.asm:asm:9.2");
		String output = readResource("asm.txt");

		// override JarHC version for reproducible test output
		command.addJavaArguments("-Djarhc.version.override=0.0.1");

		// test
		ExecResult result = container.exec(command);

		// assert
		assertThat(result).isEqualTo(0, output, "");

	}

	private void jarhcJarHC(JavaContainer container) {

		// prepare
		Command command = Command.jarHc("-s", "-jr", "jarhc.jar");
		String output = readResource("jarhc.txt");

		// override JarHC version for reproducible test output
		command.addJavaArguments("-Djarhc.version.override=0.0.1");

		// test
		ExecResult result = container.exec(command);

		// assert
		assertThat(result).isEqualTo(0, output, "");

	}

}
