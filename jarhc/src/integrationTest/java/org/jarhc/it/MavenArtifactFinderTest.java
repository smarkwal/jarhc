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
import java.util.List;
import org.jarhc.artifacts.Artifact;
import org.jarhc.artifacts.MavenArtifactFinder;
import org.jarhc.artifacts.RepositoryException;
import org.jarhc.it.utils.MavenSearchApiMockServer;
import org.jarhc.test.log.LoggerBuilder;
import org.jarhc.test.log.LoggerUtils;
import org.jarhc.utils.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;

@ExtendWith(MavenSearchApiMockServer.class)
class MavenArtifactFinderTest {

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
	void test_findArtifacts_byChecksum_CommonsIO() throws RepositoryException, IOException {

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

		File cacheFile = new File(cacheDir, "815893df5f31da2ece4040fe0a12fd44b577afaf.txt");
		assertTrue(cacheFile.isFile());
		assertEquals("commons-io:commons-io:2.6:jar\norg.netbeans.external:org-apache-commons-io:RELEASE113:jar", FileUtils.readFileToString(cacheFile));

		assertLogger(logger)
				.hasDebug("Artifact found: 815893df5f31da2ece4040fe0a12fd44b577afaf -> [commons-io:commons-io:2.6:jar, org.netbeans.external:org-apache-commons-io:RELEASE113:jar] (time: *")
				.isEmpty();
	}

	@Test
	void test_findArtifacts_byChecksum_CommonsCodec() throws RepositoryException, IOException {

		// test
		List<Artifact> artifacts = artifactFinder.findArtifacts("093ee1760aba62d6896d578bd7d247d0fa52f0e7");

		// assert
		assertEquals(1, artifacts.size());
		Artifact artifact = artifacts.get(0);
		assertEquals("commons-codec", artifact.getGroupId());
		assertEquals("commons-codec", artifact.getArtifactId());
		assertEquals("1.11", artifact.getVersion());
		assertEquals("jar", artifact.getType());

		File cacheFile = new File(cacheDir, "093ee1760aba62d6896d578bd7d247d0fa52f0e7.txt");
		assertTrue(cacheFile.isFile());
		assertEquals("commons-codec:commons-codec:1.11:jar", FileUtils.readFileToString(cacheFile));

		assertLogger(logger)
				.hasDebug("Artifact found: 093ee1760aba62d6896d578bd7d247d0fa52f0e7 -> [commons-codec:commons-codec:1.11:jar] (time: *")
				.isEmpty();
	}

	@Test
	void test_findArtifacts_byChecksum_ASM_withMemoryCache() throws RepositoryException {

		// prepare: find artifact to have it stored in memory cache
		artifactFinder.findArtifacts("d74d4ba0dee443f68fb2dcb7fcdb945a2cd89912");
		LoggerUtils.clear(logger);

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
				.hasDebug("Artifact found: d74d4ba0dee443f68fb2dcb7fcdb945a2cd89912 -> [org.ow2.asm:asm:7.0:jar] (memory cache)")
				.isEmpty();
	}

	@Test
	void test_findArtifacts_byChecksum_ASM_withDiskCache() throws RepositoryException, IOException {

		// prepare: create checksum file in disk cache
		FileUtils.writeStringToFile("org.ow2.asm:asm:7.0:jar", new File(cacheDir, "d74d4ba0dee443f68fb2dcb7fcdb945a2cd89912.txt"));

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
				.hasDebug("Artifact found: d74d4ba0dee443f68fb2dcb7fcdb945a2cd89912 -> [org.ow2.asm:asm:7.0:jar] (disk cache)")
				.isEmpty();
	}

	@Test
	void test_findArtifacts_byChecksum_TestJettyWebApp() throws RepositoryException, IOException {

		// test
		List<Artifact> artifacts = artifactFinder.findArtifacts("9d920ed18833e7275ba688d88242af4c3711fbea");

		// assert
		assertEquals(1, artifacts.size());
		Artifact artifact = artifacts.get(0);
		assertEquals("org.eclipse.jetty", artifact.getGroupId());
		assertEquals("test-jetty-webapp", artifact.getArtifactId());
		assertEquals("9.4.20.v20190813", artifact.getVersion());
		assertEquals("war", artifact.getType());

		File cacheFile = new File(cacheDir, "9d920ed18833e7275ba688d88242af4c3711fbea.txt");
		assertTrue(cacheFile.isFile());
		assertEquals("org.eclipse.jetty:test-jetty-webapp:9.4.20.v20190813:war", FileUtils.readFileToString(cacheFile));

		assertLogger(logger)
				.hasDebug("Artifact found: 9d920ed18833e7275ba688d88242af4c3711fbea -> [org.eclipse.jetty:test-jetty-webapp:9.4.20.v20190813:war] (time: *")
				.isEmpty();
	}

	@Test
	void test_findArtifacts_byChecksum_notFound() throws RepositoryException {

		// test
		List<Artifact> artifacts = artifactFinder.findArtifacts("1234567890123456789012345678901234567890");

		// assert
		assertEquals(0, artifacts.size());

		File cacheFile = new File(cacheDir, "1234567890123456789012345678901234567890.txt");
		assertFalse(cacheFile.isFile());

		assertLogger(logger)
				.hasWarn("Artifact not found: 1234567890123456789012345678901234567890 (time: *")
				.isEmpty();
	}

	@Test
	void test_findArtifacts_byChecksum_invalidChecksum() {

		// test
		assertThrows(
				IllegalArgumentException.class,
				() -> artifactFinder.findArtifacts("this is NOT a checksum!"),
				"checksum: this is NOT a checksum!"
		);

	}

}