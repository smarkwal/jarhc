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

package org.jarhc.it;

import static org.jarhc.test.log.LoggerAssertions.assertLogger;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.jarhc.artifacts.Artifact;
import org.jarhc.artifacts.ArtifactFinder;
import org.jarhc.artifacts.DepsDevApiArtifactFinder;
import org.jarhc.artifacts.RepositoryException;
import org.jarhc.it.utils.MavenProxyServerExtension;
import org.jarhc.test.log.LoggerBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;

@ExtendWith(MavenProxyServerExtension.class)
class DepsDevApiArtifactFinderTest {

	private final Logger logger = LoggerBuilder.collect(DepsDevApiArtifactFinder.class);

	private ArtifactFinder artifactFinder;

	@BeforeEach
	void setUp() {
		// the finder reads the deps.dev base URL from system property "jarhc.depsdev.url",
		// which is set by MavenProxyServerExtension
		artifactFinder = new DepsDevApiArtifactFinder(logger);
	}

	@AfterEach
	void tearDown() {
		assertLogger(logger).isEmpty();
	}

	@Test
	void test_findArtifacts_byChecksum_CommonsIO() throws RepositoryException {

		// test
		List<Artifact> artifacts = artifactFinder.findArtifacts("815893df5f31da2ece4040fe0a12fd44b577afaf");

		// assert
		assertEquals(2, artifacts.size());
		Artifact artifact = artifacts.get(0);
		assertEquals("commons-io", artifact.getGroupId());
		assertEquals("commons-io", artifact.getArtifactId());
		assertEquals("2.6", artifact.getVersion());
		assertEquals("jar", artifact.getType());
		artifact = artifacts.get(1);
		assertEquals("org.netbeans.external", artifact.getGroupId());
		assertEquals("org-apache-commons-io", artifact.getArtifactId());
		assertEquals("RELEASE113", artifact.getVersion());
		assertEquals("jar", artifact.getType());

		assertLogger(logger)
				.hasDebug("Artifact found: 815893df5f31da2ece4040fe0a12fd44b577afaf -> [commons-io:commons-io:2.6:jar, org.netbeans.external:org-apache-commons-io:RELEASE113:jar] (time: *")
				.isEmpty();
	}

	@Test
	void test_findArtifacts_byChecksum_CommonsCodec() throws RepositoryException {

		// test
		List<Artifact> artifacts = artifactFinder.findArtifacts("093ee1760aba62d6896d578bd7d247d0fa52f0e7");

		// assert
		assertEquals(1, artifacts.size());
		Artifact artifact = artifacts.get(0);
		assertEquals("commons-codec", artifact.getGroupId());
		assertEquals("commons-codec", artifact.getArtifactId());
		assertEquals("1.11", artifact.getVersion());
		assertEquals("jar", artifact.getType());

		assertLogger(logger)
				.hasDebug("Artifact found: 093ee1760aba62d6896d578bd7d247d0fa52f0e7 -> [commons-codec:commons-codec:1.11:jar] (time: *")
				.isEmpty();
	}

	@Test
	void test_findArtifacts_byChecksum_ASM() throws RepositoryException {

		// test
		List<Artifact> artifacts = artifactFinder.findArtifacts("d74d4ba0dee443f68fb2dcb7fcdb945a2cd89912");

		// assert
		assertEquals(1, artifacts.size());
		Artifact artifact = artifacts.get(0);
		assertEquals("org.ow2.asm", artifact.getGroupId());
		assertEquals("asm", artifact.getArtifactId());
		assertEquals("7.0", artifact.getVersion());
		assertEquals("jar", artifact.getType());

		assertLogger(logger)
				.hasDebug("Artifact found: d74d4ba0dee443f68fb2dcb7fcdb945a2cd89912 -> [org.ow2.asm:asm:7.0:jar] (time: *")
				.isEmpty();
	}

	@Test
	void test_findArtifacts_byChecksum_TestJettyWebApp_notIndexed() throws RepositoryException {

		// Note: the Maven Search API resolves this checksum to a "war" artifact, but
		// the deps.dev API does not index the Jetty test web application, so the
		// deps.dev finder reports it as not found.

		// test
		List<Artifact> artifacts = artifactFinder.findArtifacts("9d920ed18833e7275ba688d88242af4c3711fbea");

		// assert
		assertEquals(0, artifacts.size());

		assertLogger(logger)
				.hasWarn("Artifact not found: 9d920ed18833e7275ba688d88242af4c3711fbea (time: *")
				.isEmpty();
	}

	@Test
	void test_findArtifacts_byChecksum_notFound() throws RepositoryException {

		// test
		List<Artifact> artifacts = artifactFinder.findArtifacts("1234567890123456789012345678901234567890");

		// assert
		assertEquals(0, artifacts.size());

		assertLogger(logger)
				.hasWarn("Artifact not found: 1234567890123456789012345678901234567890 (time: *")
				.isEmpty();
	}

	@Test
	void test_findArtifacts_byChecksum_invalidChecksum() {

		// test
		assertThrows(
				IllegalArgumentException.class,
				() -> artifactFinder.findArtifacts("this is NOT a checksum!")
		);

	}

}
