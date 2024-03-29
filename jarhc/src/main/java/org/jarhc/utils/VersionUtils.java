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

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VersionUtils {

	private VersionUtils() {
		throw new IllegalStateException("utility class");
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(VersionUtils.class);

	private static final Properties properties = new Properties();

	static {
		ClassLoader classLoader = VersionUtils.class.getClassLoader();
		try {
			try (InputStream stream = classLoader.getResourceAsStream("jarhc.properties")) {
				if (stream != null) {
					properties.load(stream);
				}
			}
		} catch (IOException e) {
			LOGGER.warn("Failed to load version properties from resource.", e);
		}

		// check if version is overridden
		// (used in release tests to generate reproducible output)
		String version = System.getProperty("jarhc.version.override");
		if (version != null) {
			properties.setProperty("version", version);
		}
	}

	public static String getVersion() {
		return properties.getProperty("version");
	}

}
