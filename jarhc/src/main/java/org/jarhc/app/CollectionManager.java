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
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.jarhc.utils.JarHcException;
import org.jarhc.utils.ResourceUtils;

public class CollectionManager {

	// placeholder cache entry for negative result
	private static final List<String> NONE = List.of();

	// cache for collections
	private final Map<String, List<String>> cache = new ConcurrentHashMap<>();

	/**
	 * Check if a collection with the given name exists.
	 *
	 * @param name Collection name.
	 * @return {@code true} if a collection with the given name exists, {@code false} otherwise.
	 */
	public boolean isCollection(String name) {
		List<String> collection = getCollection(name);
		return collection != null;
	}

	/**
	 * Get a collection by name.
	 *
	 * @param name Collection name.
	 * @return List of artifact coordinates, or {@code null} if the collection does not exist.
	 */
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

	private static List<String> findCollection(String name) {

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
