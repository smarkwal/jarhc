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

package org.jarhc.test.server.search;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

class LocalSearchClient implements SearchClient {

	private final Path rootPath;

	public LocalSearchClient(Path rootPath) {
		if (rootPath == null) throw new IllegalArgumentException("rootPath");
		if (!Files.isDirectory(rootPath)) throw new IllegalArgumentException("Directory not found: " + rootPath.toAbsolutePath());
		this.rootPath = rootPath.toAbsolutePath();
	}

	@Override
	public Optional<byte[]> get(String checksum) throws IOException {
		Path file = getFile(checksum);
		if (Files.exists(file)) {
			byte[] data = Files.readAllBytes(file);
			return Optional.of(data);
		}
		return Optional.empty();
	}

	public void put(String checksum, byte[] bytes) throws IOException {
		Path file = getFile(checksum);
		Files.createDirectories(file.getParent());
		Files.write(file, bytes);
	}

	private Path getFile(String checksum) {
		if (checksum == null) throw new IllegalArgumentException("checksum");
		if (!checksum.matches("^[0-9a-f]{40}$")) throw new IllegalArgumentException("checksum");
		String fileName = checksum + ".json";
		Path file = rootPath.resolve(fileName).toAbsolutePath();
		if (!file.startsWith(rootPath)) {
			throw new IllegalArgumentException("Invalid checksum: " + checksum);
		}
		return file;
	}

}
