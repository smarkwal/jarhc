/*
 * Copyright 2025 Stephan Markwalder
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
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Base64;
import java.util.zip.ZipException;
import org.junit.jupiter.api.Test;

class CompressUtilsTest {

	@Test
	void test() {

		// prepare
		String text = "This is a test string.";

		// test
		String result = CompressUtils.compressString(text);

		// assert: Base64 encoded string
		assertTrue(result.matches("^[-A-Za-z0-9+/]*={0,3}$"));

		// test
		result = CompressUtils.decompressString(result);

		// assert
		assertEquals(text, result);
	}

	@Test
	void compressString_canCompressEmptyTest() {

		// test
		String result = CompressUtils.compressString("");

		// assert
		assertEquals("H4sIAAAAAAAAAAMAAAAAAAAAAAA=", result);
	}

	@Test
	void compressString_throwsIllegalArgumentException_forNullValue() {

		// test
		Exception result = assertThrows(IllegalArgumentException.class, () -> CompressUtils.compressString(null));

		// assert
		assertEquals("text", result.getMessage());
	}

	@Test
	void decompressString_throwsIllegalArgumentException_forNullValue() {

		// test
		Exception result = assertThrows(IllegalArgumentException.class, () -> CompressUtils.decompressString(null));

		// assert
		assertEquals("text", result.getMessage());
	}

	@Test
	void decompressString_throwsJarHcException_forUncompressedData() {

		// prepare: encoded but not compressed string
		String text = Base64.getEncoder().encodeToString("This is a test string.".getBytes());

		// test
		Exception result = assertThrows(JarHcException.class, () -> CompressUtils.decompressString(text));

		// assert
		Throwable cause = result.getCause();
		assertInstanceOf(ZipException.class, cause);
		assertEquals("Not in GZIP format", cause.getMessage());
	}

}
