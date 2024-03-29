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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import org.jarhc.test.AssertUtils;
import org.junit.jupiter.api.Test;

class VersionUtilsTest {

	@Test
	void test_VersionUtils() {
		AssertUtils.assertUtilityClass(VersionUtils.class);
	}

	@Test
	void test_getVersion() {

		// test
		String version = VersionUtils.getVersion();

		// assert
		assertNotNull(version);
		assumeFalse(version.equals("${version}"), () -> "jarhc.properties not filtered.");
		assertEquals("0.0.1", version);

	}

}
