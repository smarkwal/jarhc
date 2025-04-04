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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Properties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CollectionManagerImplTest {

	private final Properties properties = new Properties();
	private final CollectionManagerImpl collectionManager = new CollectionManagerImpl(properties);

	@BeforeEach
	void setUp() {
		properties.setProperty("collection.demo-0.1", "org.demo:demo:0.1");
	}

	@Test
	void isCollection() {

		boolean result = collectionManager.isCollection("servlet-3.1");
		assertTrue(result);

		result = collectionManager.isCollection("test-0.9");
		assertTrue(result);

		result = collectionManager.isCollection("demo-0.1");
		assertTrue(result);
	}

	@Test
	void isCollection_notFound() {

		// test
		boolean result = collectionManager.isCollection("unknown-1.0");

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
			"test-0.9",
			"demo-0.1"
	})
	void getCollection(String name) {

		// test
		List<String> result1 = collectionManager.getCollection(name);

		// assert
		assertNotNull(result1);
		assertFalse(result1.isEmpty());

		// test: cache
		List<String> result2 = collectionManager.getCollection(name);

		// assert: same instance
		assertSame(result2, result1);
	}

	@Test
	void getCollection_notFound() {

		// test
		List<String> result = collectionManager.getCollection("unknown-1.0");

		// assert
		assertNull(result);
	}

}
