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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.jarhc.artifacts.Artifact;
import org.jarhc.artifacts.Repository;
import org.jarhc.artifacts.RepositoryException;
import org.jarhc.pom.Dependency;
import org.jarhc.pom.Model;
import org.jarhc.pom.ModelEvaluator;
import org.jarhc.pom.ModelException;
import org.jarhc.pom.ModelReader;

public class RepositoryDependencyResolver implements DependencyResolver {

	private final Repository repository;
	private final Map<Artifact, List<Dependency>> cache = new ConcurrentHashMap<>();
	private final List<Dependency> POM_NOT_FOUND = Collections.emptyList();

	public RepositoryDependencyResolver(Repository repository) {
		this.repository = repository;
	}

	@Override
	public List<Dependency> getDependencies(Artifact artifact) throws ResolverException {

		artifact = artifact.withType("pom");

		// check cache
		if (cache.containsKey(artifact)) {
			List<Dependency> dependencies = cache.get(artifact);

			// check for negative cache result
			if (dependencies == POM_NOT_FOUND) {
				String message = String.format("POM file not found: %s", artifact);
				throw new PomNotFoundException(message);
			}

			return new ArrayList<>(dependencies);
		}

		Model model = loadModel(artifact);

		// if model has a parent project ...
		if (model.hasParent()) {
			loadParents(model);
		}

		// evaluate expressions in model
		ModelEvaluator evaluator = new ModelEvaluator();
		evaluator.evaluateModel(model);

		// update cache
		List<Dependency> dependencies = model.getDependencies();
		cache.put(artifact, dependencies);

		return new ArrayList<>(dependencies);

	}

	private Model loadModel(Artifact artifact) throws ResolverException {

		// try to download POM file
		Optional<InputStream> result;
		try {
			result = repository.downloadArtifact(artifact);
		} catch (RepositoryException e) {
			String message = String.format("Repository error for POM file: %s", artifact);
			throw new ResolverException(message, e);
		}

		// if POM file has not been found ...
		if (!result.isPresent()) {

			// cache negative result
			cache.put(artifact, POM_NOT_FOUND);

			String message = String.format("POM file not found: %s", artifact);
			throw new PomNotFoundException(message);
		}

		// parse POM file
		Model model;
		try (InputStream inputStream = result.get()) {
			ModelReader reader = new ModelReader();
			model = reader.read(inputStream);
		} catch (IOException | ModelException e) {
			String message = String.format("Parser error for POM file: %s", artifact);
			throw new ResolverException(message, e);
		}
		return model;

	}

	private void loadParents(Model model) throws ResolverException {

		// get parent project coordinates
		Model parent = model.getParent();
		String groupId = parent.getGroupId();
		String artifactId = parent.getArtifactId();
		String versionId = parent.getVersion();
		Artifact artifact = new Artifact(groupId, artifactId, versionId, "pom");

		// try to load parent model
		parent = loadModel(artifact); // TODO: graceful exception handling
		model.setParent(parent);

		// recursion: load parent of parent
		if (parent.hasParent()) {
			loadParents(parent);
		}

	}

}
