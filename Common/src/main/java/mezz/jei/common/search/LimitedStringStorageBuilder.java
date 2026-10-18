package mezz.jei.common.search;

import mezz.jei.api.search.ISearchStorage;
import mezz.jei.api.search.ISearchStorageBuilder;
import mezz.jei.api.search.ISearchStorageBuilderFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

public class LimitedStringStorageBuilder<T> implements ISearchStorageBuilder<T> {
	private final Map<String, Set<T>> valuesByKey = new HashMap<>();
	private final ISearchStorageBuilder<Set<T>> storageBuilder;

	public LimitedStringStorageBuilder(ISearchStorageBuilderFactory factory) {
		this.storageBuilder = factory.create();
	}

	public LimitedStringStorageBuilder(ISearchStorageBuilderFactory factory, String id) {
		this.storageBuilder = factory.create(id);
	}

	@Override
	public void put(String key, T value) {
		boolean isNewKey = !valuesByKey.containsKey(key);
		Set<T> set = valuesByKey.computeIfAbsent(key, k -> Collections.newSetFromMap(new IdentityHashMap<>()));
		set.add(value);
		if (isNewKey) {
			storageBuilder.put(key, set);
		}
	}

	@Override
	public ISearchStorage<T> build() {
		ISearchStorage<Set<T>> searchStorage = this.storageBuilder.build();
		return new LimitedStringStorage<>(searchStorage, valuesByKey);
	}
}
