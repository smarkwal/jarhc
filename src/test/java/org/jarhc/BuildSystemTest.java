/*
 * Copyright 2021 Stephan Markwalder
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

package org.jarhc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.Charset;
import java.util.Locale;
import org.junit.jupiter.api.Test;

/**
 * Tests for build system configuration and capabilities.
 */
class BuildSystemTest {

	@Test
	void defaultLocal() {
		Locale locale = Locale.getDefault();
		String code = locale.toString();
		// "en" and "en_US" are both OK
		assertTrue(code.equals("en") || code.equals("en_US"), code);
	}

	@Test
	void defaultCharset() {
		Charset charset = Charset.defaultCharset();
		assertEquals("UTF-8", charset.name());
	}

}
