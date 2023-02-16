package mezz.jei.gui.search;

import mezz.jei.api.search.ILanguageTransformer;
import mezz.jei.core.search.CombinedSearchables;
import mezz.jei.core.search.ISearchStorage;
import mezz.jei.core.search.ISearchable;
import mezz.jei.core.search.PrefixInfo;
import mezz.jei.core.search.PrefixedSearchable;
import mezz.jei.core.search.SearchMode;
import mezz.jei.gui.ingredients.IListElementInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ElementSearch implements IElementSearch {
	private static final Logger LOGGER = LogManager.getLogger();

	private final Map<PrefixInfo<IListElementInfo<?>>, PrefixedSearchable<IListElementInfo<?>>> prefixedSearchables = new IdentityHashMap<>();
	private final CombinedSearchables<IListElementInfo<?>> combinedSearchables = new CombinedSearchables<>();
	@Unmodifiable
	private final List<ILanguageTransformer> languageTransformers;

	public ElementSearch(ElementPrefixParser elementPrefixParser, Collection<ILanguageTransformer> languageTransformers) {
		this.languageTransformers = List.copyOf(languageTransformers);
		for (PrefixInfo<IListElementInfo<?>> prefixInfo : elementPrefixParser.allPrefixInfos()) {
			ISearchStorage<IListElementInfo<?>> storage = prefixInfo.createStorage();
			var prefixedSearchable = new PrefixedSearchable<>(storage, prefixInfo);
			this.prefixedSearchables.put(prefixInfo, prefixedSearchable);
			this.combinedSearchables.addSearchable(prefixedSearchable);
		}
	}

	@Override
	public Set<IListElementInfo<?>> getSearchResults(ElementPrefixParser.TokenInfo tokenInfo) {
		String token = transform(tokenInfo.token());
		if (token.isEmpty()) {
			return Set.of();
		}

		Set<IListElementInfo<?>> results = Collections.newSetFromMap(new IdentityHashMap<>());

		PrefixInfo<IListElementInfo<?>> prefixInfo = tokenInfo.prefixInfo();
		if (prefixInfo == ElementPrefixParser.NO_PREFIX) {
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
		for (PrefixedSearchable<IListElementInfo<?>> prefixedSearchable : this.prefixedSearchables.values()) {
			SearchMode searchMode = prefixedSearchable.getMode();
			if (searchMode != SearchMode.DISABLED) {
				Collection<String> strings = prefixedSearchable.getStrings(info);
				ISearchStorage<IListElementInfo<?>> searchable = prefixedSearchable.getSearchStorage();
				for (String string : strings) {
					string = transform(string);
					searchable.put(string, info);
				}
			}
		}
	}

	@Override
	public Set<IListElementInfo<?>> getAllIngredients() {
		Set<IListElementInfo<?>> results = Collections.newSetFromMap(new IdentityHashMap<>());
		this.prefixedSearchables.get(ElementPrefixParser.NO_PREFIX).getAllElements(results);
		return results;
	}

	@Override
	public void logStatistics() {
		this.prefixedSearchables.forEach((prefixInfo, value) -> {
			if (prefixInfo.getMode() != SearchMode.DISABLED) {
				ISearchStorage<IListElementInfo<?>> storage = value.getSearchStorage();
				LOGGER.info("ElementSearch {} Storage Stats: {}", prefixInfo, storage.statistics());
			}
		});
	}

	private String transform(String string) {
		for (ILanguageTransformer languageTransformer : languageTransformers) {
			string = languageTransformer.transformToken(string);
		}
		return string;
	}
}
