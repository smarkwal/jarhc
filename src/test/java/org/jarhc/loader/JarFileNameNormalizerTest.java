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

import org.jarhc.artifacts.Artifact;
import org.jarhc.artifacts.Resolver;
import org.jarhc.artifacts.ResolverException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JarFileNameNormalizerTest {

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
	}

	private void test_getFileNameWithoutVersionNumber(String input, String expectedOutput) {
		String output = JarFileNameNormalizer.getFileNameWithoutVersionNumber(input);
		assertEquals(expectedOutput, output, input);
	}

	@Test
	void test_getArtifactFileName() throws ResolverException {

		Resolver resolver = Mockito.mock(Resolver.class);
		Mockito.when(resolver.getArtifact("c1")).thenReturn(Optional.empty());
		Mockito.when(resolver.getArtifact("c2")).thenReturn(Optional.of(new Artifact("g", "a", "1.2", "jar")));
		Mockito.when(resolver.getArtifact("c3")).thenThrow(new ResolverException("test"));

		String result = JarFileNameNormalizer.getArtifactFileName("c1", resolver, false, "aa-12.jar");
		assertEquals("aa-12.jar", result);

		result = JarFileNameNormalizer.getArtifactFileName("c1", resolver, true, "aa-12.jar");
		assertEquals("aa.jar", result);

		result = JarFileNameNormalizer.getArtifactFileName("c2", resolver, false, "aa-12.jar");
		assertEquals("a-1.2.jar", result);

		result = JarFileNameNormalizer.getArtifactFileName("c2", resolver, true, "aa-12.jar");
		assertEquals("a.jar", result);

		result = JarFileNameNormalizer.getArtifactFileName("c3", resolver, false, "aa-12.jar");
		assertEquals("aa-12.jar", result);

		result = JarFileNameNormalizer.getArtifactFileName("c3", resolver, true, "aa-12.jar");
		assertEquals("aa.jar", result);

	}
}
