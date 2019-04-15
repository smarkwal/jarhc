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

package org.jarhc.analyzer;

import org.jarhc.Context;
import org.jarhc.test.ContextMock;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AnalyzerRegistryTest {

	private final AnalyzerRegistry registry = new AnalyzerRegistry();
	private final Context context = ContextMock.createContext();

	@Test
	void test_getCodes() {

		// test
		List<String> codes = registry.getCodes();

		// assert
		assertEquals(9, codes.size());
		assertTrue(codes.contains("jf"));

	}

	@Test
	void test_getDescription() {

		// test
		AnalyzerDescription description = registry.getDescription("jf");

		// assert
		assertNotNull(description);
		assertEquals("jf", description.getCode());

	}

	@TestFactory
	Collection<DynamicTest> test_createAnalyzer() {

		List<DynamicTest> tests = new ArrayList<>();

		List<String> codes = registry.getCodes();
		for (String code : codes) {
			tests.add(DynamicTest.dynamicTest("Analyzer: " + code, () -> test_createAnalyzer(code)));
		}

		return tests;
	}

	private void test_createAnalyzer(String code) {

		// test
		Analyzer analyzer = registry.createAnalyzer(code, context);

		// assert
		assertNotNull(analyzer);

	}

}
