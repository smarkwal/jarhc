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
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jarhc.TestUtils;
import org.jarhc.artifacts.Artifact;
import org.jarhc.artifacts.ArtifactFinder;
import org.jarhc.test.TestDataException;

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
			throw new TestDataException(e);
		}
	}

	@Override
	public List<Artifact> findArtifacts(String checksum) {
		String value = properties.getProperty(checksum);
		if (value == null) {
			String message = String.format("Checksum not found in test data: %s", checksum);
			throw new TestDataException(message);
		}
		if (value.isEmpty()) {
			return List.of();
		}
		String[] values = value.split(",");
		return Stream.of(values).map(Artifact::new).collect(Collectors.toList());
	}

}
