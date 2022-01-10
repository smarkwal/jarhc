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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import org.jarhc.artifacts.Artifact;
import org.jarhc.artifacts.MavenArtifactFinder;
import org.jarhc.artifacts.RepositoryException;
import org.jarhc.test.log.LoggerBuilder;
import org.jarhc.test.log.LoggerUtils;
import org.jarhc.utils.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;

class MavenArtifactFinderIT {

	private final Logger logger = LoggerBuilder.collect(MavenArtifactFinder.class);
	private File cacheDir;
	private MavenArtifactFinder artifactFinder;

	@BeforeEach
	void setUp(@TempDir Path tempDir) {
		cacheDir = tempDir.toFile();
		artifactFinder = new MavenArtifactFinder(cacheDir, logger);
	}

	@AfterEach
	void tearDown() {
		assertLogger(logger).isEmpty();
	}

	@Test
	void test_findArtifact_byChecksum_CommonsIO() throws RepositoryException, IOException {

		// test
		Optional<Artifact> artifact = artifactFinder.findArtifact("815893df5f31da2ece4040fe0a12fd44b577afaf");

		// assert
		assertTrue(artifact.isPresent());
		assertEquals("commons-io", artifact.get().getGroupId());
		assertEquals("commons-io", artifact.get().getArtifactId());
		assertEquals("2.6", artifact.get().getVersion());
		assertEquals("jar", artifact.get().getType());

		File cacheFile = new File(cacheDir, "815893df5f31da2ece4040fe0a12fd44b577afaf.txt");
		assertTrue(cacheFile.isFile());
		assertEquals("commons-io:commons-io:2.6:jar", FileUtils.readFileToString(cacheFile));

		assertLogger(logger)
				.hasDebug("Multiple artifacts found: 2")
				.hasDebug("- org.netbeans.external:org-apache-commons-io:RELEASE113 (length = 54)")
				.hasDebug("- commons-io:commons-io:2.6 (length = 25)")
				.hasDebug("Shortest: commons-io:commons-io:2.6 (length = 25)")
				.hasDebug("Artifact found: 815893df5f31da2ece4040fe0a12fd44b577afaf -> commons-io:commons-io:2.6:jar (time: *")
				.isEmpty();
	}

	@Test
	void test_findArtifact_byChecksum_CommonsCodec() throws RepositoryException, IOException {

		// test
		Optional<Artifact> artifact = artifactFinder.findArtifact("093ee1760aba62d6896d578bd7d247d0fa52f0e7");

		// assert
		assertTrue(artifact.isPresent());
		assertEquals("commons-codec", artifact.get().getGroupId());
		assertEquals("commons-codec", artifact.get().getArtifactId());
		assertEquals("1.11", artifact.get().getVersion());
		assertEquals("jar", artifact.get().getType());

		File cacheFile = new File(cacheDir, "093ee1760aba62d6896d578bd7d247d0fa52f0e7.txt");
		assertTrue(cacheFile.isFile());
		assertEquals("commons-codec:commons-codec:1.11:jar", FileUtils.readFileToString(cacheFile));

		assertLogger(logger)
				.hasDebug("Artifact found: 093ee1760aba62d6896d578bd7d247d0fa52f0e7 -> commons-codec:commons-codec:1.11:jar (time: *")
				.isEmpty();
	}

	@Test
	void test_findArtifact_byChecksum_ASM_withMemoryCache() throws RepositoryException {

		// prepare: find artifact to have it stored in memory cache
		artifactFinder.findArtifact("d74d4ba0dee443f68fb2dcb7fcdb945a2cd89912");
		LoggerUtils.clear(logger);

		// test
		Optional<Artifact> artifact = artifactFinder.findArtifact("d74d4ba0dee443f68fb2dcb7fcdb945a2cd89912");

		// assert
		assertTrue(artifact.isPresent());
		assertEquals("org.ow2.asm", artifact.get().getGroupId());
		assertEquals("asm", artifact.get().getArtifactId());
		assertEquals("7.0", artifact.get().getVersion());
		assertEquals("jar", artifact.get().getType());

		assertLogger(logger)
				.hasDebug("Artifact found: d74d4ba0dee443f68fb2dcb7fcdb945a2cd89912 -> org.ow2.asm:asm:7.0:jar (memory cache)")
				.isEmpty();
	}

	@Test
	void test_findArtifact_byChecksum_ASM_withDiskCache() throws RepositoryException, IOException {

		// prepare: create checksum file in disk cache
		FileUtils.writeStringToFile("org.ow2.asm:asm:7.0:jar", new File(cacheDir, "d74d4ba0dee443f68fb2dcb7fcdb945a2cd89912.txt"));

		// test
		Optional<Artifact> artifact = artifactFinder.findArtifact("d74d4ba0dee443f68fb2dcb7fcdb945a2cd89912");

		// assert
		assertTrue(artifact.isPresent());
		assertEquals("org.ow2.asm", artifact.get().getGroupId());
		assertEquals("asm", artifact.get().getArtifactId());
		assertEquals("7.0", artifact.get().getVersion());
		assertEquals("jar", artifact.get().getType());

		assertLogger(logger)
				.hasDebug("Artifact found: d74d4ba0dee443f68fb2dcb7fcdb945a2cd89912 -> org.ow2.asm:asm:7.0:jar (disk cache)")
				.isEmpty();
	}

	@Test
	void test_findArtifact_byChecksum_TestJettyWebApp() throws RepositoryException, IOException {

		// test
		Optional<Artifact> artifact = artifactFinder.findArtifact("9d920ed18833e7275ba688d88242af4c3711fbea");

		// assert
		assertTrue(artifact.isPresent());
		assertEquals("org.eclipse.jetty", artifact.get().getGroupId());
		assertEquals("test-jetty-webapp", artifact.get().getArtifactId());
		assertEquals("9.4.20.v20190813", artifact.get().getVersion());
		assertEquals("war", artifact.get().getType());

		File cacheFile = new File(cacheDir, "9d920ed18833e7275ba688d88242af4c3711fbea.txt");
		assertTrue(cacheFile.isFile());
		assertEquals("org.eclipse.jetty:test-jetty-webapp:9.4.20.v20190813:war", FileUtils.readFileToString(cacheFile));

		assertLogger(logger)
				.hasDebug("Artifact found: 9d920ed18833e7275ba688d88242af4c3711fbea -> org.eclipse.jetty:test-jetty-webapp:9.4.20.v20190813:war (time: *")
				.isEmpty();
	}

	@Test
	void test_findArtifact_byChecksum_notFound() throws RepositoryException {

		// test
		Optional<Artifact> artifact = artifactFinder.findArtifact("1234567890123456789012345678901234567890");

		// assert
		assertFalse(artifact.isPresent());

		File cacheFile = new File(cacheDir, "1234567890123456789012345678901234567890.txt");
		assertFalse(cacheFile.isFile());

		assertLogger(logger)
				.hasWarn("Artifact not found: 1234567890123456789012345678901234567890 (time: *")
				.isEmpty();
	}

	@Test
	void test_findArtifact_byChecksum_invalidChecksum() throws RepositoryException {

		// test
		assertThrows(
				IllegalArgumentException.class,
				() -> artifactFinder.findArtifact("this is NOT a checksum!"),
				"checksum: this is NOT a checksum!"
		);

	}

}