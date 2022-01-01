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

import static org.jarhc.test.AssertUtils.assertMock;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.jarhc.inject.Injector;
import org.jarhc.inject.InjectorException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.mockito.Mockito;

class AnalyzerRegistryTest {

	private AnalyzerRegistry registry;

	@BeforeEach
	@SuppressWarnings("unchecked")
	void setUp() throws InjectorException {

		// prepare an injector which will return mock analyzers
		Injector injector = Mockito.mock(Injector.class);
		doAnswer(args -> {
			Class<?> analyzerClass = args.getArgument(0, Class.class);
			return Mockito.mock(analyzerClass);
		}).when(injector).createInstance(any(Class.class));

		registry = new AnalyzerRegistry(injector);
	}

	@Test
	void test_getCodes() {

		// test
		List<String> codes = registry.getCodes();

		// assert
		assertEquals(10, codes.size());
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
		Analyzer analyzer = registry.createAnalyzer(code);

		// assert
		assertNotNull(analyzer);
		assertMock(analyzer);

	}

	@Test
	void test_createAnalyzer_throwsIllegalArgumentException_forUnknownCode() {

		// test and assert
		assertThrows(IllegalArgumentException.class, () -> registry.createAnalyzer("ukn"));

	}

}
