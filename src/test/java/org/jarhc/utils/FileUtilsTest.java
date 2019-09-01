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

package org.jarhc.utils;

import static org.jarhc.utils.FileUtils.formatFileSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FileUtilsTest {

	private static final long ONE_DAY = 24 * 60 * 60 * 1000L;
	private static final long ONE_MINUTE = 60 * 1000L;

	@Test
	void test_formatFileSize() {

		assertEquals("0 B", formatFileSize(0));
		assertEquals("1 B", formatFileSize(1));
		assertEquals("21 B", formatFileSize(21));
		assertEquals("321 B", formatFileSize(321));
		assertEquals("1023 B", formatFileSize(1023));
		assertEquals("1.00 KB", formatFileSize(1024));
		assertEquals("1.21 KB", formatFileSize(1234));
		assertEquals("12.1 KB", formatFileSize(12345));
		assertEquals("121 KB", formatFileSize(123456));
		assertEquals("1.18 MB", formatFileSize(1234567));
		assertEquals("11.8 MB", formatFileSize(12345678));
		assertEquals("118 MB", formatFileSize(123456789));
		assertEquals("1177 MB", formatFileSize(1234567890));

	}

	@Test
	void compareByName() {

		// prepare
		File file1 = new File("abc.jar");
		File file2 = new File("xyz.jar");

		// test
		int result = FileUtils.compareByName(file1, file2);

		// assert
		assertTrue(result < 0);

		// prepare
		file1 = new File("xyz.jar");
		file2 = new File("abc.jar");

		// test
		result = FileUtils.compareByName(file1, file2);

		// assert
		assertTrue(result > 0);

	}

	@Test
	void compareByName_returnsZero_forExactSameName() {

		// prepare
		File file1 = new File("xyz.jar");
		File file2 = new File("xyz.jar");

		// test
		int result = FileUtils.compareByName(file1, file2);

		// assert
		assertEquals(0, result);

	}

	@Test
	void compareByName_returnsNegative_forUppercaseNameFirst() {

		// prepare
		File file1 = new File("XYZ.jar");
		File file2 = new File("xyz.jar");

		// test
		int result = FileUtils.compareByName(file1, file2);

		// assert
		assertTrue(result < 0);

	}

	@Test
	void compareByName_returnsPositive_forLowercaseNameFirst() {

		// prepare
		File file1 = new File("xyz.jar");
		File file2 = new File("XYZ.jar");

		// test
		int result = FileUtils.compareByName(file1, file2);

		// assert
		assertTrue(result > 0);

	}

	@Test
	void touchFile_createsFile_ifItDoesNotExist(@TempDir Path tempDir) throws IOException {

		// prepare
		File file = new File(tempDir.toFile(), "test.txt");

		// test
		FileUtils.touchFile(file);

		// assert
		assertTrue(file.isFile());

	}

	@Test
	void touchFile_createsFile_ifDirectoryDoesNotExist(@TempDir Path tempDir) throws IOException {

		// prepare
		File directory = new File(tempDir.toFile(), "test");
		File file = new File(directory, "test.txt");

		// test
		FileUtils.touchFile(file);

		// assert
		assertTrue(directory.isDirectory());
		assertTrue(file.isFile());

	}

	@Test
	void touchFile_changesLastModified(@TempDir Path tempDir) throws IOException {

		// prepare
		File file = new File(tempDir.toFile(), "test.txt");
		FileUtils.touchFile(file);

		// set modification time to yesterday
		long today = System.currentTimeMillis();
		long yesterday = today - ONE_DAY;
		boolean modified = file.setLastModified(yesterday);

		// assume
		assumeTrue(modified);
		assertTrue(file.lastModified() <= yesterday + ONE_MINUTE);

		// test
		FileUtils.touchFile(file);

		// assert
		assertTrue(file.isFile());
		assertTrue(file.lastModified() >= today - ONE_MINUTE);

	}

	@Test
	void sha1Hex(@TempDir Path tempDir) throws IOException {

		// prepare
		File file = new File(tempDir.toFile(), "test.txt");
		FileUtils.writeStringToFile("Hello World!", file);

		// test
		String result = FileUtils.sha1Hex(file);

		// assert
		assertEquals("2ef7bde608ce5404e97d5f042f95f89f1c232871", result);

	}

	@Test
	void getFilename() {

		assertEquals("report.html", FileUtils.getFilename("report.html"));
		assertEquals("slf4j-1.2.27.jar", FileUtils.getFilename("WEB-INF/lib/slf4j-1.2.27.jar"));

	}

}
