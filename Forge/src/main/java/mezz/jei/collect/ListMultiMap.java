package mezz.jei.collect;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableListMultimap;

public class ListMultiMap<K, V> extends MultiMap<K, V, List<V>> {
	public ListMultiMap() {
		this(ArrayList::new);
	}

	public ListMultiMap(Supplier<List<V>> collectionSupplier) {
		super(collectionSupplier);
	}

	public ListMultiMap(Map<K, List<V>> map, Supplier<List<V>> collectionSupplier) {
		super(map, collectionSupplier);
	}

	@Override
	public ImmutableListMultimap<K, V> toImmutable() {
		ImmutableListMultimap.Builder<K, V> builder = ImmutableListMultimap.builder();
		map.forEach(builder::putAll);
		return builder.build();
	}
}
