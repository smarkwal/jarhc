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

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModelEvaluator {

	private static final Logger LOGGER = LoggerFactory.getLogger(ModelEvaluator.class);

	public void evaluateModel(Model model) {

		ExpressionEvaluator evaluator = new ExpressionEvaluator(model);

		// evaluate project version
		evaluate(model::getVersion, model::setVersion, evaluator);

		if (containsExpression(model.getVersion())) {
			// TODO: look for properties in parent project.
			LOGGER.warn("Project with version expression: {}:{}:{}", model.getGroupId(), model.getArtifactId(), model.getVersion());
		}

		// for every dependency ...
		List<Dependency> dependencies = model.getDependencies();
		for (Dependency dependency : dependencies) {

			// evaluate dependency versions
			evaluate(dependency::getVersion, dependency::setVersion, evaluator);

			if (dependency.getVersion().isEmpty()) {
				// TODO: look for dependency-management information in parent project.
				//  see https://maven.apache.org/guides/introduction/introduction-to-dependency-mechanism.html#Dependency_Management
				LOGGER.warn("Dependency without version: {}:{}:{} -> {}", model.getGroupId(), model.getArtifactId(), model.getVersion(), dependency);
				dependency.setVersion("?");
			} else if (containsExpression(dependency.getVersion())) {
				// TODO: look for properties in parent project.
				LOGGER.warn("Dependency with version expression: {}:{}:{} -> {}", model.getGroupId(), model.getArtifactId(), model.getVersion(), dependency);
			}

		}

	}

	private void evaluate(Supplier<String> getter, Consumer<String> setter, ExpressionEvaluator evaluator) {
		String value = getter.get();
		if (containsExpression(value)) {
			String result = evaluator.evaluateText(value);
			if (!result.equals(value)) {
				setter.accept(result);
			}
		}
	}

	private boolean containsExpression(String text) {
		return text.contains("${");
	}

}
