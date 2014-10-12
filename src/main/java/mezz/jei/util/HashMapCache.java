package mezz.jei.util;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * An ordered hash map with a limited capacity.
 * When an element is added beyond the capacity, the oldest entry is removed.
 */
public class HashMapCache<K, V> extends LinkedHashMap<K, V> {
	private final int capacity;

	public HashMapCache(int capacity) {
		super(capacity);
		this.capacity = capacity;
	}

	@Override
	protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
		return size() > capacity;
	}
}
