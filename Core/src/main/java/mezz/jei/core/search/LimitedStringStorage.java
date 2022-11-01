package mezz.jei.core.search;

import mezz.jei.core.collect.SetMultiMap;
import mezz.jei.core.search.suffixtree.GeneralizedSuffixTree;

import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

/**
 * This is more memory-efficient than {@link GeneralizedSuffixTree}
 * when there are many values for each key.
 *
 * It stores a map of keys to a set of values.
 * The set values are shared with the internal {@link GeneralizedSuffixTree} to index and find them.
 * The sets values are modified directly when values with the same key are added.
 */
public class LimitedStringStorage<T> implements ISearchStorage<T> {
	private final SetMultiMap<String, T> multiMap = new SetMultiMap<>(() -> Collections.newSetFromMap(new IdentityHashMap<>()));
	private final GeneralizedSuffixTree<Set<T>> generalizedSuffixTree = new GeneralizedSuffixTree<>();

	@Override
	public void getSearchResults(String token, Set<T> results) {
		Set<Set<T>> intermediateResults = Collections.newSetFromMap(new IdentityHashMap<>());
		generalizedSuffixTree.getSearchResults(token, intermediateResults);
		for (Set<T> set : intermediateResults) {
			results.addAll(set);
		}
	}

	@Override
	public void getAllElements(Set<T> results) {
		Collection<T> values = multiMap.allValues();
		results.addAll(values);
	}

	@Override
	public void put(String key, T value) {
		boolean isNewKey = !multiMap.containsKey(key);
		multiMap.put(key, value);
		if (isNewKey) {
			Set<T> set = multiMap.get(key);
			generalizedSuffixTree.put(key, set);
		}
	}

	@Override
	public String statistics() {
		return "LimitedStringStorage: " + generalizedSuffixTree.statistics();
	}
}
