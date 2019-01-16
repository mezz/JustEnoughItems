package mezz.jei.collect;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableMultimap;

public class MultiMap<K, V, T extends Collection<V>> {
	protected final Map<K, T> map;
	private final Function<K, T> collectionMappingFunction;

	public MultiMap(Supplier<T> collectionSupplier) {
		this(new HashMap<>(), collectionSupplier);
	}

	public MultiMap(Map<K, T> map, Supplier<T> collectionSupplier) {
		this.map = map;
		this.collectionMappingFunction = (k -> collectionSupplier.get());
	}

	public T get(K key) {
		return map.computeIfAbsent(key, collectionMappingFunction);
	}

	public boolean put(K key, V value) {
		return get(key).add(value);
	}

	public boolean remove(K key, V value) {
		T collection = map.get(key);
		return collection != null && collection.remove(value);
	}

	public boolean containsKey(K key) {
		return map.containsKey(key);
	}

	public boolean contains(K key, V value) {
		T collection = map.get(key);
		return collection != null && collection.contains(value);
	}

	public Set<Map.Entry<K, T>> entrySet() {
		return map.entrySet();
	}

	public int getTotalSize() {
		int size = 0;
		for (T value : map.values()) {
			size += value.size();
		}
		return size;
	}

	public ImmutableMultimap<K, V> toImmutable() {
		ImmutableMultimap.Builder<K, V> builder = ImmutableMultimap.builder();
		for (Map.Entry<K, T> entry : map.entrySet()) {
			K key = entry.getKey();
			for (V value : entry.getValue()) {
				builder.put(key, value);
			}
		}
		return builder.build();
	}
}
