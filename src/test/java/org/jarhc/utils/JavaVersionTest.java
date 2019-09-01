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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class JavaVersionTest {

	@Test
	void test_fromClassVersion() {

		assertEquals("Java 1.1", JavaVersion.fromClassVersion(45));
		assertEquals("Java 1.2", JavaVersion.fromClassVersion(46));
		assertEquals("Java 1.3", JavaVersion.fromClassVersion(47));
		assertEquals("Java 1.4", JavaVersion.fromClassVersion(48));
		assertEquals("Java 5", JavaVersion.fromClassVersion(49));
		assertEquals("Java 6", JavaVersion.fromClassVersion(50));
		assertEquals("Java 7", JavaVersion.fromClassVersion(51));
		assertEquals("Java 8", JavaVersion.fromClassVersion(52));
		assertEquals("Java 9", JavaVersion.fromClassVersion(53));
		assertEquals("Java 10", JavaVersion.fromClassVersion(54));
		assertEquals("Java 11", JavaVersion.fromClassVersion(55));
		assertEquals("Java 12", JavaVersion.fromClassVersion(56));

		assertEquals("[unknown:20]", JavaVersion.fromClassVersion(20));

	}

}
