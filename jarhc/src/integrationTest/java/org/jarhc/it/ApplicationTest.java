/*
 * Copyright 2018 Stephan Markwalder
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

package org.jarhc.it;

import static org.jarhc.test.log.LoggerAssertions.assertLogger;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import org.jarhc.TestUtils;
import org.jarhc.app.Application;
import org.jarhc.app.Options;
import org.jarhc.artifacts.ArtifactFinder;
import org.jarhc.artifacts.MavenRepository;
import org.jarhc.it.utils.ArtifactFinderMock;
import org.jarhc.test.JavaRuntimeMock;
import org.jarhc.test.PrintStreamBuffer;
import org.jarhc.test.TextUtils;
import org.jarhc.test.log.LoggerBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;

class ApplicationTest {

	PrintStreamBuffer out = new PrintStreamBuffer();
	Logger applicationLogger = LoggerBuilder.collect(Application.class);
	Logger mavenRepositoryLogger = LoggerBuilder.collect(MavenRepository.class);

	Application application = new Application(applicationLogger);

	@TempDir
	Path tempDir;

	@BeforeEach
	void setUp() {
		application.setOut(out);
		application.setJavaRuntimeFactory(JavaRuntimeMock::getOracleRuntime);

		ArtifactFinder artifactFinder = ArtifactFinderMock.getArtifactFinder();
		MavenRepository repository = new MavenRepository(11, TestUtils.getFileRepositorySettings(), tempDir.toString(), artifactFinder, mavenRepositoryLogger);
		application.setRepository(repository);
	}

	@Test
	void test() throws IOException {

		// prepare
		Options options = new Options();
		File file = TestUtils.getResourceAsFile("/org/jarhc/it/ApplicationTest/a.jar", tempDir);
		options.addClasspathJarPath(file.getAbsolutePath());

		// test
		int exitCode = application.run(options);

		// assert
		assertEquals(0, exitCode);

		String output = out.getText();

		if (TestUtils.createResources()) {
			TestUtils.saveResource("integrationTest", "/org/jarhc/it/ApplicationTest/result.txt", output, "UTF-8");
			return;
		}

		String expectedOutput = TestUtils.getResourceAsString("/org/jarhc/it/ApplicationTest/result.txt", "UTF-8");

		// normalize output
		output = TextUtils.toUnixLineSeparators(output);
		expectedOutput = TextUtils.toUnixLineSeparators(expectedOutput);

		assertEquals(expectedOutput, output);

		assertLogger(applicationLogger)
				.hasDebug("Time: *")
				.isEmpty();

		assertLogger(mavenRepositoryLogger)
				.hasDebug("Find artifact: b2de6f7c6eff51a28729be9c4f6555354f16a1ca")
				.isEmpty();

	}

	@Test
	void test_options() throws IOException {

		// prepare
		Options options = new Options();
		options.addClasspathJarPath("org.ow2.asm:asm-tree:7.0");
		options.addClasspathJarPath("org.ow2.asm:asm-commons:7.0");
		options.addProvidedJarPath("org.ow2.asm:asm:7.0");
		options.setSkipEmpty(true);
		options.setSortRows(true);

		// test
		int exitCode = application.run(options);

		// assert
		assertEquals(0, exitCode);

		String output = out.getText();

		if (TestUtils.createResources()) {
			TestUtils.saveResource("integrationTest", "/org/jarhc/it/ApplicationTest/result_options.txt", output, "UTF-8");
			return;
		}

		String expectedOutput = TestUtils.getResourceAsString("/org/jarhc/it/ApplicationTest/result_options.txt", "UTF-8");

		// normalize output
		output = TextUtils.toUnixLineSeparators(output);
		expectedOutput = TextUtils.toUnixLineSeparators(expectedOutput);

		assertEquals(expectedOutput, output);

		assertLogger(applicationLogger)
				.hasDebug("Time: *")
				.isEmpty();

		assertLogger(mavenRepositoryLogger)
				.inAnyOrder()
				.hasDebug("Download artifact: org.ow2.asm:asm:7.0:jar")
				.hasDebug("Find artifact: d74d4ba0dee443f68fb2dcb7fcdb945a2cd89912")
				.hasDebug("Download artifact: org.ow2.asm:asm-commons:7.0:jar")
				.hasDebug("Download artifact: org.ow2.asm:asm-tree:7.0:jar")
				.hasDebug("Find artifact: 29bc62dcb85573af6e62e5b2d735ef65966c4180")
				.hasDebug("Find artifact: 478006d07b7c561ae3a92ddc1829bca81ae0cdd1")
				.hasDebug("Get dependencies: org.ow2.asm:asm-commons:7.0:jar")
				.hasDebug("Get dependencies: org.ow2.asm:asm-tree:7.0:jar")
				.hasDebug("Get versions: org.ow2.asm:asm-commons")
				.hasDebug("Get versions: org.ow2.asm:asm-tree")
				.isEmpty();

	}

}
