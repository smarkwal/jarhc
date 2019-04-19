/*
 * Copyright 2019 Stephan Markwalder
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

import org.jarhc.utils.FileUtils;
import org.jarhc.utils.IOUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class MavenLocalRepositoryTest {

	private static final String GROUP_ID = "a.b";
	private static final String ARTIFACT_ID = "c.d";
	private static final String VERSION = "1.0";
	private static final String TYPE = "jar";
	private static final Artifact ARTIFACT = new Artifact(GROUP_ID, ARTIFACT_ID, VERSION, TYPE);
	private static final String CONTENT = "[JAR file content]";
	private static final String CHECKSUM = "abcdef0123456789";

	private static final String UNKNOWN = "unknown";
	private static final Artifact UNKNOWN_ARTIFACT = new Artifact(UNKNOWN, UNKNOWN, VERSION, TYPE);

	private static File directory;

	@BeforeAll
	static void setUp(@TempDir Path tempDir) throws IOException {

		// prepare local repository
		directory = tempDir.toFile();

		// add a JAR file to the repository
		File file = new File(directory, "a/b/c.d/1.0/c.d-1.0.jar");
		FileUtils.writeStringToFile(CONTENT, file);

	}

	@Test
	void test_findArtifact_byChecksum_noParent() throws RepositoryException {

		// prepare
		MavenLocalRepository repository = new MavenLocalRepository(directory, null);

		// test
		Optional<Artifact> result = repository.findArtifact(CHECKSUM);

		// assert
		assertFalse(result.isPresent());

	}

	@Test
	void test_findArtifact_byChecksum_withParent() throws RepositoryException {

		// prepare
		Repository parentRepository = Mockito.mock(Repository.class);
		Mockito.when(parentRepository.findArtifact(CHECKSUM)).thenReturn(Optional.of(ARTIFACT));
		MavenLocalRepository repository = new MavenLocalRepository(directory, parentRepository);

		// test
		Optional<Artifact> result = repository.findArtifact(CHECKSUM);

		// assert
		assertTrue(result.isPresent());
		assertSame(ARTIFACT, result.get());

	}

	@Test
	void test_findArtifact_byCoordinates_notFound_noParent() throws RepositoryException {

		// prepare
		MavenLocalRepository repository = new MavenLocalRepository(directory, null);

		// test
		Optional<Artifact> result = repository.findArtifact(UNKNOWN, UNKNOWN, VERSION, TYPE);

		// assert
		assertFalse(result.isPresent());

	}

	@Test
	void test_findArtifact_byCoordinates_notFound_withParent() throws RepositoryException {

		// prepare
		Repository parentRepository = Mockito.mock(Repository.class);
		Mockito.when(parentRepository.findArtifact(UNKNOWN, UNKNOWN, VERSION, TYPE)).thenReturn(Optional.empty());
		MavenLocalRepository repository = new MavenLocalRepository(directory, parentRepository);

		// test
		Optional<Artifact> result = repository.findArtifact(UNKNOWN, UNKNOWN, VERSION, TYPE);

		// assert
		assertFalse(result.isPresent());

	}

	@Test
	void test_findArtifact_byCoordinates_found() throws RepositoryException {

		// prepare
		MavenLocalRepository repository = new MavenLocalRepository(directory, null);

		// test
		Optional<Artifact> result = repository.findArtifact(GROUP_ID, ARTIFACT_ID, VERSION, TYPE);

		// assert
		assertTrue(result.isPresent());
		Artifact artifact = result.get();
		assertEquals(GROUP_ID, artifact.getGroupId());
		assertEquals(ARTIFACT_ID, artifact.getArtifactId());
		assertEquals(VERSION, artifact.getVersion());
		assertEquals(TYPE, artifact.getType());

	}

	@Test
	void test_downloadArtifact_notFound_noParent() throws RepositoryException {

		// prepare
		MavenLocalRepository repository = new MavenLocalRepository(directory, null);

		// test
		Optional<InputStream> result = repository.downloadArtifact(UNKNOWN_ARTIFACT);

		try {

			// assert
			assertFalse(result.isPresent());

		} finally {
			if (result.isPresent()) {
				try {
					result.get().close();
				} catch (IOException e) {
					// ignore
				}
			}
		}

	}

	@Test
	void test_downloadArtifact_notFound_withParent() throws RepositoryException {

		// prepare
		Repository parentRepository = Mockito.mock(Repository.class);
		Mockito.when(parentRepository.findArtifact(UNKNOWN, UNKNOWN, VERSION, TYPE)).thenReturn(Optional.empty());
		MavenLocalRepository repository = new MavenLocalRepository(directory, parentRepository);

		// test
		Optional<InputStream> result = repository.downloadArtifact(UNKNOWN_ARTIFACT);

		try {

			// assert
			assertFalse(result.isPresent());

		} finally {
			if (result.isPresent()) {
				try {
					result.get().close();
				} catch (IOException e) {
					// ignore
				}
			}
		}

	}

	@Test
	void test_downloadArtifact_found() throws RepositoryException, IOException {

		// prepare
		MavenLocalRepository repository = new MavenLocalRepository(directory, null);

		// test
		Optional<InputStream> result = repository.downloadArtifact(ARTIFACT);

		try {

			// assert
			assertTrue(result.isPresent());
			InputStream stream = result.get();
			assertTrue(stream instanceof FileInputStream);
			String content = IOUtils.toString(stream);
			assertEquals(CONTENT, content);

		} finally {
			if (result.isPresent()) {
				try {
					result.get().close();
				} catch (IOException e) {
					// ignore
				}
			}
		}

	}

}
