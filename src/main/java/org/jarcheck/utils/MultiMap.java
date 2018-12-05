package org.jarcheck.utils;

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
