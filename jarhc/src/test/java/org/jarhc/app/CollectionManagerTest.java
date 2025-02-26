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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CollectionManagerTest {

	private final CollectionManager manager = new CollectionManager();

	@Test
	void isCollection() {

		boolean result = manager.isCollection("servlet-3.1");
		assertTrue(result);

		result = manager.isCollection("test-0.9");
		assertTrue(result);
	}

	@Test
	void isCollection_notFound() {

		// test
		boolean result = manager.isCollection("unknown-1.0");

		// assert
		assertFalse(result);
	}

	@ParameterizedTest
	@CsvSource({
			"jakarta-ee-8",
			"jakarta-ee-9",
			"jakarta-ee-10",
			"servlet-3.0",
			"servlet-3.1",
			"servlet-4.0",
			"servlet-5.0",
			"servlet-6.0",
			"servlet-6.1",
			"test-0.9"
	})
	void getCollection(String name) {

		// test
		List<String> result1 = manager.getCollection(name);

		// assert
		assertNotNull(result1);
		assertFalse(result1.isEmpty());

		// test: cache
		List<String> result2 = manager.getCollection(name);

		// assert: same instance
		assertSame(result2, result1);
	}

	@Test
	void getCollection_notFound() {

		// test
		List<String> result = manager.getCollection("unknown-1.0");

		// assert
		assertNull(result);
	}

	@Test
	void getCollection_fromUserHomeFile(@TempDir Path tempDir) throws IOException {

		// override user.home property
		String userHome = System.getProperty("user.home");
		try {
			System.setProperty("user.home", tempDir.toString());

			// prepare
			Path path = tempDir.resolve(".jarhc").resolve("collections").resolve("test-0.9.txt");
			Files.createDirectories(path.getParent());
			Files.writeString(path, "org.test:test:0.9");

			// test
			List<String> result = manager.getCollection("test-0.9");

			// assert
			assertEquals(List.of("org.test:test:0.9"), result);

		} finally {
			// restore user.home property
			System.setProperty("user.home", userHome);
		}
	}

}
