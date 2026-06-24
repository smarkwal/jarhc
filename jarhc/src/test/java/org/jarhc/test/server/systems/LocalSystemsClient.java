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

package org.jarhc.test.server.systems;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Reads and writes locally stored deps.dev "GetVersion" responses, using the
 * layout <code>&lt;groupId&gt;/&lt;artifactId&gt;/&lt;version&gt;.json</code>.
 */
class LocalSystemsClient {

	private final Path rootPath;

	LocalSystemsClient(Path rootPath) {
		if (rootPath == null) throw new IllegalArgumentException("rootPath");
		if (!Files.isDirectory(rootPath)) throw new IllegalArgumentException("Directory not found: " + rootPath.toAbsolutePath());
		this.rootPath = rootPath.toAbsolutePath();
	}

	Optional<byte[]> get(String groupId, String artifactId, String version) throws IOException {
		Path file = getFile(groupId, artifactId, version);
		if (Files.exists(file)) {
			byte[] data = Files.readAllBytes(file);
			return Optional.of(data);
		}
		return Optional.empty();
	}

	void put(String groupId, String artifactId, String version, byte[] bytes) throws IOException {
		Path file = getFile(groupId, artifactId, version);
		Files.createDirectories(file.getParent());
		Files.write(file, bytes);
	}

	private Path getFile(String groupId, String artifactId, String version) {
		String fileName = artifactId + "/" + version + ".json";
		Path file = rootPath.resolve(groupId).resolve(fileName).toAbsolutePath();
		if (!file.startsWith(rootPath)) {
			throw new IllegalArgumentException("Invalid coordinates: " + groupId + ":" + artifactId + ":" + version);
		}
		return file;
	}

}
