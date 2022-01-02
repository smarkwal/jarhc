/*
 * Copyright 2022 Stephan Markwalder
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

package org.jarhc.it.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Properties;
import org.jarhc.TestUtils;
import org.jarhc.artifacts.Artifact;
import org.jarhc.artifacts.ArtifactFinder;
import org.jarhc.test.TestDataError;

/**
 * Mock implementation of an {@link ArtifactFinder} based on checksums and
 * artifact coordinates stored in test resource "checksums.txt".
 */
public class ArtifactFinderMock implements ArtifactFinder {

	private static ArtifactFinderMock artifactFinder;

	public static ArtifactFinder getArtifactFinder() {
		if (artifactFinder == null) {
			artifactFinder = new ArtifactFinderMock();
		}
		return artifactFinder;
	}

	private final Properties properties;

	private ArtifactFinderMock() {
		this.properties = new Properties();
		try (InputStream stream = TestUtils.getResourceAsStream("/checksums.txt")) {
			properties.load(stream);
		} catch (IOException e) {
			throw new TestDataError("Test data I/O error.", e);
		}
	}

	@Override
	public Optional<Artifact> findArtifact(String checksum) {
		String coordinates = properties.getProperty(checksum);
		if (coordinates == null) {
			throw new TestDataError("Checksum not found in test data: " + checksum);
		}
		if (coordinates.isEmpty()) {
			return Optional.empty();
		}
		Artifact artifact = new Artifact(coordinates);
		return Optional.of(artifact);
	}

}
