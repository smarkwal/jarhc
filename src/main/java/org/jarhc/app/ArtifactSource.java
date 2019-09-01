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

package org.jarhc.app;

import java.io.IOException;
import java.io.InputStream;
import org.jarhc.artifacts.Artifact;
import org.jarhc.artifacts.Repository;
import org.jarhc.artifacts.RepositoryException;

public class ArtifactSource implements JarSource {

	private final String coordinates;
	private final Repository repository;

	public ArtifactSource(String coordinates, Repository repository) {
		this.coordinates = coordinates;
		this.repository = repository;
	}

	public String getCoordinates() {
		return coordinates;
	}

	@Override
	public String getName() {
		Artifact artifact = new Artifact(coordinates);
		return artifact.getFileName();
	}

	@Override
	public InputStream getData() throws IOException {
		Artifact artifact = new Artifact(coordinates);
		try {
			return repository.downloadArtifact(artifact).orElseThrow(() -> new IOException("Artifact not found: " + coordinates));
		} catch (RepositoryException e) {
			throw new IOException(e);
		}
	}

}
