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

import java.util.List;

public interface CollectionManager {

	/**
	 * Check if a collection with the given name exists.
	 *
	 * @param name Collection name.
	 * @return {@code true} if a collection with the given name exists, {@code false} otherwise.
	 */
	default boolean isCollection(String name) {
		List<String> collection = getCollection(name);
		return collection != null;
	}

	/**
	 * Get a collection by name.
	 *
	 * @param name Collection name.
	 * @return List of artifact coordinates, or {@code null} if the collection does not exist.
	 */
	List<String> getCollection(String name);

}
