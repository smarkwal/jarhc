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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.jarhc.test.AssertUtils;
import org.junit.jupiter.api.Test;

class ArrayUtilsTest {

	@Test
	void test_ArrayUtils() {
		AssertUtils.assertUtilityClass(ArrayUtils.class);
	}

	@Test
	void containsAny() {

		assertFalse(ArrayUtils.containsAny(new Object[]{}));
		assertFalse(ArrayUtils.containsAny(new Object[]{}, "test"));
		assertFalse(ArrayUtils.containsAny(new Object[]{}, "test1", "test2"));
		assertFalse(ArrayUtils.containsAny(new Object[]{"foo"}, "test"));
		assertFalse(ArrayUtils.containsAny(new Object[]{"foo", "bar"}, "test"));
		assertFalse(ArrayUtils.containsAny(new Object[]{"foo", "bar"}, "test1", "test2"));

		assertTrue(ArrayUtils.containsAny(new Object[]{"foo", "bar"}, "foo"));
		assertTrue(ArrayUtils.containsAny(new Object[]{"foo", "bar"}, "bar"));
		assertTrue(ArrayUtils.containsAny(new Object[]{"foo", "bar"}, "test", "foo"));
		assertTrue(ArrayUtils.containsAny(new Object[]{"foo", "bar"}, "bar", "foo"));

	}

}