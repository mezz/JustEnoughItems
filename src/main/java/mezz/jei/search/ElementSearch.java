package mezz.jei.search;

import com.google.common.collect.ImmutableSet;
import mezz.jei.config.SearchMode;
import mezz.jei.ingredients.IIngredientListElementInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

public class ElementSearch implements IElementSearch {
	private static final Logger LOGGER = LogManager.getLogger();

	private final Map<PrefixInfo<IIngredientListElementInfo<?>>, PrefixedSearchable<IIngredientListElementInfo<?>>> prefixedSearchables = new IdentityHashMap<>();
	private final CombinedSearchables<IIngredientListElementInfo<?>> combinedSearchables = new CombinedSearchables<>();

	public ElementSearch(ElementPrefixParser elementPrefixParser) {
		for (PrefixInfo<IIngredientListElementInfo<?>> prefixInfo : elementPrefixParser.allPrefixInfos()) {
			ISearchStorage<IIngredientListElementInfo<?>> storage = prefixInfo.createStorage();
			PrefixedSearchable<IIngredientListElementInfo<?>> prefixedSearchable = new PrefixedSearchable<>(storage, prefixInfo);
			this.prefixedSearchables.put(prefixInfo, prefixedSearchable);
			this.combinedSearchables.addSearchable(prefixedSearchable);
		}
	}

	@Override
	public Set<IIngredientListElementInfo<?>> getSearchResults(ElementPrefixParser.TokenInfo tokenInfo) {
		String token = tokenInfo.token();
		if (token.isEmpty()) {
			return ImmutableSet.of();
		}

		Set<IIngredientListElementInfo<?>> results = Collections.newSetFromMap(new IdentityHashMap<>());

		PrefixInfo<IIngredientListElementInfo<?>> prefixInfo = tokenInfo.prefixInfo();
		if (prefixInfo == ElementPrefixParser.NO_PREFIX) {
			combinedSearchables.getSearchResults(token, results);
			return results;
		}
		final ISearchable<IIngredientListElementInfo<?>> searchable = this.prefixedSearchables.get(prefixInfo);
		if (searchable == null || searchable.getMode() == SearchMode.DISABLED) {
			combinedSearchables.getSearchResults(token, results);
			return results;
		}
		searchable.getSearchResults(token, results);
		return results;
	}

	@Override
	public void add(IIngredientListElementInfo<?> info) {
		for (PrefixedSearchable<IIngredientListElementInfo<?>> prefixedSearchable : this.prefixedSearchables.values()) {
			SearchMode searchMode = prefixedSearchable.getMode();
			if (searchMode != SearchMode.DISABLED) {
				Collection<String> strings = prefixedSearchable.getStrings(info);
				ISearchStorage<IIngredientListElementInfo<?>> searchable = prefixedSearchable.getSearchStorage();
				for (String string : strings) {
					searchable.put(string, info);
				}
			}
		}
	}

	@Override
	public void addAll(Collection<IIngredientListElementInfo<?>> infos) {
		for (PrefixedSearchable<IIngredientListElementInfo<?>> prefixedSearchable : this.prefixedSearchables.values()) {
			SearchMode searchMode = prefixedSearchable.getMode();
			if (searchMode != SearchMode.DISABLED) {
				for (IIngredientListElementInfo<?> info : infos) {
					Collection<String> strings = prefixedSearchable.getStrings(info);
					ISearchStorage<IIngredientListElementInfo<?>> searchable = prefixedSearchable.getSearchStorage();
					for (String string : strings) {
						searchable.put(string, info);
					}
				}
			}
		}
	}

	@Override
	public Set<IIngredientListElementInfo<?>> getAllIngredients() {
		Set<IIngredientListElementInfo<?>> results = Collections.newSetFromMap(new IdentityHashMap<>());
		this.prefixedSearchables.get(ElementPrefixParser.NO_PREFIX).getAllElements(results);
		return results;
	}

	@Override
	public void logStatistics() {
		this.prefixedSearchables.forEach((prefixInfo, value) -> {
			if (prefixInfo.getMode() != SearchMode.DISABLED) {
				ISearchStorage<IIngredientListElementInfo<?>> storage = value.getSearchStorage();
				LOGGER.info("ElementSearch {} Storage Stats: {}", prefixInfo, storage.statistics());
			}
		});
	}
}
