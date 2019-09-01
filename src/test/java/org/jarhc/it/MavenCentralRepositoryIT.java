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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Optional;
import org.jarhc.artifacts.Artifact;
import org.jarhc.artifacts.MavenCentralRepository;
import org.jarhc.artifacts.RepositoryException;
import org.jarhc.utils.IOUtils;
import org.junit.jupiter.api.Test;

class MavenCentralRepositoryIT {

	private final Duration timeout = Duration.ofSeconds(10);
	private final MavenCentralRepository repository = new MavenCentralRepository(timeout);

	@Test
	void test_findArtifact_byChecksum_CommonsIO() throws RepositoryException {

		// test
		Optional<Artifact> artifact = repository.findArtifact("815893df5f31da2ece4040fe0a12fd44b577afaf");

		// assert
		assertTrue(artifact.isPresent());
		assertEquals("commons-io", artifact.get().getGroupId());
		assertEquals("commons-io", artifact.get().getArtifactId());
		assertEquals("2.6", artifact.get().getVersion());
		assertEquals("jar", artifact.get().getType());

	}

	@Test
	void test_findArtifact_byCoordinates_CommonsIO() throws RepositoryException {

		// test
		Optional<Artifact> artifact = repository.findArtifact("commons-io", "commons-io", "2.6", "jar");

		// assert
		assertTrue(artifact.isPresent());
		assertEquals("commons-io", artifact.get().getGroupId());
		assertEquals("commons-io", artifact.get().getArtifactId());
		assertEquals("2.6", artifact.get().getVersion());
		assertEquals("jar", artifact.get().getType());

	}

	@Test
	void test_downloadArtifact_CommonsIO() throws RepositoryException, IOException {

		Artifact artifact = new Artifact("commons-io", "commons-io", "2.6", "jar");

		// test
		Optional<InputStream> stream = repository.downloadArtifact(artifact);

		// assert
		assertTrue(stream.isPresent());
		byte[] data = IOUtils.toByteArray(stream.get());
		assertEquals(214788, data.length);

	}

	@Test
	void test_findArtifact_byChecksum_CommonsCodec() throws RepositoryException {

		// test
		Optional<Artifact> artifact = repository.findArtifact("093ee1760aba62d6896d578bd7d247d0fa52f0e7");

		// assert
		assertTrue(artifact.isPresent());
		assertEquals("commons-codec", artifact.get().getGroupId());
		assertEquals("commons-codec", artifact.get().getArtifactId());
		assertEquals("1.11", artifact.get().getVersion());
		assertEquals("jar", artifact.get().getType());

	}

	@Test
	void test_findArtifact_byCoordinates_CommonsCodec() throws RepositoryException {

		// test
		Optional<Artifact> artifact = repository.findArtifact("commons-codec", "commons-codec", "1.11", "jar");

		// assert
		assertTrue(artifact.isPresent());
		assertEquals("commons-codec", artifact.get().getGroupId());
		assertEquals("commons-codec", artifact.get().getArtifactId());
		assertEquals("1.11", artifact.get().getVersion());
		assertEquals("jar", artifact.get().getType());

	}

	@Test
	void test_downloadArtifact_CommonsCodec() throws RepositoryException, IOException {

		Artifact artifact = new Artifact("commons-codec", "commons-codec", "1.11", "jar");

		// test
		Optional<InputStream> stream = repository.downloadArtifact(artifact);

		// assert
		assertTrue(stream.isPresent());
		byte[] data = IOUtils.toByteArray(stream.get());
		assertEquals(335042, data.length);

	}

	@Test
	void test_findArtifact_byChecksum_ASM() throws RepositoryException {

		// test
		Optional<Artifact> artifact = repository.findArtifact("d74d4ba0dee443f68fb2dcb7fcdb945a2cd89912");

		// assert
		assertTrue(artifact.isPresent());
		assertEquals("org.ow2.asm", artifact.get().getGroupId());
		assertEquals("asm", artifact.get().getArtifactId());
		assertEquals("7.0", artifact.get().getVersion());
		assertEquals("jar", artifact.get().getType());

	}

	@Test
	void test_findArtifact_byCoordinates_ASM() throws RepositoryException {

		// test
		Optional<Artifact> artifact = repository.findArtifact("org.ow2.asm", "asm", "7.0", "jar");

		// assert
		assertTrue(artifact.isPresent());
		assertEquals("org.ow2.asm", artifact.get().getGroupId());
		assertEquals("asm", artifact.get().getArtifactId());
		assertEquals("7.0", artifact.get().getVersion());
		assertEquals("jar", artifact.get().getType());

	}

	@Test
	void test_downloadArtifact_ASM() throws RepositoryException, IOException {

		Artifact artifact = new Artifact("org.ow2.asm", "asm", "7.0", "jar");

		// test
		Optional<InputStream> stream = repository.downloadArtifact(artifact);

		// assert
		assertTrue(stream.isPresent());
		byte[] data = IOUtils.toByteArray(stream.get());
		assertEquals(113676, data.length);

	}

	@Test
	void test_findArtifact_byChecksum_notFound() throws RepositoryException {

		// test
		Optional<Artifact> artifact = repository.findArtifact("1234567890123456789012345678901234567890");

		// assert
		assertFalse(artifact.isPresent());

	}

	@Test
	void test_findArtifact_byCoordinates_notFound() throws RepositoryException {

		// test
		Optional<Artifact> artifact = repository.findArtifact("unknown", "unknown", "1.0", "jar");

		// assert
		assertFalse(artifact.isPresent());

	}

	@Test
	void test_downloadArtifact_notFound() throws RepositoryException, IOException {

		Artifact artifact = new Artifact("unknown", "unknown", "1.0", "jar");

		// test
		Optional<InputStream> stream = repository.downloadArtifact(artifact);

		// assert
		assertFalse(stream.isPresent());

	}


}
