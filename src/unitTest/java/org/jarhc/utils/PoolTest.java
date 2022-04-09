/*
 * Copyright 2022 Stephan Markwalder
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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import org.junit.jupiter.api.Test;

class PoolTest {

	@Test
	void doBorrow() {

		// prepare
		Pool<HashSet<String>> pool = new Pool<>(HashSet::new, HashSet::clear);

		// test
		HashSet<String> set1 = pool.doBorrow();
		HashSet<String> set2 = pool.doBorrow();
		HashSet<String> set3 = pool.doBorrow();

		// assert
		assertNotNull(set1);
		assertNotNull(set2);
		assertNotNull(set3);
		assertNotSame(set2, set1);
		assertNotSame(set3, set1);
		assertNotSame(set3, set2);

	}

	@Test
	void doReturn() {

		// prepare
		Pool<HashSet<String>> pool = new Pool<>(HashSet::new, HashSet::clear);

		// test
		HashSet<String> set1 = pool.doBorrow();
		pool.doReturn(set1);
		HashSet<String> set2 = pool.doBorrow();
		pool.doReturn(set2);
		HashSet<String> set3 = pool.doBorrow();
		pool.doReturn(set3);

		// assert
		assertNotNull(set1);
		assertNotNull(set2);
		assertNotNull(set3);
		assertSame(set2, set1);
		assertSame(set3, set1);
		assertSame(set3, set2);

	}

	@Test
	void onReturn() {

		// prepare
		Pool<HashSet<String>> pool = new Pool<>(HashSet::new, HashSet::clear);

		// prepare
		HashSet<String> set = pool.doBorrow();
		set.add("Hello");
		set.add("World");

		// test
		pool.doReturn(set);

		// assert
		assertTrue(set.isEmpty());

	}

}