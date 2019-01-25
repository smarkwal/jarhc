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

package org.jarhc.app;

import org.jarhc.TestUtils;
import org.jarhc.test.PrintStreamBuffer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.TempDirectory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junitpioneer.jupiter.TempDirectory.TempDir;

@ExtendWith(TempDirectory.class)
class CommandLineParserTest {

	private final PrintStreamBuffer out = new PrintStreamBuffer();
	private final PrintStreamBuffer err = new PrintStreamBuffer();
	private final CommandLineParser parser = new CommandLineParser(out, err);

	@Test
	void test_no_arguments() {

		// prepare

		// test
		CommandLineException exception = null;
		try {
			parser.parse(new String[0]);
		} catch (CommandLineException e) {
			exception = e;
		}

		// assert
		assertNotNull(exception);
		assertEquals(-1, exception.getExitCode());
		assertEquals("Argument <path> is missing.", exception.getMessage());
		assertTrue(err.getText().startsWith("Argument <path> is missing."));

	}

	@Test
	void test_file_not_jar(@TempDir Path tempDir) throws IOException {

		// prepare
		File file = TestUtils.getResourceAsFile("/CommandLineParserTest/Main.java", tempDir);

		// test
		CommandLineException exception = null;
		try {
			parser.parse(new String[]{file.getAbsolutePath()});
		} catch (CommandLineException e) {
			exception = e;
		}

		// assert
		assertNotNull(exception);
		assertEquals(-2, exception.getExitCode());
		assertEquals("File is not a *.jar file: " + file.getAbsolutePath(), exception.getMessage());
		assertTrue(err.getText().startsWith("File is not a *.jar file: " + file.getAbsolutePath()));

	}

	@Test
	void test_file_not_found() {

		// prepare
		File file = new File("file.jar");

		// test
		CommandLineException exception = null;
		try {
			parser.parse(new String[]{file.getAbsolutePath()});
		} catch (CommandLineException e) {
			exception = e;
		}

		// assert
		assertNotNull(exception);
		assertEquals(-3, exception.getExitCode());
		assertEquals("File or directory not found: " + file.getAbsolutePath(), exception.getMessage());
		assertTrue(err.getText().startsWith("File or directory not found: " + file.getAbsolutePath()));

	}

	@Test
	void test_no_files_found(@TempDir Path tempDir) throws IOException {

		// prepare
		File file = TestUtils.getResourceAsFile("/CommandLineParserTest/Main.java", tempDir);
		File directory = file.getParentFile();

		// test
		CommandLineException exception = null;
		try {
			parser.parse(new String[]{directory.getAbsolutePath()});
		} catch (CommandLineException e) {
			exception = e;
		}

		// assert
		assertNotNull(exception);
		assertEquals(-2, exception.getExitCode());
		assertEquals("No *.jar files found in directory: " + directory.getAbsolutePath(), exception.getMessage());
		assertTrue(err.getText().startsWith("No *.jar files found in directory:"));

	}

	@Test
	void test_no_report_format() {

		// test
		CommandLineException exception = null;
		try {
			parser.parse(new String[]{"-f"});
		} catch (CommandLineException e) {
			exception = e;
		}

		// assert
		assertNotNull(exception);
		assertEquals(-5, exception.getExitCode());
		assertEquals("Report format not specified.", exception.getMessage());
		assertTrue(err.getText().startsWith("Report format not specified."));

	}

	@Test
	void test_unknown_report_format() {

		// test
		CommandLineException exception = null;
		try {
			parser.parse(new String[]{"-f", "pdf"});
		} catch (CommandLineException e) {
			exception = e;
		}

		// assert
		assertNotNull(exception);
		assertEquals(-6, exception.getExitCode());
		assertEquals("Unknown report format: 'pdf'.", exception.getMessage());
		assertTrue(err.getText().startsWith("Unknown report format: 'pdf'."));

	}

	@Test
	void test_no_report_file() {

		// test
		CommandLineException exception = null;
		try {
			parser.parse(new String[]{"-o"});
		} catch (CommandLineException e) {
			exception = e;
		}

		// assert
		assertNotNull(exception);
		assertEquals(-7, exception.getExitCode());
		assertEquals("Report file not specified.", exception.getMessage());
		assertTrue(err.getText().startsWith("Report file not specified."));

	}

	@Test
	void test_unknown_option() {

		// test
		CommandLineException exception = null;
		try {
			parser.parse(new String[]{"-u"});
		} catch (CommandLineException e) {
			exception = e;
		}

		// assert
		assertNotNull(exception);
		assertEquals(-100, exception.getExitCode());
		assertEquals("Unknown option: '-u'.", exception.getMessage());
		assertTrue(err.getText().startsWith("Unknown option: '-u'."));

	}

	@Test
	void test_one_jar_file(@TempDir Path tempDir) throws IOException, CommandLineException {

		// prepare
		File file = TestUtils.getResourceAsFile("/CommandLineParserTest/a.jar", tempDir);

		// test
		Options options = parser.parse(new String[]{"-f", "text", "-o", "report.txt", file.getAbsolutePath()});

		// assert
		assertNotNull(options);
		assertEquals("", err.getText());

		List<String> paths = options.getClasspathJarPaths();
		assertEquals(1, paths.size());
		assertEquals(file.getAbsolutePath(), paths.get(0));

		assertEquals("text", options.getReportFormat());
		assertEquals("report.txt", options.getReportFile());

	}

	@Test
	void test_one_jar_file_in_directory(@TempDir Path tempDir) throws IOException, CommandLineException {

		// prepare
		File file = TestUtils.getResourceAsFile("/CommandLineParserTest/a.jar", tempDir);
		File directory = file.getParentFile();

		// test
		Options options = parser.parse(new String[]{"-f", "html", directory.getAbsolutePath()});

		// assert
		assertNotNull(options);
		assertEquals("", err.getText());

		List<String> paths = options.getClasspathJarPaths();
		assertEquals(1, paths.size());
		assertEquals(directory.getAbsolutePath(), paths.get(0));

		assertEquals("html", options.getReportFormat());
		assertNull(options.getReportFile());

	}

	@Test
	void test_two_jar_files(@TempDir Path tempDir) throws IOException, CommandLineException {

		// prepare
		File file1 = TestUtils.getResourceAsFile("/CommandLineParserTest/a.jar", tempDir);
		File file2 = TestUtils.getResourceAsFile("/CommandLineParserTest/a.jar", tempDir); // TODO: use a different JAR file here

		// test
		Options options = parser.parse(new String[]{"-o", "report.html", file1.getAbsolutePath(), file2.getAbsolutePath()});

		// assert
		assertNotNull(options);
		assertEquals("", err.getText());

		List<String> paths = options.getClasspathJarPaths();
		assertEquals(2, paths.size());
		assertEquals(file1.getAbsolutePath(), paths.get(0));
		assertEquals(file2.getAbsolutePath(), paths.get(1));

		assertEquals("html", options.getReportFormat());
		assertEquals("report.html", options.getReportFile());

	}

	@Test
	void test_runtime(@TempDir Path tempDir) throws IOException, CommandLineException {

		// prepare
		File file1 = TestUtils.getResourceAsFile("/CommandLineParserTest/a.jar", tempDir);
		File file2 = TestUtils.getResourceAsFile("/CommandLineParserTest/a.jar", tempDir); // TODO: use a different JAR file here

		// test
		Options options = parser.parse(new String[]{"--classpath", file1.getAbsolutePath(), "--runtime", file2.getAbsolutePath()});

		// assert
		assertNotNull(options);
		assertEquals("", err.getText());

		List<String> paths = options.getClasspathJarPaths();
		assertEquals(1, paths.size());
		assertEquals(file1.getAbsolutePath(), paths.get(0));

		paths = options.getProvidedJarPaths();
		assertEquals(0, paths.size());

		paths = options.getRuntimeJarPaths();
		assertEquals(1, paths.size());
		assertEquals(file2.getAbsolutePath(), paths.get(0));

	}

	@Test
	void test_provided(@TempDir Path tempDir) throws IOException, CommandLineException {

		// prepare
		File file1 = TestUtils.getResourceAsFile("/CommandLineParserTest/a.jar", tempDir);
		File file2 = TestUtils.getResourceAsFile("/CommandLineParserTest/a.jar", tempDir); // TODO: use a different JAR file here

		// test
		Options options = parser.parse(new String[]{"--classpath", file1.getAbsolutePath(), "--provided", file2.getAbsolutePath()});

		// assert
		assertNotNull(options);
		assertEquals("", err.getText());

		List<String> paths = options.getClasspathJarPaths();
		assertEquals(1, paths.size());
		assertEquals(file1.getAbsolutePath(), paths.get(0));

		paths = options.getProvidedJarPaths();
		assertEquals(1, paths.size());
		assertEquals(file2.getAbsolutePath(), paths.get(0));

		paths = options.getRuntimeJarPaths();
		assertEquals(0, paths.size());

	}

	@Test
	void test_provided_runtime(@TempDir Path tempDir) throws IOException, CommandLineException {

		// prepare
		File file1 = TestUtils.getResourceAsFile("/CommandLineParserTest/a.jar", tempDir);
		File file2 = TestUtils.getResourceAsFile("/CommandLineParserTest/a.jar", tempDir); // TODO: use a different JAR file here
		File file3 = TestUtils.getResourceAsFile("/CommandLineParserTest/a.jar", tempDir); // TODO: use a different JAR file here

		// test
		Options options = parser.parse(new String[]{"--classpath", file1.getAbsolutePath(), "--provided", file2.getAbsolutePath(), "--runtime", file3.getAbsolutePath()});

		// assert
		assertNotNull(options);
		assertEquals("", err.getText());

		List<String> paths = options.getClasspathJarPaths();
		assertEquals(1, paths.size());
		assertEquals(file1.getAbsolutePath(), paths.get(0));

		paths = options.getProvidedJarPaths();
		assertEquals(1, paths.size());
		assertEquals(file2.getAbsolutePath(), paths.get(0));

		paths = options.getRuntimeJarPaths();
		assertEquals(1, paths.size());
		assertEquals(file3.getAbsolutePath(), paths.get(0));

	}

	@Test
	void test_title(@TempDir Path tempDir) throws IOException, CommandLineException {

		// prepare
		File file = TestUtils.getResourceAsFile("/CommandLineParserTest/a.jar", tempDir);

		// test
		Options options = parser.parse(new String[]{"-t", "Test Title", file.getAbsolutePath()});

		// assert
		String title = options.getReportTitle();
		assertEquals("Test Title", title);

	}

	@Test
	void test_sections(@TempDir Path tempDir) throws IOException, CommandLineException {

		// prepare
		File file = TestUtils.getResourceAsFile("/CommandLineParserTest/a.jar", tempDir);

		// test
		Options options = parser.parse(new String[]{"-s", "jf,cv,jd", file.getAbsolutePath()});

		// assert
		List<String> sections = options.getSections();
		assertEquals(3, sections.size());
		assertEquals("jf", sections.get(0));
		assertEquals("cv", sections.get(1));
		assertEquals("jd", sections.get(2));

	}

	@Test
	void test_sections_exclude(@TempDir Path tempDir) throws IOException, CommandLineException {

		// prepare
		File file = TestUtils.getResourceAsFile("/CommandLineParserTest/a.jar", tempDir);

		// test
		Options options = parser.parse(new String[]{"-s", "-jf,cv,jd", file.getAbsolutePath()});

		// assert
		List<String> sections = options.getSections();
		assertTrue(!sections.isEmpty());
		assertFalse(sections.contains("jf"));
		assertFalse(sections.contains("cv"));
		assertFalse(sections.contains("jd"));

	}

	@Test
	void test_help() {

		// test
		try {
			parser.parse(new String[]{"-h"});
			fail("CommandLineException not thrown");
		} catch (CommandLineException e) {
			assertEquals("OK", e.getMessage());
			assertEquals(0, e.getExitCode());
		}

		// assert
		assertTrue(out.getText().startsWith("Usage:"));
		assertEquals("", err.getText());

	}

	@Test
	void test_version() {

		// test
		try {
			parser.parse(new String[]{"-v"});
			fail("CommandLineException not thrown");
		} catch (CommandLineException e) {
			assertEquals("OK", e.getMessage());
			assertEquals(0, e.getExitCode());
		}

		// assert
		assertTrue(out.getText().startsWith("JarHC"));
		assertEquals("", err.getText());

	}

}
