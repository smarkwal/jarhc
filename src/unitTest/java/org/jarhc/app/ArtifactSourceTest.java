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

package org.jarhc.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import org.jarhc.artifacts.Artifact;
import org.jarhc.artifacts.Repository;
import org.jarhc.artifacts.RepositoryException;
import org.jarhc.utils.IOUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class ArtifactSourceTest {

	@Mock
	Repository repository;

	private ArtifactSource artifactSource;

	private AutoCloseable mocks;

	@BeforeEach
	void setUp() throws RepositoryException {

		mocks = MockitoAnnotations.openMocks(this);

		// setup repository mock
		when(repository.downloadArtifact(any(Artifact.class))).thenAnswer(
				(invocation) -> {
					Artifact artifact = invocation.getArgument(0);

					// simulate "artifact not found"
					if (artifact.getArtifactId().equals("unknown")) {
						return Optional.empty();
					}

					// simulate repository error
					if (artifact.getArtifactId().equals("error")) {
						throw new RepositoryException("internal error");
					}

					String fileName = artifact.getFileName();
					byte[] data = ("content-of-" + fileName).getBytes(StandardCharsets.UTF_8);
					InputStream stream = new ByteArrayInputStream(data);
					return Optional.of(stream);
				}
		);

		artifactSource = new ArtifactSource("org.test:test:1.0", repository);
	}

	@AfterEach
	void tearDown() throws Exception {
		mocks.close();
	}

	@Test
	void getCoordinates() {

		// test
		String result = artifactSource.getCoordinates();

		// assert
		assertEquals("org.test:test:1.0", result);

	}

	@Test
	void getName() {

		// test
		String result = artifactSource.getName();

		// assert
		assertEquals("test-1.0.jar", result);

	}

	@Test
	void getData() throws IOException {

		// test
		InputStream result = artifactSource.getInputStream();

		// assert
		String text = new String(IOUtils.toByteArray(result), StandardCharsets.UTF_8);
		assertEquals("content-of-test-1.0.jar", text);

	}

	@Test
	void getData_throwsIOException_ifArtifactIsNotFound() {

		// prepare
		artifactSource = new ArtifactSource("unknown:unknown:0.0.1", repository);

		// test & assert
		assertThrows(IOException.class, () -> artifactSource.getInputStream());

	}

	@Test
	void getData_throwsIOException_onRepositoryException() {

		// prepare
		artifactSource = new ArtifactSource("error:error:0.0.1", repository);

		// test & assert
		assertThrows(IOException.class, () -> artifactSource.getInputStream());

	}

}