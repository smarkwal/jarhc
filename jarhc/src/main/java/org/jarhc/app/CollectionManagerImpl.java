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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.jarhc.utils.JarHcException;
import org.jarhc.utils.ResourceUtils;

public class CollectionManagerImpl implements CollectionManager {

	// placeholder cache entry for negative result
	private static final List<String> NONE = List.of();

	// cache for collections
	private final Map<String, List<String>> cache = new ConcurrentHashMap<>();

	private final Properties properties;

	public CollectionManagerImpl(Properties properties) {
		if (properties == null) throw new IllegalArgumentException("properties");
		this.properties = properties;
	}

	@Override
	public List<String> getCollection(String name) {

		// check if collection has already been cached
		if (cache.containsKey(name)) {
			List<String> collection = cache.get(name);
			if (collection == NONE) {
				return null;
			}
			return collection;
		}

		// try to find collection
		List<String> collection = findCollection(name);
		if (collection == null) {
			// cache negative result
			cache.put(name, NONE);
			return null;
		}

		// cache collection
		cache.put(name, collection);
		return collection;
	}

	private List<String> findCollection(String name) {

		// try to find collection in properties
		String propertyName = "collection." + name;
		if (properties.containsKey(propertyName)) {
			String collection = properties.getProperty(propertyName, "");
			List<String> values = List.of(collection.split(","));
			return filter(values);
		}

		// try to find collection file in user home directory
		String userHome = System.getProperty("user.home");
		if (userHome != null) {
			Path path = Paths.get(userHome, ".jarhc", "collections", name + ".txt");
			if (Files.isRegularFile(path)) {
				try {
					List<String> lines = Files.readAllLines(path);
					return filter(lines);
				} catch (IOException e) {
					throw new JarHcException("Failed to read collection file: " + path, e);
				}
			}
		}

		// try to find collection file in classpath
		try {
			List<String> lines = ResourceUtils.getResourceAsLines("/collections/" + name + ".txt", "UTF-8");
			return filter(lines);
		} catch (IOException e) {
			// collection not found
			return null;
		}
	}

	private static List<String> filter(List<String> lines) {
		return lines.stream()
				.filter(line -> !line.isEmpty()) // skip empty lines
				.filter(line -> !line.startsWith("#")) // skip comments
				.collect(Collectors.toList());
	}

}
