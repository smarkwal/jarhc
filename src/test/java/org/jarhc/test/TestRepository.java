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
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.jarhc.TestUtils;
import org.jarhc.artifacts.Artifact;
import org.jarhc.artifacts.MavenCentralRepository;
import org.jarhc.artifacts.Repository;
import org.jarhc.utils.DigestUtils;
import org.jarhc.utils.FileUtils;
import org.jarhc.utils.StringUtils;

/**
 * A repository implementation loading all information and artifacts from
 * test resources.
 */
public class TestRepository implements Repository {

	public static TestRepository createRepository() {
		return new TestRepository();
	}

	private final Map<String, String> index = new HashMap<>();
	private final Map<String, String> data = new HashMap<>();

	private TestRepository() {

		try {
			List<String> lines = TestUtils.getResourceAsLines("/repository/index.txt", "UTF-8");
			for (String line : lines) {
				int pos = line.indexOf('=');
				String coordinates = line.substring(0, pos).trim();
				String checksum = line.substring(pos + 1).trim();
				index.put(checksum, coordinates);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

	public void addArtifactData(Artifact artifact, String content) {
		String coordinates = artifact.toString();
		String checksum = DigestUtils.sha1Hex(content);
		index.put(checksum, coordinates);
		data.put(coordinates, content);
	}

	@Override
	public Optional<Artifact> findArtifact(String groupId, String artifactId, String version, String type) {

		// try to find artifact in index
		Artifact artifact = new Artifact(groupId, artifactId, version, type);
		if (index.containsValue(artifact.toString())) {
			return Optional.of(artifact);
		}

		// try to find artifact in test resources
		String resourcePath = "/repository/" + artifact.getPath();
		try (InputStream stream = this.getClass().getResourceAsStream(resourcePath)) {
			if (stream != null) {
				return Optional.of(artifact);
			}
		} catch (IOException e) {
			// ignore
		}

		// artifact not found
		return Optional.empty();
	}

	@Override
	public Optional<Artifact> findArtifact(String checksum) {

		// try to find artifact in index
		if (index.containsKey(checksum)) {
			String coordinates = index.get(checksum);
			Artifact artifact = new Artifact(coordinates);
			return Optional.of(artifact);
		}

		// artifact not found
		return Optional.empty();
	}

	@Override
	public Optional<InputStream> downloadArtifact(Artifact artifact) {

		// try to find artifact in memory
		String key = artifact.toString();
		if (data.containsKey(key)) {
			String data = this.data.get(key);
			InputStream stream = new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8));
			return Optional.of(stream);
		}

		// try to find artifact in test resources
		String resourcePath = "/repository/" + artifact.getPath();
		InputStream stream = this.getClass().getResourceAsStream(resourcePath);
		if (stream != null) {
			return Optional.of(stream);
		}

		// artifact not found
		return Optional.empty();
	}

	/**
	 * Recreate index.txt file
	 */
	public static void main(String[] args) throws IOException {

		List<String> lines = new ArrayList<>();

		MavenCentralRepository repository = new MavenCentralRepository(Duration.ofSeconds(10));

		String basePath = "src/test/resources/repository";
		Path directory = Paths.get(basePath);
		Files.walk(directory)
				.filter(file -> Files.isRegularFile(file))
				.filter(file -> file.toString().endsWith(".jar"))
				.forEach(
						file -> {
							try {
								String checksum = FileUtils.sha1Hex(file.toFile());
								Optional<Artifact> artifact = repository.findArtifact(checksum);
								if (artifact.isPresent()) {
									String line = artifact.get().toString() + " = " + checksum;
									lines.add(line);

									Path expectedFile = Paths.get(basePath, artifact.get().getPath());
									if (!file.equals(expectedFile)) {
										System.out.println("File found at an unexpected path:");
										System.out.println("   Expected: " + expectedFile);
										System.out.println("   Actual  : " + file);
									}

								} else {
									System.err.println("Artifact not found: " + file + " with checksum " + checksum);
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
				);

		lines.sort(String.CASE_INSENSITIVE_ORDER);

		File indexFile = new File(basePath, "index.txt");
		FileUtils.writeStringToFile(StringUtils.joinLines(lines), indexFile);

	}

}
