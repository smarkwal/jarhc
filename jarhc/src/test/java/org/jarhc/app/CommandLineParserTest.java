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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.jarhc.TestUtils;
import org.jarhc.java.ClassLoaderStrategy;
import org.jarhc.test.PrintStreamBuffer;
import org.jarhc.utils.JavaUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CommandLineParserTest {

	private final PrintStreamBuffer out = new PrintStreamBuffer();
	private final PrintStreamBuffer err = new PrintStreamBuffer();
	private final Properties properties = new Properties();
	private final CollectionManager collectionManager = mock(CollectionManager.class);
	private final CommandLineParser parser = new CommandLineParser(out, err, properties, collectionManager);

	@BeforeEach
	void setUp() {
		doReturn(true).when(collectionManager).isCollection("servlet-3.1");
		doReturn(List.of("javax.servlet:javax.servlet-api:3.1.0")).when(collectionManager).getCollection("servlet-3.1");
	}

	@Test
	void test_default_options(@TempDir Path tempDir) throws IOException, CommandLineException {

		// prepare
		File file = TestUtils.getResourceAsFile("/org/jarhc/app/CommandLineParserTest/a.jar", tempDir);

		// test
		Options options = parser.parse(new String[] { file.getAbsolutePath() });

		// assert
		assertEquals(JavaUtils.getJavaVersion(), options.getRelease());
		assertFalse(options.getClasspathJarPaths().isEmpty());
		assertTrue(options.getProvidedJarPaths().isEmpty());
		assertTrue(options.getRuntimeJarPaths().isEmpty());
		assertEquals("https://repo1.maven.org/maven2/", options.getRepositoryUrl());
		assertNull(options.getRepositoryUsername());
		assertNull(options.getRepositoryPassword());
		assertFalse(options.isIsolatedScan());
		assertFalse(options.isIgnoreMissingAnnotations());
		assertFalse(options.isIgnoreExactCopy());
		assertNull(options.getSections());
		assertFalse(options.isSkipEmpty());
		assertEquals("JAR Health Check Report", options.getReportTitle());
		assertEquals(Collections.emptyList(), options.getReportFiles());
		assertNull(options.getDataPath());
		assertFalse(options.isDebug());
		assertFalse(options.isTrace());

	}

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
		assertEquals("Argument <artifact> is missing.", exception.getMessage());
		assertTrue(err.getText().startsWith("Argument <artifact> is missing."));

	}

	@Test
	void test_file_not_jar(@TempDir Path tempDir) throws IOException {

		// prepare
		File file = TestUtils.getResourceAsFile("/org/jarhc/app/CommandLineParserTest/Main.java", tempDir);

		// test
		CommandLineException exception = null;
		try {
			parser.parse(new String[] { file.getAbsolutePath() });
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
			parser.parse(new String[] { file.getAbsolutePath() });
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
		File file = TestUtils.getResourceAsFile("/org/jarhc/app/CommandLineParserTest/Main.java", tempDir);
		File directory = file.getParentFile();

		// test
		CommandLineException exception = null;
		try {
			parser.parse(new String[] { directory.getAbsolutePath() });
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
	void test_no_report_file() {

		// test
		CommandLineException exception = null;
		try {
			parser.parse(new String[] { "--output" });
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
			parser.parse(new String[] { "-u" });
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
		File file = TestUtils.getResourceAsFile("/org/jarhc/app/CommandLineParserTest/a.jar", tempDir);

		// test
		Options options = parser.parse(new String[] { "--output", "report.txt", file.getAbsolutePath() });

		// assert
		assertNotNull(options);
		assertEquals("", err.getText());

		List<String> paths = options.getClasspathJarPaths();
		assertEquals(1, paths.size());
		assertEquals(file.getAbsolutePath(), paths.get(0));

		assertEquals(Collections.singletonList("report.txt"), options.getReportFiles());

	}

	@Test
	void test_one_jar_file_in_directory(@TempDir Path tempDir) throws IOException, CommandLineException {

		// prepare
		File file = TestUtils.getResourceAsFile("/org/jarhc/app/CommandLineParserTest/a.jar", tempDir);
		File directory = file.getParentFile();

		// test
		Options options = parser.parse(new String[] { "--output", "report.html", directory.getAbsolutePath() });

		// assert
		assertNotNull(options);
		assertEquals("", err.getText());

		List<String> paths = options.getClasspathJarPaths();
		assertEquals(1, paths.size());
		assertEquals(directory.getAbsolutePath(), paths.get(0));

		assertEquals(Collections.singletonList("report.html"), options.getReportFiles());

	}

	@Test
	void test_two_jar_files(@TempDir Path tempDir) throws IOException, CommandLineException {

		// prepare
		File file1 = TestUtils.getResourceAsFile("/org/jarhc/app/CommandLineParserTest/a.jar", tempDir);
		File file2 = TestUtils.getResourceAsFile("/org/jarhc/app/CommandLineParserTest/a.jar", tempDir); // TODO: use a different JAR file here

		// test
		Options options = parser.parse(new String[] { "--output", "report.html", "--output", "report.txt", file1.getAbsolutePath(), file2.getAbsolutePath() });

		// assert
		assertNotNull(options);
		assertEquals("", err.getText());

		List<String> paths = options.getClasspathJarPaths();
		assertEquals(2, paths.size());
		assertEquals(file1.getAbsolutePath(), paths.get(0));
		assertEquals(file2.getAbsolutePath(), paths.get(1));

		assertEquals(List.of("report.html", "report.txt"), options.getReportFiles());

	}

	@Test
	void test_runtime(@TempDir Path tempDir) throws IOException, CommandLineException {

		// prepare
		File file1 = TestUtils.getResourceAsFile("/org/jarhc/app/CommandLineParserTest/a.jar", tempDir);
		File file2 = TestUtils.getResourceAsFile("/org/jarhc/app/CommandLineParserTest/a.jar", tempDir); // TODO: use a different JAR file here

		// test
		Options options = parser.parse(new String[] { "--classpath", file1.getAbsolutePath(), "--runtime", file2.getAbsolutePath() });

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
		File file1 = TestUtils.getResourceAsFile("/org/jarhc/app/CommandLineParserTest/a.jar", tempDir);
		File file2 = TestUtils.getResourceAsFile("/org/jarhc/app/CommandLineParserTest/a.jar", tempDir); // TODO: use a different JAR file here

		// test
		Options options = parser.parse(new String[] { "--classpath", file1.getAbsolutePath(), "--provided", file2.getAbsolutePath() });

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
		File file1 = TestUtils.getResourceAsFile("/org/jarhc/app/CommandLineParserTest/a.jar", tempDir);
		File file2 = TestUtils.getResourceAsFile("/org/jarhc/app/CommandLineParserTest/a.jar", tempDir); // TODO: use a different JAR file here
		File file3 = TestUtils.getResourceAsFile("/org/jarhc/app/CommandLineParserTest/a.jar", tempDir); // TODO: use a different JAR file here

		// test
		Options options = parser.parse(new String[] { "--classpath", file1.getAbsolutePath(), "--provided", file2.getAbsolutePath(), "--runtime", file3.getAbsolutePath() });

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
	void test_repository(@TempDir Path tempDir) throws IOException, CommandLineException {

		// prepare
		File file = TestUtils.getResourceAsFile("/org/jarhc/app/CommandLineParserTest/a.jar", tempDir);

		// test
		Options options = parser.parse(new String[] {
				"--repository-url", "https://repo.example.com/maven/",
				"--repository-username", "john.smith",
				"--repository-password", "my-secret",
				file.getAbsolutePath()
		});

		// assert
		String url = options.getRepositoryUrl();
		assertEquals("https://repo.example.com/maven/", url);
		String username = options.getRepositoryUsername();
		assertEquals("john.smith", username);
		String password = options.getRepositoryPassword();
		assertEquals("my-secret", password);

	}

	@Test
	void test_repository_properties() throws CommandLineException {

		// prepare
		properties.setProperty("repository.url", "https://repo.example.com/maven/");
		properties.setProperty("repository.username", "john.smith");
		properties.setProperty("repository.password", "my-secret");

		// test
		Options options = parser.parse(new String[] { "servlet-3.1" });

		// assert
		String url = options.getRepositoryUrl();
		assertEquals("https://repo.example.com/maven/", url);
		String username = options.getRepositoryUsername();
		assertEquals("john.smith", username);
		String password = options.getRepositoryPassword();
		assertEquals("my-secret", password);

	}

	@Test
	void test_title(@TempDir Path tempDir) throws IOException, CommandLineException {

		// prepare
		File file = TestUtils.getResourceAsFile("/org/jarhc/app/CommandLineParserTest/a.jar", tempDir);

		// test
		Options options = parser.parse(new String[] { "--title", "Test Title", file.getAbsolutePath() });

		// assert
		String title = options.getReportTitle();
		assertEquals("Test Title", title);

	}

	@Test
	void test_sections(@TempDir Path tempDir) throws IOException, CommandLineException {

		// prepare
		File file = TestUtils.getResourceAsFile("/org/jarhc/app/CommandLineParserTest/a.jar", tempDir);

		// test
		Options options = parser.parse(new String[] { "--sections", "jf,cv,jd", file.getAbsolutePath() });

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
		File file = TestUtils.getResourceAsFile("/org/jarhc/app/CommandLineParserTest/a.jar", tempDir);

		// test
		Options options = parser.parse(new String[] { "--sections", "-jf,cv,jd", file.getAbsolutePath() });

		// assert
		List<String> sections = options.getSections();
		assertFalse(sections.isEmpty());
		assertFalse(sections.contains("jf"));
		assertFalse(sections.contains("cv"));
		assertFalse(sections.contains("jd"));

	}

	@Test
	void test_data(@TempDir Path tempDir) throws IOException, CommandLineException {

		// prepare
		File file = TestUtils.getResourceAsFile("/org/jarhc/app/CommandLineParserTest/a.jar", tempDir);

		// test
		Options options = parser.parse(new String[] { "--data", "/tmp/jarhc", file.getAbsolutePath() });

		// assert
		assertEquals("/tmp/jarhc", options.getDataPath());

	}

	@Test
	void test_isolated_scan(@TempDir Path tempDir) throws IOException, CommandLineException {

		// prepare
		File file = TestUtils.getResourceAsFile("/org/jarhc/app/CommandLineParserTest/a.jar", tempDir);

		// test
		Options options = parser.parse(new String[] { "--isolated-scan", file.getAbsolutePath() });

		// assert
		assertTrue(options.isIsolatedScan());

	}

	@Test
	void test_skip_empty(@TempDir Path tempDir) throws IOException, CommandLineException {

		// prepare
		File file = TestUtils.getResourceAsFile("/org/jarhc/app/CommandLineParserTest/a.jar", tempDir);

		// test
		Options options = parser.parse(new String[] { "--skip-empty", file.getAbsolutePath() });

		// assert
		assertTrue(options.isSkipEmpty());

	}

	@Test
	void test_debug(@TempDir Path tempDir) throws IOException, CommandLineException {

		// prepare
		File file = TestUtils.getResourceAsFile("/org/jarhc/app/CommandLineParserTest/a.jar", tempDir);

		// test
		Options options = parser.parse(new String[] { "--debug", file.getAbsolutePath() });

		// assert
		assertTrue(options.isDebug());

	}

	@Test
	void test_trace(@TempDir Path tempDir) throws IOException, CommandLineException {

		// prepare
		File file = TestUtils.getResourceAsFile("/org/jarhc/app/CommandLineParserTest/a.jar", tempDir);

		// test
		Options options = parser.parse(new String[] { "--trace", file.getAbsolutePath() });

		// assert
		assertTrue(options.isTrace());

	}

	@Test
	void test_ignoreMissingAnnotations(@TempDir Path tempDir) throws IOException, CommandLineException {

		// prepare
		File file = TestUtils.getResourceAsFile("/org/jarhc/app/CommandLineParserTest/a.jar", tempDir);

		// test
		Options options = parser.parse(new String[] { "--ignore-missing-annotations", file.getAbsolutePath() });

		// assert
		assertTrue(options.isIgnoreMissingAnnotations());

	}

	@Test
	void test_ignoreExactCopy(@TempDir Path tempDir) throws IOException, CommandLineException {

		// prepare
		File file = TestUtils.getResourceAsFile("/org/jarhc/app/CommandLineParserTest/a.jar", tempDir);

		// test
		Options options = parser.parse(new String[] { "--ignore-exact-copy", file.getAbsolutePath() });

		// assert
		assertTrue(options.isIgnoreExactCopy());

	}

	@Test
	void test_help() {

		// test
		try {
			parser.parse(new String[] { "--help" });
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
			parser.parse(new String[] { "--version" });
			fail("CommandLineException not thrown");
		} catch (CommandLineException e) {
			assertEquals("OK", e.getMessage());
			assertEquals(0, e.getExitCode());
		}

		// assert
		assertTrue(out.getText().startsWith("JarHC"));
		assertEquals("", err.getText());

	}

	@Test
	void test_strategy_ParentFirst() throws CommandLineException {

		// test
		Options options = parser.parse(new String[] { "--strategy", "ParentFirst", "com.test:test:1.0" });

		// assert
		assertEquals(ClassLoaderStrategy.ParentFirst, options.getClassLoaderStrategy());

	}

	@Test
	void test_strategy_ParentLast() throws CommandLineException {

		// test
		Options options = parser.parse(new String[] { "--strategy", "ParentLast", "com.test:test:1.0" });

		// assert
		assertEquals(ClassLoaderStrategy.ParentLast, options.getClassLoaderStrategy());

	}

	@Test
	void test_strategy_Default() throws CommandLineException {

		// test
		Options options = parser.parse(new String[] { "com.test:test:1.0" });

		// assert
		assertEquals(ClassLoaderStrategy.ParentLast, options.getClassLoaderStrategy());

	}

	@Test
	void test_strategy_Unknown() {

		// test
		try {
			parser.parse(new String[] { "--strategy", "Unknown", "com.test:test:1.0" });
			fail("CommandLineException not thrown");
		} catch (CommandLineException e) {
			assertEquals("Unknown class loader strategy: Unknown", e.getMessage());
			assertEquals(-13, e.getExitCode());
		}

	}

	@Test
	void test_strategy_Incomplete() {

		// test
		try {
			parser.parse(new String[] { "com.test:test:1.0", "--strategy" });
			fail("CommandLineException not thrown");
		} catch (CommandLineException e) {
			assertEquals("Class loader strategy not specified.", e.getMessage());
			assertEquals(-12, e.getExitCode());
		}

	}

	@Test
	void test_release_Default() throws CommandLineException {

		// test
		Options options = parser.parse(new String[] { "com.test:test:1.0" });

		// assert
		assertEquals(JavaUtils.getJavaVersion(), options.getRelease());

	}

	@Test
	void test_release_8() throws CommandLineException {

		// test
		Options options = parser.parse(new String[] { "--release", "8", "com.test:test:1.0" });

		// assert
		assertEquals(8, options.getRelease());

	}

	@Test
	void test_release_11() throws CommandLineException {

		// test
		Options options = parser.parse(new String[] { "--release", "11", "com.test:test:1.0" });

		// assert
		assertEquals(11, options.getRelease());

	}

	@Test
	void test_release_Invalid() {

		// test
		try {
			parser.parse(new String[] { "--release", "latest", "com.test:test:1.0" });
			fail("CommandLineException not thrown");
		} catch (CommandLineException e) {
			assertEquals("Release 'latest' is not valid.", e.getMessage());
			assertEquals(-15, e.getExitCode());
		}

	}

	@Test
	void test_release_Unsupported() {

		// test
		try {
			parser.parse(new String[] { "--release", "5", "com.test:test:1.0" });
			fail("CommandLineException not thrown");
		} catch (CommandLineException e) {
			assertEquals("Release 5 is not supported.", e.getMessage());
			assertEquals(-16, e.getExitCode());
		}

	}

	@Test
	void test_release_Incomplete() {

		// test
		try {
			parser.parse(new String[] { "com.test:test:1.0", "--release" });
			fail("CommandLineException not thrown");
		} catch (CommandLineException e) {
			assertEquals("Release not specified.", e.getMessage());
			assertEquals(-14, e.getExitCode());
		}

	}

	@Test
	void loadOptions(@TempDir Path tempDir) throws IOException, CommandLineException {

		// prepare
		File file = TestUtils.getResourceAsFile("/org/jarhc/app/CommandLineParserTest/options.txt", tempDir);
		Iterator<String> args = List.of(file.getAbsolutePath()).iterator();
		Options options = new Options();

		// test
		parser.loadOptions(args, options);

		// assert
		assertEquals(11, options.getRelease());
		assertEquals("My App 1.0", options.getReportTitle());
		assertEquals(List.of("report.html"), options.getReportFiles());
		assertTrue(options.isSkipEmpty());

	}

	@Test
	void loadOptions_pathNotSpecified() {

		// prepare
		Iterator<String> args = Collections.emptyIterator();
		Options options = new Options();

		// test & assert
		CommandLineException exception = assertThrows(CommandLineException.class, () -> parser.loadOptions(args, options));
		assertEquals("Options path not specified.", exception.getMessage());

	}

	@Test
	void loadOptions_fileNotFound(@TempDir Path tempDir) {

		// prepare
		File file = new File(tempDir.toFile(), "options.txt");
		Iterator<String> args = List.of(file.getAbsolutePath()).iterator();
		Options options = new Options();

		// test
		CommandLineException exception = assertThrows(CommandLineException.class, () -> parser.loadOptions(args, options));
		assertTrue(exception.getMessage().startsWith("Options file not found: "));
		assertTrue(exception.getMessage().endsWith("/options.txt"));

	}

	@Test
	void loadArguments(@TempDir Path tempDir) throws IOException, CommandLineException {

		// prepare
		File file = TestUtils.getResourceAsFile("/org/jarhc/app/CommandLineParserTest/options.txt", tempDir);

		// test
		List<String> arguments = parser.loadArguments(file);

		// assert
		List<String> expected = List.of(
				"--release", "11",
				"--title", "My App 1.0",
				"--output", "report.html",
				"--skip-empty"
		);
		assertEquals(expected, arguments);

	}

	@Test
	void replaceEnvironmentVariables() {

		// prepare
		List<String> arguments = List.of("--runtime", "${JAVA_HOME}/jre/lib", "'${A}' '${B}' '${C}' '${D}'");
		Map<String, String> environment = Map.of(
				"JAVA_HOME", "/opt/java",
				"A", "a",
				"B", "b",
				"C", ""
		);

		// test
		arguments = parser.replaceEnvironmentVariables(arguments, environment::get);

		// assert
		assertEquals(List.of("--runtime", "/opt/java/jre/lib", "'a' 'b' '' ''"), arguments);

	}

}
