package mezz.jei.gui.search;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.config.DebugConfig;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ElementSearch implements IElementSearch {
	private static final Logger LOGGER = LogManager.getLogger();

	private final Map<PrefixInfo<IListElementInfo<?>, IListElement<?>>, PrefixedSearchable<IListElementInfo<?>, IListElement<?>>> prefixedSearchables = new IdentityHashMap<>();
	private final CombinedSearchables<IListElement<?>> combinedSearchables = new CombinedSearchables<>();
	// Use ConcurrentHashMap for thread-safe access during async operations
	private final Map<Object, IListElement<?>> allElements = new ConcurrentHashMap<>();

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
	public <T> void add(IListElementInfo<T> info, IIngredientManager ingredientManager) {
		IListElement<T> element = info.getElement();
		Object uid = getUid(element.getTypedIngredient(), ingredientManager);
		this.allElements.put(uid, element);
		for (PrefixedSearchable<IListElementInfo<?>, IListElement<?>> prefixedSearchable : this.prefixedSearchables.values()) {
			SearchMode searchMode = prefixedSearchable.getMode();
			if (searchMode != SearchMode.DISABLED) {
				Collection<String> strings = prefixedSearchable.getStrings(info);
				ISearchStorage<IListElement<?>> storage = prefixedSearchable.getSearchStorage();
				for (String string : strings) {
					storage.put(string, element);
				}
			}
		}
	}

	private static <T> Object getUid(ITypedIngredient<T> typedIngredient, IIngredientManager ingredientManager) {
		IIngredientHelper<T> ingredientHelper = ingredientManager.getIngredientHelper(typedIngredient.getType());
		return ingredientHelper.getUniqueId(typedIngredient.getIngredient(), UidContext.Ingredient);
	}

	@Override
	public void addAll(Collection<IListElementInfo<?>> infos, IIngredientManager ingredientManager) {
		// Use parallel processing for large ingredient lists
		if (DebugConfig.isParallelSearchEnabled() && infos.size() >= 100) {
			addAllParallel(infos, ingredientManager);
		} else {
			addAllSequential(infos, ingredientManager);
		}
	}

	/**
	 * Sequential addAll for small lists (less overhead).
	 */
	private void addAllSequential(Collection<IListElementInfo<?>> infos, IIngredientManager ingredientManager) {
		// First pass: populate allElements map
		for (IListElementInfo<?> info : infos) {
			IListElement<?> element = info.getElement();
			Object uid = getUid(info.getTypedIngredient(), ingredientManager);
			this.allElements.put(uid, element);
		}

		// Second pass: populate search indexes
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

	/**
	 * Parallel addAll for large lists (better performance with many ingredients).
	 */
	private void addAllParallel(Collection<IListElementInfo<?>> infos, IIngredientManager ingredientManager) {
		LOGGER.info("Adding {} ingredients using parallel processing", infos.size());

		// First pass: populate allElements map (thread-safe with ConcurrentHashMap)
		infos.parallelStream()
			.forEach(info -> {
				IListElement<?> element = info.getElement();
				Object uid = getUid(info.getTypedIngredient(), ingredientManager);
				this.allElements.put(uid, element);
			});

		// Second pass: populate search indexes in parallel per prefix
		List<PrefixedSearchable<IListElementInfo<?>, IListElement<?>>> activeSearchables =
			this.prefixedSearchables.values().stream()
				.filter(p -> p.getMode() != SearchMode.DISABLED)
				.toList();

		activeSearchables.parallelStream()
			.forEach(prefixedSearchable -> {
				ISearchStorage<IListElement<?>> storage = prefixedSearchable.getSearchStorage();
				for (IListElementInfo<?> info : infos) {
					Collection<String> strings = prefixedSearchable.getStrings(info);
					for (String string : strings) {
						storage.put(string, info.getElement());
					}
				}
			});

		LOGGER.info("Parallel ingredient addition complete");
	}

	@Override
	public <T> Optional<IListElement<T>> findElement(ITypedIngredient<T> ingredient, IIngredientHelper<T> ingredientHelper) {
		Object ingredientUid = ingredientHelper.getUniqueId(ingredient.getIngredient(), UidContext.Ingredient);

		IListElement<?> listElement = allElements.get(ingredientUid);
		if (listElement != null && listElement.getTypedIngredient().getType().equals(ingredient.getType())) {
			@SuppressWarnings("unchecked")
			IListElement<T> cast = (IListElement<T>) listElement;
			return Optional.of(cast);
		}

		return Optional.empty();
	}

	@Override
	public Collection<IListElement<?>> getAllIngredients() {
		return Collections.unmodifiableCollection(allElements.values());
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
