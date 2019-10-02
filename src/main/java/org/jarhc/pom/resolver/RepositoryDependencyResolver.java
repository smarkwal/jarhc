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

package org.jarhc.pom.resolver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.jarhc.artifacts.Artifact;
import org.jarhc.pom.Dependency;
import org.jarhc.pom.POM;
import org.jarhc.pom.POMEvaluator;
import org.jarhc.pom.POMException;
import org.jarhc.pom.POMLoader;

public class RepositoryDependencyResolver implements DependencyResolver {

	private static final List<Dependency> POM_NOT_FOUND = Collections.emptyList();

	private final POMLoader pomLoader;
	private final Map<Artifact, List<Dependency>> cache = new ConcurrentHashMap<>();

	public RepositoryDependencyResolver(POMLoader pomLoader) {
		this.pomLoader = pomLoader;
	}

	@Override
	public List<Dependency> getDependencies(Artifact artifact) throws POMException {

		artifact = artifact.withType("pom");

		// check cache
		if (cache.containsKey(artifact)) {
			List<Dependency> dependencies = cache.get(artifact);

			// check for negative cache result
			if (dependencies == POM_NOT_FOUND) {
				String message = String.format("POM file not found: %s", artifact);
				throw new POMNotFoundException(message);
			}

			return new ArrayList<>(dependencies);
		}

		POM pom = pomLoader.load(artifact);

		// evaluate expressions in POM
		POMEvaluator evaluator = new POMEvaluator();
		evaluator.evaluatePOM(pom);

		// update cache
		List<Dependency> dependencies = pom.getDependencies();
		cache.put(artifact, dependencies);

		return new ArrayList<>(dependencies);

	}

}
