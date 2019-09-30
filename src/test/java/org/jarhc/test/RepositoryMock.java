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
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import org.jarhc.TestUtils;
import org.jarhc.artifacts.Artifact;
import org.jarhc.artifacts.MavenCentralRepository;
import org.jarhc.artifacts.Repository;
import org.jarhc.utils.DigestUtils;

/**
 * A repository implementation loading all information and artifacts from
 * test resources.
 */
public class RepositoryMock implements Repository {

	public static RepositoryMock createRepository() {
		return new RepositoryMock();
	}

	private final Properties properties = new Properties();
	private final Map<String, String> artifactData = new HashMap<>();

	private RepositoryMock() {
		try {
			try (InputStream stream = TestUtils.getResourceAsStream("/repository/repository.properties")) {
				properties.load(stream);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
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
		String fileName = artifact.getFileName();
		String resourcePath = "/repository/" + fileName;
		try (InputStream stream = this.getClass().getResourceAsStream(resourcePath)) {
			if (stream != null) {
				return Optional.of(artifact);
			}
		} catch (IOException e) {
			// ignore
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
	public Optional<InputStream> downloadArtifact(Artifact artifact) {
		String key = artifact.toString();
		if (artifactData.containsKey(key)) {
			String data = artifactData.get(key);
			InputStream stream = new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8));
			return Optional.of(stream);
		}
		String fileName = artifact.getFileName();
		String resourcePath = "/repository/" + fileName;
		InputStream stream = this.getClass().getResourceAsStream(resourcePath);
		if (stream != null) {
			return Optional.of(stream);
		}
		return Optional.empty();
	}

	/**
	 * Recreate repository.properties file
	 */
	public static void main(String[] args) {

		StringWriter writer = new StringWriter();

		MavenCentralRepository repository = new MavenCentralRepository(Duration.ofSeconds(10));

		File directory = new File("src/test/resources/repository");
		File[] files = directory.listFiles();
		assert files != null;
		Arrays.sort(files, (f1, f2) -> f1.getName().compareToIgnoreCase(f2.getName()));
		for (File file : files) {
			String name = file.getName();
			if (name.endsWith(".jar") || name.endsWith(".pom")) {
				try (FileInputStream stream = new FileInputStream(file)) {
					String checksum = DigestUtils.sha1Hex(stream);
					repository.findArtifact(checksum)
							.map(Artifact::toString)
							.ifPresent(coordinates -> writer.write("artifact." + checksum + "=" + coordinates + "\n"));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		try {
			TestUtils.saveResource("/repository/repository.properties", writer.toString(), "UTF-8");
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
