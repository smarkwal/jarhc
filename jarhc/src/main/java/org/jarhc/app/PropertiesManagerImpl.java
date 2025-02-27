/*
 * Copyright 2025 Stephan Markwalder
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

package org.jarhc.app;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import org.jarhc.utils.JarHcException;

public class PropertiesManagerImpl implements PropertiesManager {

	@Override
	public Properties loadProperties() {

		Properties properties = new Properties();

		// try to load properties from user home directory
		String userHome = System.getProperty("user.home");
		if (userHome != null) {
			Path path = Path.of(userHome, ".jarhc", "jarhc.properties");
			loadProperties(path, properties);
		}

		// try to load properties from current directory
		Path path = Path.of("jarhc.properties");
		loadProperties(path, properties);

		return properties;
	}

	/**
	 * Load properties from a file.
	 * If the file does not exist, no properties are loaded.
	 *
	 * @param path       Path to properties file.
	 * @param properties Properties object to load properties into.
	 */
	private static void loadProperties(Path path, Properties properties) {
		if (path == null) throw new IllegalArgumentException("path");
		if (properties == null) throw new IllegalArgumentException("properties");
		if (Files.isRegularFile(path)) {
			try (InputStream stream = new FileInputStream(path.toFile())) {
				properties.load(stream);
			} catch (IOException e) {
				throw new JarHcException("Error loading properties file: " + path, e);
			}
		}
	}

}
