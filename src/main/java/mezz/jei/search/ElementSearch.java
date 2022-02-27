package mezz.jei.search;

import mezz.jei.config.SearchMode;
import mezz.jei.ingredients.IListElementInfo;
import mezz.jei.ingredients.PrefixedSearchable;
import mezz.jei.search.suffixtree.GeneralizedSuffixTree;

import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

public class ElementSearch implements IElementSearch {
	private final Map<PrefixInfo, PrefixedSearchable> prefixedSearchables = new IdentityHashMap<>();
	private final CombinedSearchables<IListElementInfo<?>> combinedSearchables = new CombinedSearchables<>();

	public ElementSearch(PrefixInfos prefixInfos) {
		for (PrefixInfo prefixInfo : prefixInfos.allPrefixInfos()) {
			GeneralizedSuffixTree<IListElementInfo<?>> searchable = new GeneralizedSuffixTree<>();
			var prefixedSearchable = new PrefixedSearchable(searchable, prefixInfo);
			this.prefixedSearchables.put(prefixInfo, prefixedSearchable);
			this.combinedSearchables.addSearchable(prefixedSearchable);
		}
	}

	@Override
	public Set<IListElementInfo<?>> getSearchResults(String token, PrefixInfo prefixInfo) {
		if (token.isEmpty()) {
			return Set.of();
		}

		final ISearchable<IListElementInfo<?>> searchable = this.prefixedSearchables.get(prefixInfo);

		Set<IListElementInfo<?>> results = Collections.newSetFromMap(new IdentityHashMap<>());
		if (searchable != null && searchable.getMode() != SearchMode.DISABLED) {
			searchable.getSearchResults(token, results);
		} else {
			combinedSearchables.getSearchResults(token, results);
		}
		return results;
	}

	@Override
	public void add(IListElementInfo<?> info) {
		for (PrefixedSearchable prefixedSearchable : this.prefixedSearchables.values()) {
			SearchMode searchMode = prefixedSearchable.getMode();
			if (searchMode != SearchMode.DISABLED) {
				Collection<String> strings = prefixedSearchable.getStrings(info);
				GeneralizedSuffixTree<IListElementInfo<?>> searchable = prefixedSearchable.getSearchable();
				for (String string : strings) {
					searchable.put(string, info);
				}
			}
		}
	}

	@Override
	public Set<IListElementInfo<?>> getAllIngredients() {
		Set<IListElementInfo<?>> results = Collections.newSetFromMap(new IdentityHashMap<>());
		this.prefixedSearchables.get(PrefixInfo.NO_PREFIX).getAllElements(results);
		return results;
	}
}
