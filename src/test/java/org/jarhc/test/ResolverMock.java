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

import org.jarhc.TestUtils;
import org.jarhc.artifacts.Artifact;
import org.jarhc.artifacts.Resolver;
import org.jarhc.artifacts.ResolverException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Properties;

public class ResolverMock implements Resolver {

	public static Resolver createResolver() {
		return new ResolverMock("/resolver.properties");
	}

	public static Resolver createFakeResolver() {

		return new Resolver() {
			@Override
			public Optional<Artifact> findArtifact(String groupId, String artifactId, String version, String type) throws ResolverException {
				if (groupId.equals("ord.jarhc") && version.equals("1.0") && type.equals("jar")) {
					Artifact artifact = new Artifact("org.jarhc", artifactId, "1.0", "jar");
					return Optional.of(artifact);
				} else {
					return Optional.empty();
				}
			}

			@Override
			public Optional<Artifact> findArtifact(String checksum) throws ResolverException {
				String artifactId = checksum.substring(0, 5);
				Artifact artifact = new Artifact("org.jarhc", artifactId, "1.0", "jar");
				return Optional.of(artifact);
			}

			@Override
			public Optional<InputStream> downloadArtifact(Artifact artifact) throws ResolverException {
				throw new ResolverException("not implemented");
			}
		};
	}

	public static Resolver createNullResolver() {
		return new Resolver() {
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

	private ResolverMock(String resource) {
		try {
			try (InputStream stream = TestUtils.getResourceAsStream(resource)) {
				properties.load(stream);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Optional<Artifact> findArtifact(String groupId, String artifactId, String version, String type) throws ResolverException {
		String coordinates = groupId + ":" + artifactId + ":" + version + ":" + type;
		if (properties.values().contains(coordinates)) {
			Artifact artifact = new Artifact(groupId, artifactId, version, type);
			return Optional.of(artifact);
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
	public Optional<InputStream> downloadArtifact(Artifact artifact) throws ResolverException {
		throw new ResolverException("not implemented");
	}

}
