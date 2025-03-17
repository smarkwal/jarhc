/*
 * Copyright 2025 Stephan Markwalder
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

package org.jarhc.test.server.repo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

class LocalRepoClient implements RepoClient {

	private final Path rootPath;

	public LocalRepoClient(Path rootPath) {
		if (rootPath == null) throw new IllegalArgumentException("rootPath");
		if (!Files.isDirectory(rootPath)) throw new IllegalArgumentException("Directory not found: " + rootPath.toAbsolutePath());
		this.rootPath = rootPath.toAbsolutePath();
	}

	@Override
	public Optional<byte[]> get(String path) throws IOException {
		Path file = getFile(path);
		if (Files.exists(file)) {
			byte[] data = Files.readAllBytes(file);
			return Optional.of(data);
		}
		return Optional.empty();
	}

	public void put(String path, byte[] bytes) throws IOException {
		Path file = getFile(path);
		Files.createDirectories(file.getParent());
		Files.write(file, bytes);
	}

	private Path getFile(String path) {
		Path file = rootPath.resolve(path).toAbsolutePath();
		if (!file.startsWith(rootPath)) {
			throw new IllegalArgumentException("Invalid path: " + path);
		}
		return file;
	}

}
