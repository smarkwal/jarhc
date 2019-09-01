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

package org.jarhc.artifacts;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;
import java.nio.file.Path;
import java.util.Optional;
import org.jarhc.test.RepositoryMock;
import org.jarhc.utils.FileUtils;
import org.jarhc.utils.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.api.io.TempDir;

class CachedRepositoryTest {

	private static final String CHECKSUM_UNKNOWN = "1234567890123456789012345678901234567890";
	private static final String CHECKSUM_ASM_70 = "d74d4ba0dee443f68fb2dcb7fcdb945a2cd89912";

	private File cacheDir;
	private CachedRepository repository;
	private RepositoryMock parentRepository = RepositoryMock.createRepository();

	@BeforeEach
	void setUp(@TempDir Path tempDir) {
		cacheDir = tempDir.toFile();
		repository = new CachedRepository(cacheDir, parentRepository);
	}

	@Test
	void test_illegal_arguments() {

		// test
		Executable executable = () -> repository.findArtifact(null);
		assertThrows(IllegalArgumentException.class, executable);

		// test
		executable = () -> repository.findArtifact("");
		assertThrows(IllegalArgumentException.class, executable);

		// test
		executable = () -> repository.findArtifact("../test");
		assertThrows(IllegalArgumentException.class, executable);

	}

	@Test
	void test_findArtifact_unknown() throws RepositoryException {

		// prepare
		String checksum = CHECKSUM_UNKNOWN;
		File cacheFile = new File(cacheDir, "sha1/" + checksum + ".txt");

		// assume
		assumeFalse(cacheFile.exists());

		// test
		Optional<Artifact> artifact = repository.findArtifact(checksum);

		// assert
		assertFalse(artifact.isPresent()); // artifact has not been found
		assertTrue(cacheFile.isFile()); // negative result has been cached ...
		assertEquals(0, cacheFile.length()); // ... as an empty file

	}

	@Test
	void test_findArtifact_asm() throws RepositoryException, IOException {

		// prepare
		String checksum = CHECKSUM_ASM_70;
		File cacheFile = new File(cacheDir, "sha1/" + checksum + ".txt");

		// assume
		assumeFalse(cacheFile.exists());

		// test
		Optional<Artifact> artifact = repository.findArtifact(checksum);

		// assert
		assertTrue(artifact.isPresent()); // artifact has been found
		assertEquals("org.ow2.asm", artifact.get().getGroupId());
		assertEquals("asm", artifact.get().getArtifactId());
		assertEquals("7.0", artifact.get().getVersion());
		assertEquals("jar", artifact.get().getType());
		assertTrue(cacheFile.isFile()); // result has been cached
		assertEquals("org.ow2.asm:asm:7.0:jar", FileUtils.readFileToString(cacheFile));

	}

	@Test
	void test_findArtifact_cached() throws RepositoryException, IOException {

		// prepare
		String checksum = CHECKSUM_UNKNOWN;
		File cacheFile = new File(cacheDir, "sha1/" + checksum + ".txt");
		FileUtils.writeStringToFile("org.test:test:1.0:jar", cacheFile);

		// assume
		assumeTrue(cacheFile.isFile());

		// test
		Optional<Artifact> artifact = repository.findArtifact(checksum);

		// assert
		assertTrue(artifact.isPresent()); // artifact has been found (in cache)
		assertEquals("org.test", artifact.get().getGroupId());
		assertEquals("test", artifact.get().getArtifactId());
		assertEquals("1.0", artifact.get().getVersion());
		assertEquals("jar", artifact.get().getType());

	}

	@Test
	void test_findArtifact_cached_unknown() throws RepositoryException, IOException {

		// prepare
		String checksum = CHECKSUM_ASM_70;
		File cacheFile = new File(cacheDir, "sha1/" + checksum + ".txt");
		FileUtils.touchFile(cacheFile); // cache a negative response

		// assume
		assumeTrue(cacheFile.isFile());
		assumeTrue(cacheFile.length() == 0);

		// test
		Optional<Artifact> artifact = repository.findArtifact(checksum);

		// assert
		assertFalse(artifact.isPresent()); // artifact has not been found

	}

	@Test
	void test_findArtifact_no_parent() throws RepositoryException {

		// override
		repository = new CachedRepository(cacheDir, null);

		// prepare
		String checksum = CHECKSUM_ASM_70;
		File cacheFile = new File(cacheDir, "sha1/" + checksum + ".txt");

		// assume
		assumeFalse(cacheFile.exists());

		// test
		Optional<Artifact> artifact = repository.findArtifact(checksum);

		// assert
		assertFalse(artifact.isPresent()); // artifact has not been found

	}

	@Test
	@Disabled("Test fails on some platforms")
	void test_findArtifact_io_exception_read() throws IOException {

		// prepare
		String checksum = CHECKSUM_UNKNOWN;
		File cacheFile = new File(cacheDir, "sha1/" + checksum + ".txt");
		FileUtils.writeStringToFile("org.test:test:1.0:jar", cacheFile);

		// assume
		assumeTrue(cacheFile.isFile());

		// lock the cache file
		try (RandomAccessFile raf = new RandomAccessFile(cacheFile, "rw")) {
			try (FileLock lock = raf.getChannel().lock()) {

				// test
				try {
					repository.findArtifact(checksum);

					fail("expected exception not thrown");
				} catch (RepositoryException e) {

					// assert
					assertEquals("I/O error", e.getMessage());
					Throwable cause = e.getCause();
					assertNotNull(cause);
					assertTrue(cause instanceof IOException);

				}

			}
		}

	}

	@Test
	void test_findArtifact_io_exception_write() {

		// prepare
		String checksum = CHECKSUM_ASM_70;
		File cacheFile = new File(cacheDir, "sha1/" + checksum + ".txt");
		cacheFile.mkdirs(); // create a directory at the place of the cache file

		// assume
		assumeTrue(cacheFile.isDirectory());

		// test
		try {
			repository.findArtifact(checksum);

			fail("expected exception not thrown");
		} catch (RepositoryException e) {

			// assert
			assertEquals("I/O error", e.getMessage());
			Throwable cause = e.getCause();
			assertNotNull(cause);
			assertTrue(cause instanceof IOException);

		}

	}

	@Test
	void findArtifact_withUnknownCoordinates() throws RepositoryException {

		// test
		Optional<Artifact> result = repository.findArtifact("org.unknown", "unknown", "0.99", "jar");

		// assert
		assertFalse(result.isPresent());

	}

	@Test
	void findArtifact_withKnownCoordinates() throws RepositoryException {

		// test
		Optional<Artifact> result = repository.findArtifact("org.ow2.asm", "asm", "7.0", "jar");

		// assert
		assertTrue(result.isPresent());

		Artifact artifact = result.get();
		assertEquals("org.ow2.asm", artifact.getGroupId());
		assertEquals("asm", artifact.getArtifactId());
		assertEquals("7.0", artifact.getVersion());
		assertEquals("jar", artifact.getType());

	}

	@Test
	void findArtifact_withCachedCoordinates() throws RepositoryException, IOException {

		// prepare
		File cacheFile = new File(cacheDir, "org/test/test/1.0/test-1.0.jar");
		FileUtils.writeStringToFile("--content-of-test-1.0.jar--", cacheFile);

		// assume
		assumeTrue(cacheFile.isFile());

		// test
		Optional<Artifact> result = repository.findArtifact("org.test", "test", "1.0", "jar");

		// assert
		assertTrue(result.isPresent());

		Artifact artifact = result.get();
		assertEquals("org.test", artifact.getGroupId());
		assertEquals("test", artifact.getArtifactId());
		assertEquals("1.0", artifact.getVersion());
		assertEquals("jar", artifact.getType());

	}

	@Test
	void downloadArtifact_withUnknownCoordinates() throws RepositoryException {

		// prepare
		Artifact artifact = new Artifact("org.unknown", "unknown", "0.99", "jar");

		// test
		Optional<InputStream> result = repository.downloadArtifact(artifact);

		// assert
		assertFalse(result.isPresent());

	}

	@Test
	void downloadArtifact_withKnownCoordinates() throws RepositoryException, IOException {

		// prepare
		Artifact artifact = new Artifact("org.ow2.asm", "asm", "7.0", "jar");
		parentRepository.addArtifactData(artifact, "--content-of-asm-7.0.jar--");

		// test
		Optional<InputStream> result = repository.downloadArtifact(artifact);

		// assert
		assertTrue(result.isPresent());
		String data = IOUtils.toString(result.get());
		assertEquals("--content-of-asm-7.0.jar--", data);

	}

	@Test
	void downloadArtifact_withCachedCoordinates() throws RepositoryException, IOException {

		// prepare
		Artifact artifact = new Artifact("org.test", "test", "1.0", "jar");
		File cacheFile = new File(cacheDir, "org/test/test/1.0/test-1.0.jar");
		FileUtils.writeStringToFile("--content-of-test-1.0.jar--", cacheFile);

		// test
		Optional<InputStream> result = repository.downloadArtifact(artifact);

		// assert
		assertTrue(result.isPresent());
		String data = IOUtils.toString(result.get());
		assertEquals("--content-of-test-1.0.jar--", data);

	}

}
