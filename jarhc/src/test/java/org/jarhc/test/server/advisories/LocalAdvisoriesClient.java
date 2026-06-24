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

package org.jarhc.test.server.advisories;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Reads and writes locally stored deps.dev "GetAdvisory" responses, one JSON
 * file per advisory ID.
 */
class LocalAdvisoriesClient {

	private final Path rootPath;

	LocalAdvisoriesClient(Path rootPath) {
		if (rootPath == null) throw new IllegalArgumentException("rootPath");
		if (!Files.isDirectory(rootPath)) throw new IllegalArgumentException("Directory not found: " + rootPath.toAbsolutePath());
		this.rootPath = rootPath.toAbsolutePath();
	}

	Optional<byte[]> get(String advisoryId) throws IOException {
		Path file = getFile(advisoryId);
		if (Files.exists(file)) {
			byte[] data = Files.readAllBytes(file);
			return Optional.of(data);
		}
		return Optional.empty();
	}

	void put(String advisoryId, byte[] bytes) throws IOException {
		Path file = getFile(advisoryId);
		Files.createDirectories(file.getParent());
		Files.write(file, bytes);
	}

	private Path getFile(String advisoryId) {
		if (!advisoryId.matches("[A-Za-z0-9._-]+")) {
			throw new IllegalArgumentException("Invalid advisory ID: " + advisoryId);
		}
		Path file = rootPath.resolve(advisoryId + ".json").toAbsolutePath();
		if (!file.startsWith(rootPath)) {
			throw new IllegalArgumentException("Invalid advisory ID: " + advisoryId);
		}
		return file;
	}

}
