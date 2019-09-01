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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.junit.jupiter.api.Test;

class IOUtilsTest {

	@Test
	void test_toByteArray() throws IOException {

		// prepare
		ByteArrayInputStream stream = new ByteArrayInputStream("Hello World".getBytes());

		// test
		byte[] result = IOUtils.toByteArray(stream);

		// assert
		assertEquals("Hello World", new String(result));

	}

	@Test
	void test_toString() throws IOException {

		// prepare
		ByteArrayInputStream stream = new ByteArrayInputStream("Hello World".getBytes());

		// test
		String result = IOUtils.toString(stream);

		// assert
		assertEquals("Hello World", result);

	}

}
