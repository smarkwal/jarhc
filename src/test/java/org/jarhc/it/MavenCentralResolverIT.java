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

package org.jarhc.it;

import org.jarhc.artifacts.Artifact;
import org.jarhc.artifacts.MavenCentralResolver;
import org.jarhc.artifacts.ResolverException;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class MavenCentralResolverIT {

	private final Duration timeout = Duration.ofSeconds(10);
	private final MavenCentralResolver resolver = new MavenCentralResolver(timeout);

	@Test
	void test_commons_io() throws ResolverException {

		// test
		Optional<Artifact> artifact = resolver.getArtifact("815893df5f31da2ece4040fe0a12fd44b577afaf");

		// assert
		assertTrue(artifact.isPresent());
		assertEquals("commons-io", artifact.get().getGroupId());
		assertEquals("commons-io", artifact.get().getArtifactId());
		assertEquals("2.6", artifact.get().getVersion());
		assertEquals("jar", artifact.get().getType());

	}

	@Test
	void test_commons_codec() throws ResolverException {

		// test
		Optional<Artifact> artifact = resolver.getArtifact("093ee1760aba62d6896d578bd7d247d0fa52f0e7");

		// assert
		assertTrue(artifact.isPresent());
		assertEquals("commons-codec", artifact.get().getGroupId());
		assertEquals("commons-codec", artifact.get().getArtifactId());
		assertEquals("1.11", artifact.get().getVersion());
		assertEquals("jar", artifact.get().getType());

	}

	@Test
	void test_asm() throws ResolverException {

		// test
		Optional<Artifact> artifact = resolver.getArtifact("d74d4ba0dee443f68fb2dcb7fcdb945a2cd89912");

		// assert
		assertTrue(artifact.isPresent());
		assertEquals("org.ow2.asm", artifact.get().getGroupId());
		assertEquals("asm", artifact.get().getArtifactId());
		assertEquals("7.0", artifact.get().getVersion());
		assertEquals("jar", artifact.get().getType());

	}

	@Test
	void test_notfound() throws ResolverException {

		// test
		Optional<Artifact> artifact = resolver.getArtifact("1234567890123456789012345678901234567890");

		// assert
		assertFalse(artifact.isPresent());

	}


}
