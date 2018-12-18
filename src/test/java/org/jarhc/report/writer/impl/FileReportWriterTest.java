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

package org.jarhc.report.writer.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.TempDirectory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(TempDirectory.class)
class FileReportWriterTest {

	@Test
	void test_print(@TempDirectory.TempDir Path tempDir) throws IOException {

		// prepare
		File file = new File(tempDir.toFile(), "file.txt");
		FileReportWriter reportWriter = new FileReportWriter(file);

		// assert: file as not been created yet
		assertFalse(file.exists());

		// test
		reportWriter.print("Hello");

		// assert: file has been created
		assertTrue(file.isFile());

		reportWriter.close();

		// assert: content
		String result = new String(Files.readAllBytes(file.toPath()));
		assertEquals("Hello", result);

	}

	@Test
	void test_print_file_error(@TempDirectory.TempDir Path tempDir) {

		// prepare
		File file = tempDir.toFile(); // file is an existing directory
		FileReportWriter reportWriter = new FileReportWriter(file);

		Exception exception = null;
		try {
			// test
			reportWriter.print("Hello");
		} catch (Exception e) {
			exception = e;
		}

		// assert
		assertNotNull(exception);
		assertSame(exception.getClass(), RuntimeException.class);
		Throwable cause = exception.getCause();
		assertNotNull(cause);
		assertSame(cause.getClass(), FileNotFoundException.class);

	}

	@Test
	void test_close_no_output(@TempDirectory.TempDir Path tempDir) throws IOException {

		// prepare
		File file = new File(tempDir.toFile(), "file.txt");
		FileReportWriter reportWriter = new FileReportWriter(file);

		// test
		reportWriter.close();

		// assert: no output -> file has not been created
		assertFalse(file.exists());

	}

}
