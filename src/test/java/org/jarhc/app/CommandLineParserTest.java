package org.jarhc.app;

import org.jarhc.TestUtils;
import org.jarhc.test.PrintStreamBuffer;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CommandLineParserTest {

	@Test
	void test_no_arguments() {

		// prepare
		PrintStreamBuffer err = new PrintStreamBuffer();
		CommandLineParser parser = new CommandLineParser(err);

		// test
		Options options = parser.parse(new String[0]);

		// assert
		assertNotNull(options);
		assertEquals(-1, options.getErrorCode());
		assertTrue(err.getText().startsWith("Argument <path> is missing."));

	}

	@Test
	void test_file_not_jar() throws IOException {

		// prepare
		PrintStreamBuffer err = new PrintStreamBuffer();
		CommandLineParser parser = new CommandLineParser(err);
		File file = TestUtils.getResourceAsFile("/test/Main.java", "CommandLineParserTest-");

		// test
		Options options = parser.parse(new String[]{file.getAbsolutePath()});

		// assert
		assertNotNull(options);
		assertEquals(-2, options.getErrorCode());
		assertTrue(err.getText().startsWith("File is not a *.jar file: " + file.getAbsolutePath()));

	}

	@Test
	void test_file_not_found() {

		// prepare
		PrintStreamBuffer err = new PrintStreamBuffer();
		CommandLineParser parser = new CommandLineParser(err);
		File file = new File("file.jar");

		// test
		Options options = parser.parse(new String[]{file.getAbsolutePath()});

		// assert
		assertNotNull(options);
		assertEquals(-3, options.getErrorCode());
		assertTrue(err.getText().startsWith("File or directory not found: " + file.getAbsolutePath()));

	}

	@Test
	void test_no_files_found() throws IOException {

		// prepare
		PrintStreamBuffer err = new PrintStreamBuffer();
		CommandLineParser parser = new CommandLineParser(err);
		File file = TestUtils.getResourceAsFile("/test/Main.java", "CommandLineParserTest-");
		File directory = file.getParentFile();

		// test
		Options options = parser.parse(new String[]{directory.getAbsolutePath()});

		// assert
		assertNotNull(options);
		assertEquals(-4, options.getErrorCode());
		assertTrue(err.getText().startsWith("No *.jar files found in path."));

	}

	@Test
	void test_one_jar_file() throws IOException {

		// prepare
		PrintStreamBuffer err = new PrintStreamBuffer();
		CommandLineParser parser = new CommandLineParser(err);
		File file = TestUtils.getResourceAsFile("/test2/a.jar", "CommandLineParserTest-");

		// test
		Options options = parser.parse(new String[]{file.getAbsolutePath()});

		// assert
		assertNotNull(options);
		assertEquals(0, options.getErrorCode());
		assertEquals("", err.getText());

		List<File> files = options.getFiles();
		assertEquals(1, files.size());
		assertEquals(file, files.get(0));

	}

	@Test
	void test_one_jar_file_in_directory() throws IOException {

		// prepare
		PrintStreamBuffer err = new PrintStreamBuffer();
		CommandLineParser parser = new CommandLineParser(err);
		File file = TestUtils.getResourceAsFile("/test2/a.jar", "CommandLineParserTest-");
		File directory = file.getParentFile();

		// test
		Options options = parser.parse(new String[]{directory.getAbsolutePath()});

		// assert
		assertNotNull(options);
		assertEquals(0, options.getErrorCode());
		assertEquals("", err.getText());

		List<File> files = options.getFiles();
		assertEquals(1, files.size());
		assertEquals(file, files.get(0));

	}

	@Test
	void test_two_jar_files() throws IOException {

		// prepare
		PrintStreamBuffer err = new PrintStreamBuffer();
		CommandLineParser parser = new CommandLineParser(err);
		File file1 = TestUtils.getResourceAsFile("/test2/a.jar", "CommandLineParserTest-");
		File file2 = TestUtils.getResourceAsFile("/test2/a.jar", "CommandLineParserTest-");

		// test
		Options options = parser.parse(new String[]{file1.getAbsolutePath(), file2.getAbsolutePath()});

		// assert
		assertNotNull(options);
		assertEquals(0, options.getErrorCode());
		assertEquals("", err.getText());

		List<File> files = options.getFiles();
		assertEquals(2, files.size());
		assertEquals(file1, files.get(0));
		assertEquals(file2, files.get(1));

	}

}
