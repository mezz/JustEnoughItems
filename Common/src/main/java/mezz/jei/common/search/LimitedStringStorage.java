package mezz.jei.common.search;

import mezz.jei.api.search.ISearchStorage;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
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
	private final Map<String, Set<T>> valuesByKey;
	private final ISearchStorage<Set<T>> backingStorage;

	public LimitedStringStorage(ISearchStorage<Set<T>> searchStorage) {
		this(searchStorage, new HashMap<>());
	}

	public LimitedStringStorage(ISearchStorage<Set<T>> searchStorage, Map<String, Set<T>> valuesByKey) {
		this.backingStorage = searchStorage;
		this.valuesByKey = valuesByKey;
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
		backingStorage.getAllElements(resultSet -> {
			for (Collection<T> result : resultSet) {
				resultsConsumer.accept(result);
			}
		});
	}

	@Override
	public void put(String key, T value) {
		boolean isNewKey = !valuesByKey.containsKey(key);
		Set<T> set = valuesByKey.computeIfAbsent(key, k -> Collections.newSetFromMap(new IdentityHashMap<>()));
		set.add(value);
		if (isNewKey) {
			backingStorage.put(key, set);
		}
	}

	@Override
	public boolean supportsDeletion() {
		return backingStorage.supportsDeletion();
	}

	@Override
	public boolean removeAll(Collection<T> values) {
		Set<T> removed = Collections.newSetFromMap(new IdentityHashMap<>());
		removed.addAll(values);
		List<Set<T>> emptiedSets = valuesByKey.values().stream()
			.filter(set -> set.size() <= removed.size() && removed.containsAll(set))
			.toList();
		if (!backingStorage.removeAll(emptiedSets)) {
			return false;
		}
		valuesByKey.values().removeIf(set -> {
			// Iterate the smaller collection so small updates do not scan every member of shared keys.
			if (removed.size() < set.size()) {
				removed.forEach(set::remove);
			} else {
				set.removeIf(removed::contains);
			}
			return set.isEmpty();
		});
		return true;
	}

	@Override
	public String statistics() {
		return "LimitedStringStorage: " + backingStorage.statistics();
	}
}
