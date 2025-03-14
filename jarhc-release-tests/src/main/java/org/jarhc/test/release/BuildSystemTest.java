
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

package org.jarhc.test.release;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.TimeZone;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class BuildSystemTest extends ReleaseTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(BuildSystemTest.class);

	@Test
	void dumpSystemProperties() {
		Properties properties = System.getProperties();
		List<String> names = new ArrayList<>(properties.stringPropertyNames());
		names.sort(String.CASE_INSENSITIVE_ORDER);
		LOGGER.info("Java System Properties:");
		for (String name : names) {
			String value = properties.getProperty(name);
			if (name.equals("line.separator")) {
				value = value.replace("\n", "\\n").replace("\r", "\\r"); // for better readability
			}
			if (name.contains("password") || name.contains("key") || name.contains("secret")) {
				value = "****************";
			}
			LOGGER.info("{} = '{}'", name, value);
		}
	}

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

	@Test
	void defaultTimezone() {
		TimeZone timeZone = TimeZone.getDefault();
		assertEquals("UTC", timeZone.getID());
	}

	@Test
	void lineSeparator() {
		String lineSeparator = System.lineSeparator();
		assertEquals("\n", lineSeparator);
	}

}
