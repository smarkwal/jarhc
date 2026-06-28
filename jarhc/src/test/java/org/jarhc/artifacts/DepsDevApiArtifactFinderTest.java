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

import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.Map;
import org.jarhc.test.log.LoggerBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;

class DepsDevApiArtifactFinderTest {

	private final Logger logger = LoggerBuilder.collect(DepsDevApiArtifactFinder.class);
	private final DepsDevApiSettings settings = Mockito.mock(DepsDevApiSettings.class);
	private final DepsDevApiArtifactFinder artifactFinder = new DepsDevApiArtifactFinder(logger, settings);

	@BeforeEach
	void setUp() {
		doReturn("http://localhost").when(settings).getBaseUrl();
		doReturn(10).when(settings).getTimeout();
		doReturn(Map.of()).when(settings).getHeaders();
	}

	@Test
	void test_findArtifact_withInvalidChecksum() {

		// test
		assertThrows(IllegalArgumentException.class, () -> artifactFinder.findArtifacts("this is NOT a checksum!"));

		// assert
		assertLogger(logger).isEmpty();
	}

	@Test
	void test_findArtifact_withInvalidURL() {

		// prepare
		doReturn("htp://localhost").when(settings).getBaseUrl();

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
		doReturn("http://unknown").when(settings).getBaseUrl();

		// test
		Exception result = assertThrows(RepositoryException.class, () -> artifactFinder.findArtifacts("1234"));

		// assert
		assertThat(result)
				.hasMessageStartingWith("Unexpected I/O error for URL: http://unknown/query?")
				.hasCause(new UnknownHostException("unknown"));
		assertLogger(logger)
				.hasWarn("Problem with api.deps.dev. Visit https://deps.dev to check the status of the deps.dev API.")
				.isEmpty();

		// test
		result = assertThrows(RepositoryException.class, () -> artifactFinder.findArtifacts("1234"));

		// assert
		assertThat(result)
				.hasMessageStartingWith("Unexpected I/O error for URL: http://unknown/query?")
				.hasCause(new UnknownHostException("unknown"));
		assertLogger(logger)
				// no additional warning
				.isEmpty();
	}

}
