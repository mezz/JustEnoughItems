package mezz.jei.common.search;

import mezz.jei.api.search.ISearchStorage;
import mezz.jei.common.collect.SetMultiMap;

import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import java.util.function.Consumer;

/**
 * This is more memory-efficient than storing each value directly in an {@link ISearchStorage}
 * when many values share each key.
 *
 * It stores a map of keys to a set of values.
 * The set values are shared with the internal backing search storage to index and find them.
 * The set's values are modified directly when values with the same key are added.
 */
public class LimitedStringStorage<T> implements ISearchStorage<T> {
	private final SetMultiMap<String, T> multiMap;
	private final ISearchStorage<Set<T>> backingStorage;

	public LimitedStringStorage(ISearchStorage<Set<T>> searchStorage) {
		this.backingStorage = searchStorage;
		this.multiMap = new SetMultiMap<>(() -> Collections.newSetFromMap(new IdentityHashMap<>()));
	}

	public LimitedStringStorage(ISearchStorage<Set<T>> searchStorage, SetMultiMap<String, T> multiMap) {
		this.backingStorage = searchStorage;
		this.multiMap = multiMap;
	}

	@Override
	public void getSearchResults(String token, Consumer<Collection<T>> resultsConsumer) {
		backingStorage.getSearchResults(token, resultSet -> {
			for (Collection<T> result : resultSet) {
				resultsConsumer.accept(result);
			}
		});
	}

	@Override
	public void getAllElements(Consumer<Collection<T>> resultsConsumer) {
		Collection<T> values = multiMap.allValues();
		resultsConsumer.accept(values);
	}

	@Override
	public void put(String key, T value) {
		boolean isNewKey = !multiMap.containsKey(key);
		multiMap.put(key, value);
		if (isNewKey) {
			Set<T> set = multiMap.get(key);
			backingStorage.put(key, set);
		}
	}

	@Override
	public String statistics() {
		return "LimitedStringStorage: " + backingStorage.statistics();
	}
}
