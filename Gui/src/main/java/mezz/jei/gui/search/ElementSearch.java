package mezz.jei.gui.search;

import mezz.jei.core.search.CombinedSearchables;
import mezz.jei.core.search.ISearchStorage;
import mezz.jei.core.search.ISearchable;
import mezz.jei.core.search.PrefixInfo;
import mezz.jei.core.search.PrefixedSearchable;
import mezz.jei.core.search.SearchMode;
import mezz.jei.gui.ingredients.IListElement;
import mezz.jei.gui.ingredients.IListElementInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

public class ElementSearch implements IElementSearch {
	private static final Logger LOGGER = LogManager.getLogger();

	private final Map<PrefixInfo<IListElementInfo<?>, IListElement<?>>, PrefixedSearchable<IListElementInfo<?>, IListElement<?>>> prefixedSearchables = new IdentityHashMap<>();
	private final CombinedSearchables<IListElement<?>> combinedSearchables = new CombinedSearchables<>();
	private final Set<IListElement<?>> allElements = Collections.newSetFromMap(new IdentityHashMap<>());

	public ElementSearch(ElementPrefixParser elementPrefixParser) {
		for (PrefixInfo<IListElementInfo<?>, IListElement<?>> prefixInfo : elementPrefixParser.allPrefixInfos()) {
			ISearchStorage<IListElement<?>> storage = prefixInfo.createStorage();
			var prefixedSearchable = new PrefixedSearchable<>(storage, prefixInfo);
			this.prefixedSearchables.put(prefixInfo, prefixedSearchable);
			this.combinedSearchables.addSearchable(prefixedSearchable);
		}
	}

	@Override
	public Set<IListElement<?>> getSearchResults(ElementPrefixParser.TokenInfo tokenInfo) {
		String token = tokenInfo.token();
		if (token.isEmpty()) {
			return Set.of();
		}

		Set<IListElement<?>> results = Collections.newSetFromMap(new IdentityHashMap<>());

		PrefixInfo<IListElementInfo<?>, IListElement<?>> prefixInfo = tokenInfo.prefixInfo();
		if (prefixInfo == ElementPrefixParser.NO_PREFIX) {
			combinedSearchables.getSearchResults(token, results::addAll);
			return results;
		}
		final ISearchable<IListElement<?>> searchable = this.prefixedSearchables.get(prefixInfo);
		if (searchable == null || searchable.getMode() == SearchMode.DISABLED) {
			combinedSearchables.getSearchResults(token, results::addAll);
			return results;
		}
		searchable.getSearchResults(token, results::addAll);
		return results;
	}

	@Override
	public void add(IListElementInfo<?> info) {
		this.allElements.add(info.getElement());
		for (PrefixedSearchable<IListElementInfo<?>, IListElement<?>> prefixedSearchable : this.prefixedSearchables.values()) {
			SearchMode searchMode = prefixedSearchable.getMode();
			if (searchMode != SearchMode.DISABLED) {
				Collection<String> strings = prefixedSearchable.getStrings(info);
				ISearchStorage<IListElement<?>> storage = prefixedSearchable.getSearchStorage();
				for (String string : strings) {
					storage.put(string, info.getElement());
				}
			}
		}
	}

	@Override
	public void addAll(Collection<IListElementInfo<?>> infos) {
		for (IListElementInfo<?> info : infos) {
			this.allElements.add(info.getElement());
		}
		for (PrefixedSearchable<IListElementInfo<?>, IListElement<?>> prefixedSearchable : this.prefixedSearchables.values()) {
			SearchMode searchMode = prefixedSearchable.getMode();
			if (searchMode != SearchMode.DISABLED) {
				ISearchStorage<IListElement<?>> storage = prefixedSearchable.getSearchStorage();
				for (IListElementInfo<?> info : infos) {
					Collection<String> strings = prefixedSearchable.getStrings(info);
					for (String string : strings) {
						storage.put(string, info.getElement());
					}
				}
			}
		}
	}

	@Override
	public Set<IListElement<?>> getAllIngredients() {
		return Collections.unmodifiableSet(allElements);
	}

	@Override
	public void logStatistics() {
		this.prefixedSearchables.forEach((prefixInfo, value) -> {
			if (prefixInfo.getMode() != SearchMode.DISABLED) {
				ISearchStorage<IListElement<?>> storage = value.getSearchStorage();
				LOGGER.info("ElementSearch {} Storage Stats: {}", prefixInfo, storage.statistics());
			}
		});
	}
}
