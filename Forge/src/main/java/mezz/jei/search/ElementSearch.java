package mezz.jei.search;

import mezz.jei.config.SearchMode;
import mezz.jei.ingredients.IListElementInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

public class ElementSearch implements IElementSearch {
	private static final Logger LOGGER = LogManager.getLogger();

	private final Map<PrefixInfo, PrefixedSearchable> prefixedSearchables = new IdentityHashMap<>();
	private final CombinedSearchables<IListElementInfo<?>> combinedSearchables = new CombinedSearchables<>();

	public ElementSearch(PrefixInfos prefixInfos) {
		for (PrefixInfo prefixInfo : prefixInfos.allPrefixInfos()) {
			ISearchStorage<IListElementInfo<?>> storage = prefixInfo.createStorage();
			var prefixedSearchable = new PrefixedSearchable(storage, prefixInfo);
			this.prefixedSearchables.put(prefixInfo, prefixedSearchable);
			this.combinedSearchables.addSearchable(prefixedSearchable);
		}
	}

	@Override
	public Set<IListElementInfo<?>> getSearchResults(PrefixInfos.TokenInfo tokenInfo) {
		String token = tokenInfo.token();
		if (token.isEmpty()) {
			return Set.of();
		}

		Set<IListElementInfo<?>> results = Collections.newSetFromMap(new IdentityHashMap<>());

		PrefixInfo prefixInfo = tokenInfo.prefixInfo();
		if (prefixInfo == PrefixInfo.NO_PREFIX) {
			combinedSearchables.getSearchResults(token, results);
			return results;
		}
		final ISearchable<IListElementInfo<?>> searchable = this.prefixedSearchables.get(prefixInfo);
		if (searchable == null || searchable.getMode() == SearchMode.DISABLED) {
			combinedSearchables.getSearchResults(token, results);
			return results;
		}
		searchable.getSearchResults(token, results);
		return results;
	}

	@Override
	public void add(IListElementInfo<?> info) {
		for (PrefixedSearchable prefixedSearchable : this.prefixedSearchables.values()) {
			SearchMode searchMode = prefixedSearchable.getMode();
			if (searchMode != SearchMode.DISABLED) {
				Collection<String> strings = prefixedSearchable.getStrings(info);
				ISearchStorage<IListElementInfo<?>> searchable = prefixedSearchable.getSearchStorage();
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

	@Override
	public void logStatistics() {
		for (Map.Entry<PrefixInfo, PrefixedSearchable> e : this.prefixedSearchables.entrySet()) {
			PrefixInfo prefixInfo = e.getKey();
			if (prefixInfo.getMode() != SearchMode.DISABLED) {
				ISearchStorage<IListElementInfo<?>> storage = e.getValue().getSearchStorage();
				LOGGER.info("ElementSearch {} Storage Stats: {}", prefixInfo, storage.statistics());
			}
		}
	}
}
