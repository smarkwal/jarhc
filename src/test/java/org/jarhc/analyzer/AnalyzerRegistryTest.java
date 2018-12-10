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

import org.jarhc.model.Classpath;
import org.jarhc.report.ReportSection;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AnalyzerRegistryTest {

	@Test
	void test_constructor() {

		// test
		AnalyzerRegistry registry = new AnalyzerRegistry(false);

		// assert
		assertTrue(registry.getAnalyzerNames().isEmpty());
		assertTrue(registry.getAnalyzers().isEmpty());

		// test
		registry = new AnalyzerRegistry(true);

		// assert
		assertFalse(registry.getAnalyzerNames().isEmpty());
		assertFalse(registry.getAnalyzers().isEmpty());

	}

	@Test
	void test_getAnalyzerNames() {

		// prepare
		AnalyzerRegistry registry = new AnalyzerRegistry(false);
		registry.register(new TestAnalyzer());

		// test
		List<String> analyzerNames = registry.getAnalyzerNames();

		// assert
		assertEquals(1, analyzerNames.size());
		assertTrue(analyzerNames.contains("TestAnalyzer"));

	}

	@Test
	void test_getAnalyzers() {

		// prepare
		AnalyzerRegistry registry = new AnalyzerRegistry(false);
		registry.register(new TestAnalyzer());

		// test
		List<Analyzer> analyzers = registry.getAnalyzers();

		// assert
		assertEquals(1, analyzers.size());
		assertTrue(analyzers.get(0) instanceof TestAnalyzer);

	}

	@Test
	void test_getAnalyzerName() {

		// prepare
		TestAnalyzer analyzer = new TestAnalyzer();

		// test
		String analyzerName = AnalyzerRegistry.getAnalyzerName(analyzer);

		// assert
		assertEquals("TestAnalyzer", analyzerName);

	}

	@Test
	void test_getAnalyzer() {

		// prepare
		AnalyzerRegistry registry = new AnalyzerRegistry(false);
		registry.register(new TestAnalyzer());

		// test
		Analyzer analyzer = registry.getAnalyzer("TestAnalyzer");

		// assert
		assertNotNull(analyzer);
		assertTrue(analyzer instanceof TestAnalyzer);

		analyzer = registry.getAnalyzer("UnknownAnalyzer");

		// assert
		assertNull(analyzer);

	}

	private static class TestAnalyzer extends Analyzer {
		@Override
		public ReportSection analyze(Classpath classpath) {
			return null;
		}
	}

}
