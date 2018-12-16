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

	@Test
	void test_no_arguments() {

		// prepare
		PrintStreamBuffer err = new PrintStreamBuffer();
		CommandLineParser parser = new CommandLineParser(err);

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
		PrintStreamBuffer err = new PrintStreamBuffer();
		CommandLineParser parser = new CommandLineParser(err);
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
		PrintStreamBuffer err = new PrintStreamBuffer();
		CommandLineParser parser = new CommandLineParser(err);
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
		PrintStreamBuffer err = new PrintStreamBuffer();
		CommandLineParser parser = new CommandLineParser(err);
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
		assertEquals(-4, exception.getExitCode());
		assertEquals("No *.jar files found in path.", exception.getMessage());
		assertTrue(err.getText().startsWith("No *.jar files found in path."));

	}

	@Test
	void test_no_report_format() {

		// prepare
		PrintStreamBuffer err = new PrintStreamBuffer();
		CommandLineParser parser = new CommandLineParser(err);

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

		// prepare
		PrintStreamBuffer err = new PrintStreamBuffer();
		CommandLineParser parser = new CommandLineParser(err);

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

		// prepare
		PrintStreamBuffer err = new PrintStreamBuffer();
		CommandLineParser parser = new CommandLineParser(err);

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

		// prepare
		PrintStreamBuffer err = new PrintStreamBuffer();
		CommandLineParser parser = new CommandLineParser(err);

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
		PrintStreamBuffer err = new PrintStreamBuffer();
		CommandLineParser parser = new CommandLineParser(err);
		File file = TestUtils.getResourceAsFile("/CommandLineParserTest/a.jar", tempDir);

		// test
		Options options = parser.parse(new String[]{"-f", "text", "-o", "report.txt", file.getAbsolutePath()});

		// assert
		assertNotNull(options);
		assertEquals("", err.getText());

		List<File> files = options.getJarFiles();
		assertEquals(1, files.size());
		assertEquals(file, files.get(0));

		assertEquals("text", options.getReportFormat());
		assertEquals("report.txt", options.getReportFile());

	}

	@Test
	void test_one_jar_file_in_directory(@TempDir Path tempDir) throws IOException, CommandLineException {

		// prepare
		PrintStreamBuffer err = new PrintStreamBuffer();
		CommandLineParser parser = new CommandLineParser(err);
		File file = TestUtils.getResourceAsFile("/CommandLineParserTest/a.jar", tempDir);
		File directory = file.getParentFile();

		// test
		Options options = parser.parse(new String[]{"-f", "html", directory.getAbsolutePath()});

		// assert
		assertNotNull(options);
		assertEquals("", err.getText());

		List<File> files = options.getJarFiles();
		assertEquals(1, files.size());
		assertEquals(file, files.get(0));

		assertEquals("html", options.getReportFormat());
		assertNull(options.getReportFile());

	}

	@Test
	void test_two_jar_files(@TempDir Path tempDir) throws IOException, CommandLineException {

		// prepare
		PrintStreamBuffer err = new PrintStreamBuffer();
		CommandLineParser parser = new CommandLineParser(err);
		File file1 = TestUtils.getResourceAsFile("/CommandLineParserTest/a.jar", tempDir);
		File file2 = TestUtils.getResourceAsFile("/CommandLineParserTest/a.jar", tempDir); // TODO: use a different JAR file here

		// test
		Options options = parser.parse(new String[]{"-o", "report.html", file1.getAbsolutePath(), file2.getAbsolutePath()});

		// assert
		assertNotNull(options);
		assertEquals("", err.getText());

		List<File> files = options.getJarFiles();
		assertEquals(2, files.size());
		assertEquals(file1, files.get(0));
		assertEquals(file2, files.get(1));

		assertEquals("html", options.getReportFormat());
		assertEquals("report.html", options.getReportFile());

	}

}
