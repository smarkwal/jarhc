/*
 * Copyright 2026 Stephan Markwalder
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

package org.jarhc.artifacts;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MemoryCacheArtifactFinder implements ArtifactFinder {

	private final ArtifactFinder delegate;
	private final Logger logger;
	private final Map<String, List<Artifact>> cache = new ConcurrentHashMap<>();

	public MemoryCacheArtifactFinder(ArtifactFinder delegate) {
		this(delegate, LoggerFactory.getLogger(MemoryCacheArtifactFinder.class));
	}

	public MemoryCacheArtifactFinder(ArtifactFinder delegate, Logger logger) {
		this.delegate = delegate;
		this.logger = logger;
	}

	@Override
	public List<Artifact> findArtifacts(String checksum) throws RepositoryException {

		if (cache.containsKey(checksum)) {
			List<Artifact> artifacts = new ArrayList<>(cache.get(checksum));
			logger.debug("Artifact found: {} -> {} (memory cache)", checksum, artifacts);
			return artifacts;
		}

		List<Artifact> artifacts = delegate.findArtifacts(checksum);
		cache.put(checksum, new ArrayList<>(artifacts));
		return artifacts;
	}

}
