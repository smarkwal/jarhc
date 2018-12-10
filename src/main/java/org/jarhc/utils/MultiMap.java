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

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class MultiMap<K, V> {

	private final Map<K, Set<V>> map = new TreeMap<>();

	public void add(K key, V value) {
		Set<V> set = map.computeIfAbsent(key, k -> new TreeSet<V>());
		set.add(value);
	}

	public int getSize() {
		return map.size();
	}

	public Set<K> getKeys() {
		return map.keySet();
	}

	public Set<V> getValues(K key) {
		return map.get(key);
	}

	public MultiMap<V, K> invert() {
		MultiMap<V, K> result = new MultiMap<>();
		for (Map.Entry<K, Set<V>> entry : map.entrySet()) {
			K key = entry.getKey();
			for (V value : entry.getValue()) {
				result.add(value, key);
			}
		}
		return result;
	}

}
