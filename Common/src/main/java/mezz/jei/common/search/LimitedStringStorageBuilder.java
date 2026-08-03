package mezz.jei.common.search;

import mezz.jei.api.search.ISearchStorage;
import mezz.jei.api.search.ISearchStorageBuilder;
import mezz.jei.api.search.ISearchStorageBuilderFactory;
import mezz.jei.common.collect.SetMultiMap;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

public class LimitedStringStorageBuilder<T> implements ISearchStorageBuilder<T> {
	private final SetMultiMap<String, T> multiMap = new SetMultiMap<>(() -> Collections.newSetFromMap(new IdentityHashMap<>()));
	private final ISearchStorageBuilder<Set<T>> storageBuilder;

	public LimitedStringStorageBuilder(ISearchStorageBuilderFactory factory) {
		this.storageBuilder = factory.create();
	}

	public LimitedStringStorageBuilder(ISearchStorageBuilderFactory factory, String id) {
		this.storageBuilder = factory.create(id);
	}

	@Override
	public void put(String key, T value) {
		boolean isNewKey = !multiMap.containsKey(key);
		multiMap.put(key, value);
		if (isNewKey) {
			Set<T> set = multiMap.get(key);
			storageBuilder.put(key, set);
		}
	}

	@Override
	public ISearchStorage<T> build() {
		ISearchStorage<Set<T>> searchStorage = this.storageBuilder.build();
		return new LimitedStringStorage<>(searchStorage, multiMap);
	}
}
