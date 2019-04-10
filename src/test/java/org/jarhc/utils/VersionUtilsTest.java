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

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

class VersionUtilsTest {

	@Test
	void test_getVersion() {

		// test
		String version = VersionUtils.getVersion();

		// assert
		assertNotNull(version);
		assumeFalse(version.equals("${project.version}"), () -> "jarhc.properties not filtered.");
		assertEquals("1.0", version);

	}

	@Test
	void test_getDate() {

		// test
		String date = VersionUtils.getDate();

		// assert
		assertNotNull(date);
		assumeFalse(date.equals("${timestamp}"), () -> "jarhc.properties not filtered.");
		assertTrue(date.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}Z"));

	}

}
