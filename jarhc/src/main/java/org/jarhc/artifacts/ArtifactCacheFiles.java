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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.jarhc.utils.FileUtils;
import org.jarhc.utils.JarHcException;

final class ArtifactCacheFiles {

	private ArtifactCacheFiles() {
		// utility class
	}

	static List<Artifact> readArtifactsFromFile(File cacheFile) {

		String[] lines;
		try {
			lines = FileUtils.readFileToString(cacheFile).split("\n");
		} catch (IOException e) {
			throw new JarHcException(e);
		}

		List<Artifact> artifacts = new ArrayList<>();
		for (String coordinates : lines) {
			if (Artifact.validateCoordinates(coordinates)) {
				artifacts.add(new Artifact(coordinates));
			} else {
				throw new JarHcException("Invalid artifact coordinates: " + coordinates);
			}
		}

		return artifacts;
	}

	static void writeArtifactsToFile(List<Artifact> artifacts, File cacheFile) {
		String lines = artifacts.stream().map(Artifact::toString).collect(Collectors.joining("\n"));
		try {
			FileUtils.writeStringToFile(lines, cacheFile);
		} catch (IOException e) {
			throw new JarHcException(e);
		}
	}
}
