
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

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
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
			LOGGER.info("{} = {}", name, value);
		}
	}

}
