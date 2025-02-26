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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class FileNameNormalizerTest {

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

}
