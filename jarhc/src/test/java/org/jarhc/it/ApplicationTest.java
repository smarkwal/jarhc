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

import static org.assertj.core.api.Assertions.assertThat;
import static org.jarhc.test.log.LoggerAssertions.assertLogger;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.jarhc.TestUtils;
import org.jarhc.app.Application;
import org.jarhc.app.Options;
import org.jarhc.artifacts.ArtifactFinder;
import org.jarhc.artifacts.MavenRepository;
import org.jarhc.it.utils.ArtifactFinderMock;
import org.jarhc.test.JavaRuntimeMock;
import org.jarhc.test.PrintStreamBuffer;
import org.jarhc.test.log.LoggerBuilder;
import org.jarhc.utils.JarHcException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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
			TestUtils.saveResource("test", "/org/jarhc/it/ApplicationTest/result.txt", output, "UTF-8");
			return;
		}

		String expectedOutput = TestUtils.getResourceAsString("/org/jarhc/it/ApplicationTest/result.txt", "UTF-8");
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

		// test
		int exitCode = application.run(options);

		// assert
		assertEquals(0, exitCode);

		String output = out.getText();

		if (TestUtils.createResources()) {
			TestUtils.saveResource("test", "/org/jarhc/it/ApplicationTest/result_options.txt", output, "UTF-8");
			return;
		}

		String expectedOutput = TestUtils.getResourceAsString("/org/jarhc/it/ApplicationTest/result_options.txt", "UTF-8");
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

	@Test
	void test_sectionNotFound() {

		// prepare: section "foo" does not exist
		Options options = new Options();
		options.setSections(List.of("foo"));

		// test
		int exitCode = application.run(options);

		// assert
		assertEquals(3, exitCode);

		String output = out.getText();
		assertEquals("JarHC - JAR Health Check 0.0.1\n==============================\n\nLoad JAR files ...\nScan JAR files ...\nAnalyze classpath ...\n", output);

		assertLogger(applicationLogger)
				.hasError("Analyzer not found: foo")
				.isEmpty();

		assertLogger(mavenRepositoryLogger)
				.isEmpty();
	}

	@Test
	void test_reportDirNotFound() {

		// prepare: directory "foo" does not exist
		File reportFile = new File("foo/report.txt");
		Options options = new Options();
		options.addReportFile(reportFile.getPath());

		// test
		int exitCode = application.run(options);

		// assert
		assertEquals(2, exitCode);

		String output = out.getText();
		assertEquals("JarHC - JAR Health Check 0.0.1\n==============================\n\nLoad JAR files ...\nScan JAR files ...\nAnalyze classpath ...\nCreate report ...\n\n", output);

		assertLogger(applicationLogger)
				.hasDebug("Time: *")
				.hasError("Internal error while generating report.", new JarHcException("I/O error for file '" + reportFile.getAbsolutePath() + "'"))
				.isEmpty();

		assertLogger(mavenRepositoryLogger)
				.isEmpty();
	}

	@Test
	void test_directoryPath() throws IOException {

		// prepare
		Path libsDir = tempDir.resolve("libs");
		Files.createDirectory(libsDir);

		Options options = new Options();
		TestUtils.getResourceAsFile("/org/jarhc/it/ApplicationTest/a.jar", libsDir);
		options.addClasspathJarPath(libsDir.toAbsolutePath().toString());

		// test
		int exitCode = application.run(options);

		// assert
		assertEquals(0, exitCode);

		String output = out.getText();
		assertThat(output)
				.startsWith("JarHC - JAR Health Check 0.0.1")
				.contains("a.jar")
				.contains("b2de6f7c6eff51a28729be9c4f6555354f16a1ca");

		assertLogger(applicationLogger)
				.hasDebug("Time: *")
				.isEmpty();

		assertLogger(mavenRepositoryLogger)
				.hasDebug("Find artifact: b2de6f7c6eff51a28729be9c4f6555354f16a1ca")
				.isEmpty();
	}

	@Test
	void test_runtimePath() throws IOException {

		// prepare
		Path libsDir = tempDir.resolve("libs");
		Files.createDirectory(libsDir);

		Options options = new Options();
		TestUtils.getResourceAsFile("/org/jarhc/it/ApplicationTest/a.jar", libsDir);
		options.addRuntimeJarPath(libsDir.toAbsolutePath().toString());

		// test
		int exitCode = application.run(options);

		// assert
		assertEquals(0, exitCode);

		String output = out.getText();
		assertThat(output)
				.startsWith("JarHC - JAR Health Check 0.0.1")
				.contains("Java runtime : [unknown]");

		assertLogger(applicationLogger)
				.hasDebug("Time: *")
				.isEmpty();

		assertLogger(mavenRepositoryLogger)
				.isEmpty();
	}

	@ParameterizedTest
	@CsvSource({
			"report.txt,Artifact  | Version | Source",
			"report.html,<meta name=\"generator\" content=\"JarHC 0.0.1\">",
			"report-list.txt,Artifact: Classpath",
			"report.json,\"title\": \"JAR Health Check Report\""
	})
	void test_report(String reportFileName, String expectedContent) {

		// prepare
		Options options = new Options();
		File reportFile = tempDir.resolve(reportFileName).toFile();
		options.addReportFile(reportFile.getAbsolutePath());

		// test
		int exitCode = application.run(options);

		// assert
		assertEquals(0, exitCode);

		String output = out.getText();
		assertEquals("JarHC - JAR Health Check 0.0.1\n==============================\n\nLoad JAR files ...\nScan JAR files ...\nAnalyze classpath ...\nCreate report ...\n\n", output);

		// assert
		assertThat(reportFile)
				.isFile()
				.content()
				.contains(expectedContent);

		assertLogger(applicationLogger)
				.hasDebug("Time: *")
				.isEmpty();

		assertLogger(mavenRepositoryLogger)
				.isEmpty();
	}

}
