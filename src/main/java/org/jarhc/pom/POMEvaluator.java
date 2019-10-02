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
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class POMEvaluator {

	private static final Logger LOGGER = LoggerFactory.getLogger(POMEvaluator.class);

	public void evaluatePOM(POM pom) {

		// TODO: check if POM has already been evaluated

		// evaluate parent first (if present)
		if (pom.hasParent()) {
			POM parent = pom.getParent();
			evaluatePOM(parent);
		}

		ExpressionEvaluator evaluator = new ExpressionEvaluator(pom);

		// evaluate project version
		evaluate(pom::getVersion, pom::setVersion, evaluator);

		if (containsExpression(pom.getVersion())) {
			LOGGER.warn("Project with version expression: {}:{}:{}", pom.getGroupId(), pom.getArtifactId(), pom.getVersion());
		}

		// for every dependency management ...
		for (Dependency dependency : pom.getDependencyManagement()) {

			// evaluate dependency version
			evaluate(dependency::getVersion, dependency::setVersion, evaluator);

			if (containsExpression(dependency.getVersion())) {
				LOGGER.warn("Dependency Management with version expression: {}:{}:{} -> {}", pom.getGroupId(), pom.getArtifactId(), pom.getVersion(), dependency);
			}

		}

		// for every dependency ...
		List<Dependency> dependencies = pom.getDependencies();
		for (Dependency dependency : dependencies) {

			// evaluate dependency version
			evaluate(dependency::getVersion, dependency::setVersion, evaluator);

			if (dependency.getVersion().isEmpty()) {

				// look for dependency-management information in parent projects
				Optional<Dependency> dependencyManagement = pom.findDependencyManagement(dependency.getGroupId(), dependency.getArtifactId());
				if (dependencyManagement.isPresent()) {
					// copy version from dependency management
					String version = dependencyManagement.get().getVersion();
					dependency.setVersion(version);
				} else {
					LOGGER.warn("Dependency without version: {}:{}:{} -> {}", pom.getGroupId(), pom.getArtifactId(), pom.getVersion(), dependency);
					dependency.setVersion("?");
				}

			}

			if (containsExpression(dependency.getVersion())) {
				LOGGER.warn("Dependency with version expression: {}:{}:{} -> {}", pom.getGroupId(), pom.getArtifactId(), pom.getVersion(), dependency);
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
