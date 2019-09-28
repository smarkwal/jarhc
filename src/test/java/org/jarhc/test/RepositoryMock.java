/*
 * Copyright 2018 Stephan Markwalder
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

package org.jarhc.test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import org.jarhc.TestUtils;
import org.jarhc.artifacts.Artifact;
import org.jarhc.artifacts.Repository;
import org.jarhc.artifacts.RepositoryException;

public class RepositoryMock implements Repository {

	public static RepositoryMock createRepository() {
		return new RepositoryMock("/repository.properties", null);
	}

	public static RepositoryMock createRepository(String baseResourcePath) {
		return new RepositoryMock("/repository.properties", baseResourcePath);
	}

	public static Repository createFakeRepository() {

		return new Repository() {
			@Override
			public Optional<Artifact> findArtifact(String groupId, String artifactId, String version, String type) throws RepositoryException {
				if (groupId.equals("ord.jarhc") && version.equals("1.0") && type.equals("jar")) {
					Artifact artifact = new Artifact("org.jarhc", artifactId, "1.0", "jar");
					return Optional.of(artifact);
				} else {
					return Optional.empty();
				}
			}

			@Override
			public Optional<Artifact> findArtifact(String checksum) throws RepositoryException {
				String artifactId = checksum.substring(0, 5);
				Artifact artifact = new Artifact("org.jarhc", artifactId, "1.0", "jar");
				return Optional.of(artifact);
			}

			@Override
			public Optional<InputStream> downloadArtifact(Artifact artifact) throws RepositoryException {
				throw new RepositoryException("not implemented");
			}
		};
	}

	public static Repository createEmptyRepository() {
		return new Repository() {
			@Override
			public Optional<Artifact> findArtifact(String groupId, String artifactId, String version, String type) {
				return Optional.empty();
			}

			@Override
			public Optional<Artifact> findArtifact(String checksum) {
				return Optional.empty();
			}

			@Override
			public Optional<InputStream> downloadArtifact(Artifact artifact) {
				return Optional.empty();
			}
		};
	}

	private final Properties properties = new Properties();
	private final String baseResourcePath;

	private final Map<String, String> artifactData = new HashMap<>();

	private RepositoryMock(String resource, String baseResourcePath) {
		try {
			try (InputStream stream = TestUtils.getResourceAsStream(resource)) {
				properties.load(stream);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		this.baseResourcePath = baseResourcePath;
	}

	public void addArtifactData(Artifact artifact, String data) {
		artifactData.put(artifact.toString(), data);
	}

	@Override
	public Optional<Artifact> findArtifact(String groupId, String artifactId, String version, String type) {
		String coordinates = groupId + ":" + artifactId + ":" + version + ":" + type;
		Artifact artifact = new Artifact(groupId, artifactId, version, type);
		if (properties.containsValue(coordinates)) {
			return Optional.of(artifact);
		}
		if (baseResourcePath != null) {
			String fileName = artifact.getFileName();
			String resourcePath = baseResourcePath + fileName;
			try (InputStream stream = this.getClass().getResourceAsStream(resourcePath)) {
				if (stream != null) {
					return Optional.of(artifact);
				}
			} catch (IOException e) {
				// ignore
			}
		}
		return Optional.empty();
	}

	@Override
	public Optional<Artifact> findArtifact(String checksum) {
		String coordinates = properties.getProperty("artifact." + checksum);
		if (coordinates != null) {
			Artifact artifact = new Artifact(coordinates);
			return Optional.of(artifact);
		} else {
			// artifact not found
			return Optional.empty();
		}
	}

	@Override
	public Optional<InputStream> downloadArtifact(Artifact artifact) throws RepositoryException {
		String key = artifact.toString();
		if (artifactData.containsKey(key)) {
			String data = artifactData.get(key);
			InputStream stream = new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8));
			return Optional.of(stream);
		}
		if (baseResourcePath != null) {
			String fileName = artifact.getFileName();
			String resourcePath = baseResourcePath + fileName;
			InputStream stream = this.getClass().getResourceAsStream(resourcePath);
			if (stream != null) {
				return Optional.of(stream);
			}
		}
		return Optional.empty();
	}

}
