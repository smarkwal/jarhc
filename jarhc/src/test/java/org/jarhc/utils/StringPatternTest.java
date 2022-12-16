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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class StringPatternTest {

	@Test
	void matches() {

		StringPattern pattern = new StringPattern("test", false);
		assertTrue(pattern.matches("test"));
		assertFalse(pattern.matches("abc test"));
		assertFalse(pattern.matches("test xyz"));
		assertFalse(pattern.matches("abc test xyz"));

		pattern = new StringPattern("test *", false);
		assertFalse(pattern.matches("test"));
		assertFalse(pattern.matches("abc test"));
		assertTrue(pattern.matches("test xyz"));
		assertFalse(pattern.matches("abc test xyz"));

		pattern = new StringPattern("* test", false);
		assertFalse(pattern.matches("test"));
		assertTrue(pattern.matches("abc test"));
		assertFalse(pattern.matches("test xyz"));
		assertFalse(pattern.matches("abc test xyz"));

		pattern = new StringPattern("* test *", false);
		assertFalse(pattern.matches("test"));
		assertFalse(pattern.matches("abc test"));
		assertFalse(pattern.matches("test xyz"));
		assertTrue(pattern.matches("abc test xyz"));

		pattern = new StringPattern("te*st", false);
		assertTrue(pattern.matches("test"));
		assertFalse(pattern.matches("abc test"));
		assertFalse(pattern.matches("test xyz"));
		assertFalse(pattern.matches("abc test xyz"));
		assertTrue(pattern.matches("test test"));

		pattern = new StringPattern("test", true);
		assertTrue(pattern.matches("test"));
		assertTrue(pattern.matches("TEST"));

		pattern = new StringPattern("TEST", true);
		assertTrue(pattern.matches("test"));
		assertTrue(pattern.matches("TEST"));

	}

}