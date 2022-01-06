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

package org.jarhc.test.release.utils;

import java.util.Properties;

public class TestUtils {

	/**
	 * Checks if the system property "jarhc.test.resources.generate" is set.
	 * This property is used as flag to instruct tests to re-generate their test resources.
	 *
	 * @return <code>true</code> if test resources should be generated.
	 */
	public static boolean createResources() {
		Properties properties = System.getProperties();
		return properties.containsKey("jarhc.test.resources.generate");
	}

	public static String normalizeResult(String result) {
		return result.replaceAll("\\| [0-9a-f]{40} \\|", "| 0000000000000000000000000000000000000000 |");
	}

}
