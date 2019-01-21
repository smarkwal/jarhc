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

package org.jarhc.artifacts;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Optional;

/**
 * Read-only access to a local Maven repository.
 */
public class MavenLocalRepository implements Repository {

	private final File directory;
	private final Repository repository;

	public MavenLocalRepository(File directory, Repository repository) {
		this.directory = directory;
		this.repository = repository;
	}

	@Override
	public Optional<Artifact> findArtifact(String groupId, String artifactId, String version, String type) throws RepositoryException {
		Artifact artifact = new Artifact(groupId, artifactId, version, type);

		// check if file exists in local repository
		String path = artifact.getPath();
		File file = new File(directory, path);
		if (file.isFile()) {
			return Optional.of(artifact);
		}

		// try to find artifact in parent repository
		if (repository != null) {
			return repository.findArtifact(groupId, artifactId, version, type);
		}

		// artifact not found
		return Optional.empty();
	}

	@Override
	public Optional<Artifact> findArtifact(String checksum) throws RepositoryException {
		// search by checksum is not supported in local repository

		// try to find artifact in parent repository
		if (repository != null) {
			return repository.findArtifact(checksum);
		}

		// artifact not found
		return Optional.empty();
	}

	@Override
	public Optional<InputStream> downloadArtifact(Artifact artifact) throws RepositoryException {

		// check if file exists in local repository
		String path = artifact.getPath();
		File file = new File(directory, path);
		if (file.isFile()) {

			// read directly from file in local repository
			try {
				InputStream stream = new FileInputStream(file);
				return Optional.of(stream);
			} catch (FileNotFoundException e) {
				throw new RepositoryException("I/O error", e);
			}
		}

		// try to download artifact from parent repository
		if (repository != null) {
			return repository.downloadArtifact(artifact);
		}

		// artifact not found
		return Optional.empty();
	}

}
