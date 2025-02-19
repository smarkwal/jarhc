/*
 * Copyright 2025 Stephan Markwalder
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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import org.jarhc.utils.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FileSourceTest {

	@Test
	void getFile() {

		// prepare
		File file = new File("test.jar");
		FileSource source = new FileSource(file);

		// test
		File result = source.getFile();

		// assert
		assertSame(file, result);
	}

	@Test
	void getName() {

		// prepare
		File file = new File("test.jar");
		FileSource source = new FileSource(file);

		// test
		String result = source.getName();

		// assert
		assertEquals("test.jar", result);
	}

	@Test
	void getInputStream(@TempDir File tempDir) throws Exception {

		// prepare
		File file = new File(tempDir, "test.jar");
		FileUtils.writeStringToFile("Test JAR", file);
		FileSource source = new FileSource(file);

		// test
		try (InputStream result = source.getInputStream()) {

			// assert
			assertNotNull(result);
			assertTrue(result instanceof FileInputStream);
			assertEquals(8, result.available());
		}
	}

	@Test
	void testEquals() {

		// prepare
		File file1 = new File("test-a.jar");
		File file2 = new File("test-a.jar");
		File file3 = new File("test-b.jar");
		FileSource source1 = new FileSource(file1);
		FileSource source2 = new FileSource(file2);
		FileSource source3 = new FileSource(file3);

		// test & assert
		assertEquals(source1, source1);
		assertEquals(source1, source2);
		assertNotEquals(source1, source3);
		assertEquals(source2, source1);
		assertEquals(source2, source2);
		assertNotEquals(source2, source3);
		assertNotEquals(source3, source1);
		assertNotEquals(source3, source2);
		assertEquals(source3, source3);
	}

	@Test
	void testHashCode() {

		// prepare
		File file1 = new File("test-a.jar");
		File file2 = new File("test-a.jar");
		File file3 = new File("test-b.jar");
		FileSource source1 = new FileSource(file1);
		FileSource source2 = new FileSource(file2);
		FileSource source3 = new FileSource(file3);

		// test & assert
		assertEquals(source1.hashCode(), source2.hashCode());
		assertNotEquals(source1.hashCode(), source3.hashCode());
		assertNotEquals(source2.hashCode(), source3.hashCode());
	}

}