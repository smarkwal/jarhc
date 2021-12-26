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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.jarhc.test.AssertUtils;
import org.junit.jupiter.api.Test;

class StringUtilsTest {

	@Test
	void test_StringUtils() {
		AssertUtils.assertUtilityClass(StringUtils.class);
	}

	@Test
	void repeat() {

		assertEquals("", StringUtils.repeat(null, 0));
		assertEquals("", StringUtils.repeat("", 0));
		assertEquals("", StringUtils.repeat(null, 5));
		assertEquals("", StringUtils.repeat("", 5));
		assertEquals("", StringUtils.repeat("X", 0));
		assertEquals("XXXXX", StringUtils.repeat("X", 5));

	}

	@Test
	void splitText() {

		// prepare
		String text = "Java 12 (8), Java 11 (8846), Java 9 (758), Java 8 (17423), Java 7 (6090), Java 6 (2580), Java 5 (5241), Java 1.4 (672), Java 1.3 (2359), Java 1.2 (523), Java 1.1 (13)";

		// test
		List<String> result = StringUtils.splitText(text, 60);

		// assert
		List<String> expected = Arrays.asList(
				"Java 12 (8), Java 11 (8846), Java 9 (758), Java 8 (17423),",
				"Java 7 (6090), Java 6 (2580), Java 5 (5241), Java 1.4 (672),",
				"Java 1.3 (2359), Java 1.2 (523), Java 1.1 (13)"
		);
		assertEquals(expected, result);

	}

	@Test
	void splitText_separatorNotFound() {

		// prepare
		String text = "This is a long sentence without commas.";

		// test
		List<String> result = StringUtils.splitText(text, 12);

		// assert
		List<String> expected = Collections.singletonList(text);
		assertEquals(expected, result);

	}

	@Test
	void splitText_shortSentence() {

		// prepare
		String text = "Hello World!";

		// test
		List<String> result = StringUtils.splitText(text, 60);

		// assert
		List<String> expected = Collections.singletonList(text);
		assertEquals(expected, result);

	}

	@Test
	void splitText_longSentences() {

		// prepare
		String text = "This is a long sentence, which contains only a few, but very well placed commas.";

		// test
		List<String> result = StringUtils.splitText(text, 12);

		// assert
		List<String> expected = Arrays.asList(
				"This is a long sentence,",
				"which contains only a few,",
				"but very well placed commas."
		);
		assertEquals(expected, result);

	}

	@Test
	void splitText_emptyText() {

		// prepare
		String text = "";

		// test
		List<String> result = StringUtils.splitText(text, 12);

		// assert
		List<String> expected = Collections.singletonList("");
		assertEquals(expected, result);

	}

}