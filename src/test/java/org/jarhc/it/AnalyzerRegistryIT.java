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

package org.jarhc.it;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.jarhc.analyzer.Analyzer;
import org.jarhc.analyzer.AnalyzerRegistry;
import org.jarhc.app.Options;
import org.jarhc.artifacts.Repository;
import org.jarhc.env.JavaRuntime;
import org.jarhc.inject.Injector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.mockito.Mockito;

@SuppressWarnings("NewClassNamingConvention")
class AnalyzerRegistryIT {

	private AnalyzerRegistry registry;

	@BeforeEach
	void setUp() {

		// prepare mock context
		Options options = Mockito.mock(Options.class);
		JavaRuntime javaRuntime = Mockito.mock(JavaRuntime.class);
		Repository repository = Mockito.mock(Repository.class);

		// prepare an injector
		Injector injector = new Injector();
		injector.addBinding(Options.class, options);
		injector.addBinding(JavaRuntime.class, javaRuntime);
		injector.addBinding(Repository.class, repository);

		registry = new AnalyzerRegistry(injector);
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

	}

}
