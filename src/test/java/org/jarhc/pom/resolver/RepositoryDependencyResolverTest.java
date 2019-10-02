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

package org.jarhc.pom.resolver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import org.jarhc.artifacts.Artifact;
import org.jarhc.artifacts.Repository;
import org.jarhc.artifacts.RepositoryException;
import org.jarhc.pom.Dependency;
import org.jarhc.pom.PomUtils;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class RepositoryDependencyResolverTest {

	private final Repository repository = mock(Repository.class);
	private final RepositoryDependencyResolver resolver = new RepositoryDependencyResolver(repository);

	@Test
	void getDependencies() throws ResolverException, RepositoryException {

		// prepare
		Artifact artifact = new Artifact("group", "deps", "1.0", "jar");
		when(repository.downloadArtifact(any(Artifact.class))).thenReturn(generatePom(artifact, 3));

		// test
		List<Dependency> result = resolver.getDependencies(artifact);

		// assert
		assertEquals(3, result.size());

	}

	@Test
	void getDependencies_returnsEmptyList_ifPomContainsNoDependencies() throws ResolverException, RepositoryException {

		// prepare
		Artifact artifact = new Artifact("group", "no-deps", "1.0", "jar");
		when(repository.downloadArtifact(any(Artifact.class))).thenReturn(generatePom(artifact, 0));

		// test
		List<Dependency> result = resolver.getDependencies(artifact);

		// assert
		assertEquals(0, result.size());

	}

	@Test
	void getDependencies_throwsPomNotFoundException_forUnknownArtifact() throws RepositoryException {

		// prepare
		Artifact artifact = new Artifact("group", "unknown", "1.0", "jar");
		when(repository.downloadArtifact(any(Artifact.class))).thenReturn(Optional.empty());

		// test
		assertThrows(POMNotFoundException.class, () -> resolver.getDependencies(artifact));

	}

	@Test
	void getDependencies_throwsResolverException_onRepositoryException() throws RepositoryException {

		// prepare
		Artifact artifact = new Artifact("group", "error", "1.0", "jar");
		when(repository.downloadArtifact(any(Artifact.class))).thenThrow(new RepositoryException("test"));

		// test
		assertThrows(ResolverException.class, () -> resolver.getDependencies(artifact));

	}

	@Test
	void getDependencies_throwsResolverException_forIllegalPom() throws RepositoryException {

		// prepare
		Artifact artifact = new Artifact("group", "illegal", "1.0", "jar");

		// return an incomplete POM XML document
		Optional<InputStream> stream = Optional.of(new ByteArrayInputStream("<?xml version=\"1.0\" encoding=\"UTF-8\"?><project>".getBytes(StandardCharsets.UTF_8)));
		when(repository.downloadArtifact(any(Artifact.class))).thenReturn(stream);

		// test
		assertThrows(ResolverException.class, () -> resolver.getDependencies(artifact));

	}

	@Test
	void getDependencies_returnsCachedDependencies() throws ResolverException, RepositoryException {

		// prepare
		Artifact artifact = new Artifact("group", "cached", "1.0", "jar");
		when(repository.downloadArtifact(any(Artifact.class))).thenReturn(generatePom(artifact, 3));

		// test: first call
		List<Dependency> result1 = resolver.getDependencies(artifact);

		// verify: repository has been called once
		verify(repository, times(1)).downloadArtifact(any(Artifact.class));

		// assert
		assertEquals(3, result1.size());

		// reset
		Mockito.reset(repository);

		// test: second call
		List<Dependency> result2 = resolver.getDependencies(artifact);

		// verify: repository has not been called anymore
		verifyZeroInteractions(repository);

		// assert
		assertEquals(result1, result2);

	}

	@Test
	void getDependencies_cachesNegativeResults() throws RepositoryException {

		// prepare
		Artifact artifact = new Artifact("group", "cached", "1.0", "jar");
		when(repository.downloadArtifact(any(Artifact.class))).thenReturn(Optional.empty());

		// test: first call
		assertThrows(POMNotFoundException.class, () -> resolver.getDependencies(artifact));

		// verify: repository has been called once
		verify(repository, times(1)).downloadArtifact(any(Artifact.class));

		// reset
		Mockito.reset(repository);

		// test: second call
		assertThrows(POMNotFoundException.class, () -> resolver.getDependencies(artifact));

		// verify: repository has not been called anymore
		verifyZeroInteractions(repository);

	}

	private Optional<InputStream> generatePom(Artifact artifact, int dependencies) {
		String xml = PomUtils.generatePomXml(artifact, dependencies);
		ByteArrayInputStream stream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
		return Optional.of(stream);
	}

}