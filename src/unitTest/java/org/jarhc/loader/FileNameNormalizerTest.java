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

package org.jarhc.loader;

import static org.jarhc.test.log.LoggerAssertions.assertLogger;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import org.jarhc.app.Options;
import org.jarhc.artifacts.Artifact;
import org.jarhc.artifacts.Repository;
import org.jarhc.artifacts.RepositoryException;
import org.jarhc.test.log.LoggerBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;

class FileNameNormalizerTest {

	private final Repository repository = Mockito.mock(Repository.class);
	private final Logger logger = LoggerBuilder.collect(FileNameNormalizer.class);

	@BeforeEach
	void setUp() throws RepositoryException {
		Mockito.when(repository.findArtifact("c1")).thenReturn(Optional.empty());
		Mockito.when(repository.findArtifact("c2")).thenReturn(Optional.of(new Artifact("g", "a", "1.2", "jar")));
		Mockito.when(repository.findArtifact("c3")).thenThrow(new RepositoryException("test"));
	}

	@AfterEach
	void tearDown() {
		assertLogger(logger).isEmpty();
	}

	@Test
	void test_getFileNameWithoutVersionNumber() {
		test_getFileNameWithoutVersionNumber("a.jar", "a.jar");
		test_getFileNameWithoutVersionNumber("a-1.jar", "a.jar");
		test_getFileNameWithoutVersionNumber("a-1.2.jar", "a.jar");
		test_getFileNameWithoutVersionNumber("a-1.2.3.jar", "a.jar");
		test_getFileNameWithoutVersionNumber("a-1.22.333.jar", "a.jar");
		test_getFileNameWithoutVersionNumber("a-b-c-1.0.jar", "a-b-c.jar");
		test_getFileNameWithoutVersionNumber("a-b-c-1.0-SNAPSHOT.jar", "a-b-c.jar");
		test_getFileNameWithoutVersionNumber("a-b-c-1.0-test.jar", "a-b-c-test.jar");

		test_getFileNameWithoutVersionNumber("java.base.jmod", "java.base.jmod");
	}

	private void test_getFileNameWithoutVersionNumber(String input, String expectedOutput) {
		String output = FileNameNormalizer.getFileNameWithoutVersionNumber(input);
		assertEquals(expectedOutput, output, input);
	}

	@Test
	void getFileName_keepVersion() {

		// prepare
		Options options = new Options();
		options.setUseArtifactName(true);
		options.setRemoveVersion(false);
		FileNameNormalizer fileNameNormalizer = new FileNameNormalizer(options, repository, logger);

		String result = fileNameNormalizer.getFileName("aa-12.jar", "c1");
		assertEquals("aa-12.jar", result);
		assertLogger(logger).isEmpty();

		result = fileNameNormalizer.getFileName("aa-12.jar", "c2");
		assertEquals("a-1.2.jar", result);
		assertLogger(logger).isEmpty();

		result = fileNameNormalizer.getFileName("aa-12.jar", "c3");
		assertEquals("aa-12.jar", result);
		assertLogger(logger).hasWarn("Failed to find artifact in repository.", new RepositoryException("test"));

		result = fileNameNormalizer.getFileName("java.base.jmod", "c1");
		assertEquals("java.base.jmod", result);
		assertLogger(logger).isEmpty();

	}

	@Test
	void getFileName_removeVersion() {

		// prepare
		Options options = new Options();
		options.setUseArtifactName(true);
		options.setRemoveVersion(true);
		FileNameNormalizer fileNameNormalizer = new FileNameNormalizer(options, repository, logger);

		String result = fileNameNormalizer.getFileName("aa-12.jar", "c1");
		assertEquals("aa.jar", result);
		assertLogger(logger).isEmpty();

		result = fileNameNormalizer.getFileName("aa-12.jar", "c2");
		assertEquals("a.jar", result);
		assertLogger(logger).isEmpty();

		result = fileNameNormalizer.getFileName("aa-12.jar", "c3");
		assertEquals("aa.jar", result);
		assertLogger(logger).hasWarn("Failed to find artifact in repository.", new RepositoryException("test"));

	}

}
