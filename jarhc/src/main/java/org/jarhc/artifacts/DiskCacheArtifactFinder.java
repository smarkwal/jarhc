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

import java.io.File;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DiskCacheArtifactFinder implements ArtifactFinder {

	private final File cacheDir;
	private final ArtifactFinder delegate;
	private final Logger logger;

	public DiskCacheArtifactFinder(File cacheDir, ArtifactFinder delegate) {
		this(cacheDir, delegate, LoggerFactory.getLogger(DiskCacheArtifactFinder.class));
	}

	public DiskCacheArtifactFinder(File cacheDir, ArtifactFinder delegate, Logger logger) {
		this.cacheDir = cacheDir;
		this.delegate = delegate;
		this.logger = logger;
	}

	@Override
	public List<Artifact> findArtifacts(String checksum) throws RepositoryException {

		ArtifactFinder.validateChecksum(checksum);

		File cacheFile = new File(cacheDir, checksum + ".txt");
		if (cacheFile.isFile()) {
			List<Artifact> artifacts = ArtifactCacheFiles.readArtifactsFromFile(cacheFile);
			logger.debug("Artifact found: {} -> {} (disk cache)", checksum, artifacts);
			return artifacts;
		}

		List<Artifact> artifacts = delegate.findArtifacts(checksum);
		if (!artifacts.isEmpty()) {
			ArtifactCacheFiles.writeArtifactsToFile(artifacts, cacheFile);
		}
		return artifacts;
	}

}
