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

package org.jarhc.artifacts;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jarhc.test.log.LoggerAssertions.assertLogger;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;

import java.io.File;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.util.Map;
import org.jarhc.test.log.LoggerBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;
import org.slf4j.Logger;

class MavenArtifactFinderTest {

	private final Logger logger = LoggerBuilder.collect(MavenArtifactFinder.class);
	private File cacheDir;
	private final MavenArtifactFinder.Settings settings = Mockito.mock(MavenArtifactFinder.Settings.class);
	private final MavenArtifactFinder artifactFinder = new MavenArtifactFinder(cacheDir, logger, settings);

	@BeforeEach
	void setUp(@TempDir Path tempDir) {
		cacheDir = tempDir.toFile();

		doReturn("http://localhost/checksum=%s").when(settings).getUrl();
		doReturn(10).when(settings).getTimeout();
		doReturn(Map.of()).when(settings).getHeaders();
	}

	@Test
	void test_findArtifact_withInvalidURL() {

		// prepare
		doReturn("htp://localhost/checksum=%s").when(settings).getUrl();

		// test
		Exception result = assertThrows(RepositoryException.class, () -> artifactFinder.findArtifacts("1234"));

		// assert
		assertThat(result)
				.hasMessage("Malformed URL for checksum: 1234")
				.hasCause(new MalformedURLException("unknown protocol: htp"));
		assertLogger(logger).isEmpty();
	}

	@Test
	void test_findArtifact_withUnknownURL() {

		// prepare
		doReturn("http://unknown/checksum=%s").when(settings).getUrl();

		// test
		Exception result = assertThrows(RepositoryException.class, () -> artifactFinder.findArtifacts("1234"));

		// assert
		assertThat(result)
				.hasMessage("Unexpected I/O error for URL: http://unknown/checksum=1234")
				.hasCause(new UnknownHostException("unknown"));
		assertLogger(logger)
				.hasWarn("Problem with search.maven.org. Visit https://status.maven.org to check the status of Maven Central.")
				.isEmpty();

		// test
		result = assertThrows(RepositoryException.class, () -> artifactFinder.findArtifacts("1234"));

		// assert
		assertThat(result)
				.hasMessage("Unexpected I/O error for URL: http://unknown/checksum=1234")
				.hasCause(new UnknownHostException("unknown"));
		assertLogger(logger)
				// no additional warning
				.isEmpty();
	}

}