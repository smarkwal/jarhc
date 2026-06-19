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
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;
import org.jarhc.test.log.LoggerBuilder;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;

class MemoryCacheArtifactFinderTest {

	private final Logger logger = LoggerBuilder.collect(MemoryCacheArtifactFinder.class);
	private final ArtifactFinder delegate = Mockito.mock(ArtifactFinder.class);
	private final MemoryCacheArtifactFinder artifactFinder = new MemoryCacheArtifactFinder(delegate, logger);

	@Test
	void test_findArtifacts_withMemoryCacheHit() throws RepositoryException {

		ArrayList<Artifact> delegatedArtifacts = new ArrayList<>();
		delegatedArtifacts.add(new Artifact("org.ow2.asm", "asm", "7.0", "jar"));
		doReturn(delegatedArtifacts).when(delegate).findArtifacts("1234");

		List<Artifact> firstResult = artifactFinder.findArtifacts("1234");
		firstResult.add(new Artifact("unexpected", "artifact", "1.0", "jar"));
		List<Artifact> secondResult = artifactFinder.findArtifacts("1234");

		assertThat(secondResult).containsExactly(new Artifact("org.ow2.asm", "asm", "7.0", "jar"));
		verify(delegate, times(1)).findArtifacts("1234");
		assertLogger(logger)
				.hasDebug("Artifact found: 1234 -> [org.ow2.asm:asm:7.0:jar] (memory cache)")
				.isEmpty();
	}

	@Test
	void test_findArtifacts_withDelegateMiss_updatesMemoryCache() throws RepositoryException {

		doReturn(List.of()).when(delegate).findArtifacts("ffff");

		List<Artifact> firstResult = artifactFinder.findArtifacts("ffff");
		List<Artifact> secondResult = artifactFinder.findArtifacts("ffff");

		assertThat(firstResult).isEmpty();
		assertThat(secondResult).isEmpty();
		verify(delegate, times(1)).findArtifacts("ffff");
		assertLogger(logger)
				.hasDebug("Artifact found: ffff -> [] (memory cache)")
				.isEmpty();
	}

}
