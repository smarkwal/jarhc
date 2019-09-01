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

package org.jarhc.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.jarhc.test.AssertUtils;
import org.junit.jupiter.api.Test;

class DigestUtilsTest {

	@Test
	void test_DigestUtils() {
		AssertUtils.assertUtilityClass(DigestUtils.class);
	}

	@Test
	void test_sha1Hex_ByteArray() {

		// prepare
		byte[] input = "Hello World".getBytes(StandardCharsets.UTF_8);

		// test
		String result = DigestUtils.sha1Hex(input);

		// assert
		assertEquals("0a4d55a8d778e5022fab701977c5d840bbc486d0", result);

	}

	@Test
	void test_sha1Hex_String() {

		// prepare
		String input = "Hello World";

		// test
		String result = DigestUtils.sha1Hex(input);

		// assert
		assertEquals("0a4d55a8d778e5022fab701977c5d840bbc486d0", result);

	}

	@Test
	void test_sha1Hex_InputStream() throws IOException {

		// prepare
		InputStream input = new ByteArrayInputStream("Hello World".getBytes(StandardCharsets.UTF_8));

		// test
		String result = DigestUtils.sha1Hex(input);

		// assert
		assertEquals("0a4d55a8d778e5022fab701977c5d840bbc486d0", result);

	}

	@Test
	void test_sha1Hex_unknownAlgorithm() {

		// remember original SHA-1 algorithm
		String originalAlgorithm = DigestUtils.getAlgorithm();
		try {

			// prepare: override SHA-1 algorithm
			DigestUtils.setAlgorithm("Unknown");

			// test & assert
			assertThrows(RuntimeException.class, () -> DigestUtils.sha1Hex("test"));

		} finally {
			// remember original SHA-1 algorithm
			DigestUtils.setAlgorithm(originalAlgorithm);
		}

	}

}
