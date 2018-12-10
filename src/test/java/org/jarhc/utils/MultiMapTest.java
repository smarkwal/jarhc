/*
 * Copyright 2018 Stephan Markwalder
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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class MultiMapTest {

	@Test
	void test_empty() {

		// test
		MultiMap<String, Integer> map = new MultiMap<>();

		// assert
		assertEquals(0, map.getSize());
		assertEquals(0, map.getKeys().size());
		assertNull(map.getValues("a"));

		// test
		MultiMap<Integer, String> result = map.invert();

		// assert
		assertEquals(0, result.getSize());
		assertEquals(0, result.getKeys().size());
		assertNull(result.getValues(1));

	}

	@Test
	void test_add() {

		// prepare
		MultiMap<String, Integer> map = new MultiMap<>();

		// test
		map.add("a", 1);
		map.add("a", 2);
		map.add("a", 3);
		map.add("b", 4);
		map.add("b", 5);
		map.add("c", 6);
		map.add("c", 6);

		// assert
		assertEquals(3, map.getSize());
		assertEquals(3, map.getKeys().size());
		assertEquals(3, map.getValues("a").size());
		assertEquals(2, map.getValues("b").size());
		assertEquals(1, map.getValues("c").size());
		assertNull(map.getValues("d"));

	}

	@Test
	void test_invert() {

		// prepare
		MultiMap<String, Integer> map = new MultiMap<>();
		map.add("a", 1);
		map.add("a", 2);
		map.add("a", 3);
		map.add("b", 1);
		map.add("b", 2);
		map.add("c", 3);
		map.add("c", 1);

		// test
		MultiMap<Integer, String> result = map.invert();

		// assert
		assertEquals(3, result.getSize());
		assertEquals(3, result.getKeys().size());
		assertEquals(3, result.getValues(1).size());
		assertEquals(2, result.getValues(2).size());
		assertEquals(2, result.getValues(3).size());
		assertNull(result.getValues(4));

	}

}
