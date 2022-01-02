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
import org.jarhc.test.release.utils.JarHcContainer;
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

			// TODO: #74 enable after support for Java 17 has been fixed
			// tests.add(DynamicTest.dynamicTest("JarHC for JarHC", () -> runInContainer(javaImageName, this::jarhcJarHC)));

			// add all tests to a test container for grouping
			containers.add(DynamicContainer.dynamicContainer(javaImageName, tests));
		}

		// return test containers
		return containers;
	}

	private void runInContainer(String javaImageName, Consumer<JarHcContainer> consumer) {
		JarHcContainer container = createJarHcContainer(javaImageName);
		try {
			container.start();
			consumer.accept(container);
		} finally {
			container.stop();
		}
	}

	private void javaVersion(JarHcContainer container) {

		// test
		ExecResult result = container.execJava("-version");

		// assert
		// TODO: check actual Java version
		Assertions.assertThat(result.getExitCode()).isEqualTo(0);
		Assertions.assertThat(result.getStdout()).isEmpty();
		Assertions.assertThat(result.getStderr()).contains("Temurin");

	}

	private void jarhcVersion(JarHcContainer container) {

		// prepare
		String output = String.format("JarHC - JAR Health Check %s\n", getJarHcVersion());

		// test
		ExecResult result = container.execJarHc("--version");

		// assert
		assertThat(result).isEqualTo(0, output, "");

	}

	private void jarhcHelp(JarHcContainer container) {

		// prepare
		String output = readProjectFile("src/main/resources/usage.txt");

		// test
		ExecResult result = container.execJarHc("--help");

		// assert
		Assertions.assertThat(result.getExitCode()).isEqualTo(0);
		Assertions.assertThat(result.getStdout()).startsWith(output);
		Assertions.assertThat(result.getStderr()).isEmpty();

	}

	private void jarhcASM(JarHcContainer container) {

		// prepare
		String output = readResource("asm.txt");

		// test
		ExecResult result = container.execJarHc("-s", "-jr", "org.ow2.asm:asm:9.2");

		// assert
		assertThat(result).isEqualTo(0, output, "");

	}

	private void jarhcJarHC(JarHcContainer container) {

		// prepare
		String output = readResource("jarhc.txt");

		// test
		ExecResult result = container.execJarHc("-s", "-jr", "jarhc.jar");

		// assert
		assertThat(result).isEqualTo(0, output, "");

	}

}
