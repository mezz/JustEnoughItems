package mezz.jei.core.collect;

import com.google.common.collect.ImmutableSetMultimap;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class SetMultiMap<K, V> extends MultiMap<K, V, Set<V>> {
	public SetMultiMap() {
		this(HashSet::new);
	}

	public SetMultiMap(Supplier<Set<V>> collectionSupplier) {
		super(collectionSupplier);
	}

	public SetMultiMap(Map<K, Set<V>> map, Supplier<Set<V>> collectionSupplier) {
		super(map, collectionSupplier);
	}

	@Override
	public Set<V> get(K key) {
		Set<V> collection = map.get(key);
		if (collection != null) {
			return Collections.unmodifiableSet(collection);
		}
		return Collections.emptySet();
	}

	@Override
	public ImmutableSetMultimap<K, V> toImmutable() {
		ImmutableSetMultimap.Builder<K, V> builder = ImmutableSetMultimap.builder();
		map.forEach(builder::putAll);
		return builder.build();
	}
}
