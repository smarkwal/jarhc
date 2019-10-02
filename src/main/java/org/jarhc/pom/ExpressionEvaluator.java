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

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ExpressionEvaluator {

	private static final Logger LOGGER = LoggerFactory.getLogger(ExpressionEvaluator.class);
	private static final Pattern pattern = Pattern.compile("\\$\\{([^}]+)}");

	private final POM pom;

	ExpressionEvaluator(POM pom) {
		this.pom = pom;
	}

	String evaluateText(String text) {

		if (text == null || text.isEmpty()) return text;
		if (!text.contains("${")) return text;

		StringBuffer buffer = new StringBuffer();

		Matcher matcher = pattern.matcher(text);
		while (matcher.find()) {
			String expression = matcher.group(1);

			Optional<String> value = evaluateExpression(expression);
			if (value.isPresent()) {
				matcher.appendReplacement(buffer, value.get());
			} else {
				LOGGER.warn("Unknown expression: ${{}}", expression);
				matcher.appendReplacement(buffer, "\\${" + expression + "}");
			}
		}
		matcher.appendTail(buffer);

		return buffer.toString();
	}

	Optional<String> evaluateExpression(String expression) {
		if (pom.hasProperty(expression)) {
			String property = pom.getProperty(expression);
			property = evaluateText(property); // recursive
			return Optional.of(property);
		} else if (expression.equals("project.version")) {
			return Optional.of(pom.getVersion());
		} else {
			return Optional.empty();
		}
	}

}
