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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import org.jarhc.utils.IOUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class MavenLocalRepositoryIT {

	private static final String GROUP_ID = "org.ow2.asm";
	private static final String ARTIFACT_ID = "asm";
	private static final String VERSION = "7.0";
	private static final String TYPE = "jar";

	private static File directory;

	@BeforeAll
	static void setUp() {
		String userHome = System.getProperty("user.home");
		directory = new File(userHome, ".m2/repository");
		assumeTrue(directory.isDirectory(), "Local Maven repository not found: " + directory.getAbsolutePath());
	}

	@Test
	void test_findArtifact() throws RepositoryException {

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
	void test_downloadArtifact() throws RepositoryException, IOException {

		// prepare
		MavenLocalRepository repository = new MavenLocalRepository(directory, null);

		// test
		Artifact artifact = new Artifact(GROUP_ID, ARTIFACT_ID, VERSION, TYPE);
		Optional<InputStream> result = repository.downloadArtifact(artifact);

		try {

			// assert
			assertTrue(result.isPresent());

			InputStream stream = result.get();
			assertTrue(stream instanceof FileInputStream);

			byte[] data = IOUtils.toByteArray(stream);
			assertEquals(113676, data.length);

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
