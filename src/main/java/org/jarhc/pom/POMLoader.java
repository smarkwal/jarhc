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

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.jarhc.artifacts.Artifact;
import org.jarhc.artifacts.Repository;
import org.jarhc.artifacts.RepositoryException;
import org.jarhc.pom.resolver.POMNotFoundException;

public class POMLoader {

	private static final POM POM_NOT_FOUND = new POM("unknown", "unknown", "0");

	private final Repository repository;
	private final Map<Artifact, POM> cache = new ConcurrentHashMap<>();

	public POMLoader(Repository repository) {
		this.repository = repository;
	}

	public POM load(Artifact artifact) throws POMException, POMNotFoundException {

		artifact = artifact.withType("pom");

		// check cache
		if (cache.containsKey(artifact)) {
			POM pom = cache.get(artifact);

			// check for negative cache result
			if (pom == POM_NOT_FOUND) {
				throw new POMNotFoundException(artifact.toString());
			}

			return pom;
		}

		POM pom = loadPOM(artifact);

		// if POM has a parent project ...
		if (pom.hasParent()) {
			loadParents(pom);
		}

		// evaluate expressions in POM (including parent POMs)
		POMEvaluator evaluator = new POMEvaluator();
		evaluator.evaluatePOM(pom);

		return pom;
	}


	private POM loadPOM(Artifact artifact) throws POMException {

		// try to download POM file
		Optional<InputStream> result;
		try {
			result = repository.downloadArtifact(artifact);
		} catch (RepositoryException e) {
			String message = String.format("Repository error for POM file: %s", artifact);
			throw new POMException(message, e);
		}

		// if POM file has not been found ...
		if (!result.isPresent()) {

			// cache negative result
			cache.put(artifact, POM_NOT_FOUND);

			throw new POMNotFoundException(artifact.toString());
		}

		// parse POM file
		POM pom;
		try (InputStream inputStream = result.get()) {
			POMReader reader = new POMReader();
			pom = reader.read(inputStream);
		} catch (IOException | POMException e) {
			String message = String.format("Parser error for POM file: %s", artifact);
			throw new POMException(message, e);
		}

		// update cache
		cache.put(artifact, pom);

		return pom;
	}

	private void loadParents(POM pom) throws POMException {

		// get parent project coordinates
		POM parent = pom.getParent();
		String groupId = parent.getGroupId();
		String artifactId = parent.getArtifactId();
		String versionId = parent.getVersion();
		Artifact artifact = new Artifact(groupId, artifactId, versionId, "pom");

		// try to load parent POM
		parent = loadPOM(artifact); // TODO: graceful exception handling
		pom.setParent(parent);

		// recursion: load parent of parent
		if (parent.hasParent()) {
			loadParents(parent);
		}

	}

}
