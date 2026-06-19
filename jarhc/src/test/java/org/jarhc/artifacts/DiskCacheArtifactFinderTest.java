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

package org.jarhc.artifacts;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jarhc.test.log.LoggerAssertions.assertLogger;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import org.jarhc.test.log.LoggerBuilder;
import org.jarhc.utils.FileUtils;
import org.jarhc.utils.JarHcException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;
import org.slf4j.Logger;

class DiskCacheArtifactFinderTest {

	private final Logger logger = LoggerBuilder.collect(DiskCacheArtifactFinder.class);
	private final ArtifactFinder delegate = Mockito.mock(ArtifactFinder.class);
	private File cacheDir;
	private DiskCacheArtifactFinder artifactFinder;

	@BeforeEach
	void setUp(@TempDir Path tempDir) {
		cacheDir = tempDir.toFile();
		artifactFinder = new DiskCacheArtifactFinder(cacheDir, delegate, logger);
	}

	@Test
	void test_findArtifacts_withDiskCacheHit() throws RepositoryException, IOException {

		File cacheFile = new File(cacheDir, "1234.txt");
		FileUtils.writeStringToFile("commons-io:commons-io:2.6:jar", cacheFile);

		List<Artifact> result = artifactFinder.findArtifacts("1234");

		assertThat(result).containsExactly(new Artifact("commons-io", "commons-io", "2.6", "jar"));
		verify(delegate, times(0)).findArtifacts("1234");
		assertLogger(logger)
				.hasDebug("Artifact found: 1234 -> [commons-io:commons-io:2.6:jar] (disk cache)")
				.isEmpty();
	}

	@Test
	void test_findArtifacts_withDelegateHit_updatesDiskCache() throws RepositoryException, IOException {

		List<Artifact> artifacts = List.of(new Artifact("commons-codec", "commons-codec", "1.11", "jar"));
		doReturn(artifacts).when(delegate).findArtifacts("abcd");

		List<Artifact> result = artifactFinder.findArtifacts("abcd");

		assertThat(result).containsExactlyElementsOf(artifacts);
		verify(delegate).findArtifacts("abcd");
		assertThat(new File(cacheDir, "abcd.txt")).isFile();
		assertThat(FileUtils.readFileToString(new File(cacheDir, "abcd.txt"))).isEqualTo("commons-codec:commons-codec:1.11:jar");
		assertLogger(logger).isEmpty();
	}

	@Test
	void test_findArtifacts_withDelegateMiss_doesNotWriteDiskCache() throws RepositoryException {

		doReturn(List.of()).when(delegate).findArtifacts("ffff");

		List<Artifact> result = artifactFinder.findArtifacts("ffff");

		assertThat(result).isEmpty();
		verify(delegate).findArtifacts("ffff");
		assertThat(new File(cacheDir, "ffff.txt")).doesNotExist();
		assertLogger(logger).isEmpty();
	}

	@Test
	void test_findArtifacts_withMalformedDiskCacheContent() throws IOException {

		FileUtils.writeStringToFile("invalid/coordinates", new File(cacheDir, "1234.txt"));

		Exception result = assertThrows(JarHcException.class, () -> artifactFinder.findArtifacts("1234"));

		assertThat(result).hasMessage("Invalid artifact coordinates: invalid/coordinates");
	}

}
