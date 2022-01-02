/*
 * Copyright 2021 Stephan Markwalder
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

import static org.jarhc.test.log.LoggerAssertions.assertLogger;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.jarhc.artifacts.Artifact;
import org.jarhc.artifacts.MavenArtifactFinder;
import org.jarhc.artifacts.RepositoryException;
import org.jarhc.test.log.LoggerBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

class MavenArtifactFinderIT {

	private final Logger logger = LoggerBuilder.collect(MavenArtifactFinder.class);
	private final MavenArtifactFinder artifactFinder = new MavenArtifactFinder(logger);

	@AfterEach
	void tearDown() {
		assertLogger(logger).isEmpty();
	}

	@Test
	void test_findArtifact_byChecksum_CommonsIO() throws RepositoryException {

		// test
		Optional<Artifact> artifact = artifactFinder.findArtifact("815893df5f31da2ece4040fe0a12fd44b577afaf");

		// assert
		assertTrue(artifact.isPresent());
		assertEquals("commons-io", artifact.get().getGroupId());
		assertEquals("commons-io", artifact.get().getArtifactId());
		assertEquals("2.6", artifact.get().getVersion());
		assertEquals("jar", artifact.get().getType());

		assertLogger(logger)
				.hasDebug("Multiple artifacts found: 2")
				.hasDebug("- org.netbeans.external:org-apache-commons-io:RELEASE113 (length = 54)")
				.hasDebug("- commons-io:commons-io:2.6 (length = 25)")
				.hasDebug("Shortest: commons-io:commons-io:2.6 (length = 25)")
				.hasDebug("Artifact found: 815893df5f31da2ece4040fe0a12fd44b577afaf -> commons-io:commons-io:2.6 (time: *")
				.isEmpty();
	}

	@Test
	void test_findArtifact_byChecksum_CommonsCodec() throws RepositoryException {

		// test
		Optional<Artifact> artifact = artifactFinder.findArtifact("093ee1760aba62d6896d578bd7d247d0fa52f0e7");

		// assert
		assertTrue(artifact.isPresent());
		assertEquals("commons-codec", artifact.get().getGroupId());
		assertEquals("commons-codec", artifact.get().getArtifactId());
		assertEquals("1.11", artifact.get().getVersion());
		assertEquals("jar", artifact.get().getType());

		assertLogger(logger)
				.hasDebug("Artifact found: 093ee1760aba62d6896d578bd7d247d0fa52f0e7 -> commons-codec:commons-codec:1.11 (time: *")
				.isEmpty();
	}

	@Test
	void test_findArtifact_byChecksum_ASM_withCached() throws RepositoryException {

		// test
		Optional<Artifact> artifact = artifactFinder.findArtifact("d74d4ba0dee443f68fb2dcb7fcdb945a2cd89912");

		// assert
		assertTrue(artifact.isPresent());
		assertEquals("org.ow2.asm", artifact.get().getGroupId());
		assertEquals("asm", artifact.get().getArtifactId());
		assertEquals("7.0", artifact.get().getVersion());
		assertEquals("jar", artifact.get().getType());

		assertLogger(logger)
				.hasDebug("Artifact found: d74d4ba0dee443f68fb2dcb7fcdb945a2cd89912 -> org.ow2.asm:asm:7.0 (time: *")
				.isEmpty();

		// test cache
		artifact = artifactFinder.findArtifact("d74d4ba0dee443f68fb2dcb7fcdb945a2cd89912");

		// assert
		assertTrue(artifact.isPresent());
		assertEquals("org.ow2.asm", artifact.get().getGroupId());
		assertEquals("asm", artifact.get().getArtifactId());
		assertEquals("7.0", artifact.get().getVersion());
		assertEquals("jar", artifact.get().getType());

		assertLogger(logger)
				.hasDebug("Artifact found: d74d4ba0dee443f68fb2dcb7fcdb945a2cd89912 -> org.ow2.asm:asm:7.0 (cached)")
				.isEmpty();
	}

	@Test
	void test_findArtifact_byChecksum_TestJettyWebApp() throws RepositoryException {

		// test
		Optional<Artifact> artifact = artifactFinder.findArtifact("9d920ed18833e7275ba688d88242af4c3711fbea");

		// assert
		assertTrue(artifact.isPresent());
		assertEquals("org.eclipse.jetty", artifact.get().getGroupId());
		assertEquals("test-jetty-webapp", artifact.get().getArtifactId());
		assertEquals("9.4.20.v20190813", artifact.get().getVersion());
		assertEquals("war", artifact.get().getType());

		assertLogger(logger)
				.hasDebug("Artifact found: 9d920ed18833e7275ba688d88242af4c3711fbea -> org.eclipse.jetty:test-jetty-webapp:9.4.20.v20190813 (time: *")
				.isEmpty();
	}

	@Test
	void test_findArtifact_byChecksum_notFound() throws RepositoryException {

		// test
		Optional<Artifact> artifact = artifactFinder.findArtifact("1234567890123456789012345678901234567890");

		// assert
		assertFalse(artifact.isPresent());

		assertLogger(logger)
				.hasWarn("Artifact not found: 1234567890123456789012345678901234567890 (time: *")
				.isEmpty();
	}

}