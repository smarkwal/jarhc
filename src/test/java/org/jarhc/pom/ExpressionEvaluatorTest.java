/*
 * Copyright 2019 Stephan Markwalder
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

package org.jarhc.pom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.api.Test;

class ExpressionEvaluatorTest {

	@Test
	void evaluateText() {

		// prepare
		POM pom = new POM("org.jarhc", "jarhc", "1.0");
		ExpressionEvaluator evaluator = new ExpressionEvaluator(pom);
		pom.setProperty("lib.name", "jarhc");

		// test
		String result = evaluator.evaluateText("${lib.name}-${project.version}.jar");

		// assert
		assertEquals("jarhc-1.0.jar", result);

	}

	@Test
	void evaluateText_withUnknownProperty() {

		// prepare
		POM pom = new POM("org.jarhc", "jarhc", "1.0");
		ExpressionEvaluator evaluator = new ExpressionEvaluator(pom);

		// test
		String result = evaluator.evaluateText("${lib.name}-${lib.version}.jar");

		// assert
		assertEquals("${lib.name}-${lib.version}.jar", result);

	}

	@Test
	void evaluateExpression() {

		// prepare
		POM pom = new POM("org.jarhc", "jarhc", "1.0");
		ExpressionEvaluator evaluator = new ExpressionEvaluator(pom);
		pom.setProperty("skip.benchmarks", "true");

		// test
		Optional<String> result = evaluator.evaluateExpression("skip.benchmarks");

		// assert
		assertTrue(result.isPresent());
		assertEquals("true", result.get());

	}

	@Test
	void evaluateExpression_withUnknownProperty() {

		// prepare
		POM pom = new POM("org.jarhc", "jarhc", "1.0");
		ExpressionEvaluator evaluator = new ExpressionEvaluator(pom);

		// test
		Optional<String> result = evaluator.evaluateExpression("unknown.property");

		// assert
		assertFalse(result.isPresent());

	}

	@Test
	void evaluateExpression_withProjectVersion() {

		// prepare
		POM pom = new POM("org.jarhc", "jarhc", "1.0");
		ExpressionEvaluator evaluator = new ExpressionEvaluator(pom);

		// test
		Optional<String> result = evaluator.evaluateExpression("project.version");

		// assert
		assertTrue(result.isPresent());
		assertEquals("1.0", result.get());

	}

}