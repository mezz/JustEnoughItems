package mezz.jei.gui.ingredients;

import mezz.jei.api.helpers.IColorHelper;
import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IIngredientVisibility;
import mezz.jei.common.config.DebugConfig;
import mezz.jei.common.util.Translator;
import mezz.jei.gui.config.IClientConfig;
import mezz.jei.gui.config.IIngredientFilterConfig;
import mezz.jei.gui.filter.IFilterTextSource;
import mezz.jei.gui.overlay.IIngredientGridSource;
import mezz.jei.gui.search.ElementPrefixParser;
import mezz.jei.gui.search.ElementSearch;
import mezz.jei.gui.search.ElementSearchLowMem;
import mezz.jei.gui.search.IElementSearch;
import net.minecraft.core.NonNullList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class IngredientFilter implements IIngredientGridSource, IIngredientManager.IIngredientListener {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final Pattern QUOTE_PATTERN = Pattern.compile("\"");
	private static final Pattern FILTER_SPLIT_PATTERN = Pattern.compile("(-?\".*?(?:\"|$)|\\S+)");

	private final IFilterTextSource filterTextSource;
	private final IIngredientManager ingredientManager;
	private final IIngredientSorter sorter;
	private final IModIdHelper modIdHelper;
	private final IIngredientVisibility ingredientVisibility;

	private final IElementSearch elementSearch;
	private final ElementPrefixParser elementPrefixParser;
	private final Set<String> modNamesForSorting = new HashSet<>();

	@Nullable
	private List<ITypedIngredient<?>> ingredientListCached;
	private final List<SourceListChangedListener> listeners = new ArrayList<>();

	public IngredientFilter(
		IFilterTextSource filterTextSource,
		IClientConfig clientConfig,
		IIngredientFilterConfig config,
		IIngredientManager ingredientManager,
		IIngredientSorter sorter,
		NonNullList<IListElement<?>> ingredients,
		IModIdHelper modIdHelper,
		IIngredientVisibility ingredientVisibility,
		IColorHelper colorHelper
	) {
		this.filterTextSource = filterTextSource;
		this.ingredientManager = ingredientManager;
		this.sorter = sorter;
		this.modIdHelper = modIdHelper;
		this.ingredientVisibility = ingredientVisibility;
		this.elementPrefixParser = new ElementPrefixParser(ingredientManager, config, colorHelper);

		if (clientConfig.isLowMemorySlowSearchEnabled()) {
			this.elementSearch = new ElementSearchLowMem();
		} else {
			this.elementSearch = new ElementSearch(this.elementPrefixParser);
		}

		LOGGER.info("Adding {} ingredients", ingredients.size());
		ingredients.stream()
			.map(i -> ListElementInfo.create(i, ingredientManager, modIdHelper))
			.flatMap(Optional::stream)
			.forEach(this::addIngredient);
		LOGGER.info("Added {} ingredients", ingredients.size());

		this.filterTextSource.addListener(filterText -> {
			ingredientListCached = null;
			notifyListenersOfChange();
		});
	}

	public <V> void addIngredient(IListElementInfo<V> info) {
		IListElement<V> element = info.getElement();
		updateHiddenState(element);

		this.elementSearch.add(info);

		String modNameForSorting = info.getModNameForSorting();
		this.modNamesForSorting.add(modNameForSorting);

		invalidateCache();
	}

	public void invalidateCache() {
		ingredientListCached = null;
		sorter.invalidateCache();
	}

	public <V> Optional<IListElementInfo<V>> searchForMatchingElement(
		IIngredientHelper<V> ingredientHelper,
		ITypedIngredient<V> typedIngredient
	) {
		V ingredient = typedIngredient.getIngredient();
		IIngredientType<V> type = typedIngredient.getType();
		Function<ITypedIngredient<V>, String> uidFunction = (i) -> ingredientHelper.getUniqueId(i.getIngredient(), UidContext.Ingredient);
		String ingredientUid = uidFunction.apply(typedIngredient);
		String displayName = ingredientHelper.getDisplayName(ingredient);
		String lowercaseDisplayName = Translator.toLowercaseWithLocale(displayName);

		ElementPrefixParser.TokenInfo tokenInfo = new ElementPrefixParser.TokenInfo(lowercaseDisplayName, ElementPrefixParser.NO_PREFIX);
		return this.elementSearch.getSearchResults(tokenInfo)
			.stream()
			.map(elementInfo -> checkForMatch(elementInfo, type, ingredientUid, uidFunction))
			.flatMap(Optional::stream)
			.findFirst();
	}

	public void updateHidden() {
		boolean changed = false;
		for (IListElementInfo<?> info : this.elementSearch.getAllIngredients()) {
			IListElement<?> element = info.getElement();
			changed |= updateHiddenState(element);
		}
		if (changed) {
			ingredientListCached = null;
			notifyListenersOfChange();
		}
	}

	public <V> boolean updateHiddenState(IListElement<V> element) {
		ITypedIngredient<V> typedIngredient = element.getTypedIngredient();
		boolean visible = this.ingredientVisibility.isIngredientVisible(typedIngredient);
		if (element.isVisible() != visible) {
			element.setVisible(visible);
			return true;
		}
		return false;
	}

	public <V> void onIngredientVisibilityChanged(ITypedIngredient<V> ingredient, boolean visible) {
		IIngredientType<V> ingredientType = ingredient.getType();
		IIngredientHelper<V> ingredientHelper = ingredientManager.getIngredientHelper(ingredientType);
		searchForMatchingElement(ingredientHelper, ingredient)
			.ifPresent(matchingElementInfo -> {
				IListElement<V> element = matchingElementInfo.getElement();
				if (element.isVisible() != visible) {
					element.setVisible(visible);
					notifyListenersOfChange();
				}
			});
	}

	@Override
	public List<ITypedIngredient<?>> getIngredientList() {
		String filterText = this.filterTextSource.getFilterText();
		filterText = filterText.toLowerCase();
		if (ingredientListCached == null) {
			ingredientListCached = getIngredientListUncached(filterText);
		}
		return ingredientListCached;
	}

	//This is used to allow the sorting function to set all item's indexes, precomputing the master sort order.
	@Unmodifiable
	public List<IListElementInfo<?>> getIngredientListPreSort(Comparator<IListElementInfo<?>> directComparator) {
		return this.elementSearch.getAllIngredients()
			.stream()
			.sorted(directComparator)
			.toList();
	}

	public Set<String> getModNamesForSorting() {
		return Collections.unmodifiableSet(this.modNamesForSorting);
	}

	public <T> List<T> getFilteredIngredients(IIngredientType<T> ingredientType) {
		return getIngredientList().stream()
			.map(i -> i.getIngredient(ingredientType))
			.flatMap(Optional::stream)
			.toList();
	}

	private List<ITypedIngredient<?>> getIngredientListUncached(String filterText) {
		String[] filters = filterText.split("\\|");
		List<SearchTokens> searchTokens = Arrays.stream(filters)
			.map(this::parseSearchTokens)
			.filter(s -> !s.toSearch.isEmpty())
			.toList();

		Stream<IListElementInfo<?>> elementInfoStream;
		if (searchTokens.isEmpty()) {
			elementInfoStream = this.elementSearch.getAllIngredients()
				.parallelStream();
		} else {
			elementInfoStream = searchTokens.stream()
				.map(this::getSearchResults)
				.flatMap(Set::stream)
				.distinct();
		}

		return elementInfoStream
			.filter(info -> info.getElement().isVisible())
			.sorted(sorter.getComparator(this, this.ingredientManager))
			.<ITypedIngredient<?>>map(IListElementInfo::getTypedIngredient)
			.toList();
	}

	private static <T> Optional<IListElementInfo<T>> checkForMatch(IListElementInfo<?> info, IIngredientType<T> ingredientType, String uid, Function<ITypedIngredient<T>, String> uidFunction) {
		return optionalCast(info, ingredientType)
			.filter(cast -> {
				ITypedIngredient<T> typedIngredient = cast.getTypedIngredient();
				String elementUid = uidFunction.apply(typedIngredient);
				return uid.equals(elementUid);
			});
	}

	private static <T> Optional<IListElementInfo<T>> optionalCast(IListElementInfo<?> info, IIngredientType<T> ingredientType) {
		ITypedIngredient<?> typedIngredient = info.getTypedIngredient();
		if (typedIngredient.getType() == ingredientType) {
			@SuppressWarnings("unchecked")
			IListElementInfo<T> cast = (IListElementInfo<T>) info;
			return Optional.of(cast);
		}
		return Optional.empty();
	}

	@Override
	public <V> void onIngredientsAdded(IIngredientHelper<V> ingredientHelper, Collection<ITypedIngredient<V>> ingredients) {
		for (ITypedIngredient<V> value : ingredients) {
			Optional<IListElementInfo<V>> matchingElementInfo = searchForMatchingElement(ingredientHelper, value);
			if (matchingElementInfo.isPresent()) {
				IListElement<V> matchingElement = matchingElementInfo.get().getElement();
				updateHiddenState(matchingElement);
				if (DebugConfig.isDebugModeEnabled()) {
					LOGGER.debug("Updated ingredient: {}", ingredientHelper.getErrorInfo(value.getIngredient()));
				}
			} else {
				IListElement<V> element = IngredientListElementFactory.createOrderedElement(value);
				ListElementInfo.create(element, this.ingredientManager, modIdHelper)
					.ifPresent(info -> {
						addIngredient(info);
						if (DebugConfig.isDebugModeEnabled()) {
							LOGGER.debug("Added ingredient: {}", ingredientHelper.getErrorInfo(value.getIngredient()));
						}
					});
			}
		}
		invalidateCache();
	}

	@Override
	public <V> void onIngredientsRemoved(IIngredientHelper<V> ingredientHelper, Collection<ITypedIngredient<V>> ingredients) {
		for (ITypedIngredient<V> typedIngredient : ingredients) {
			Optional<IListElementInfo<V>> matchingElementInfo = searchForMatchingElement(ingredientHelper, typedIngredient);
			if (matchingElementInfo.isEmpty()) {
				String errorInfo = ingredientHelper.getErrorInfo(typedIngredient.getIngredient());
				LOGGER.error("Could not find a matching ingredient to remove: {}", errorInfo);
			} else {
				if (DebugConfig.isDebugModeEnabled()) {
					LOGGER.debug("Removed ingredient: {}", ingredientHelper.getErrorInfo(typedIngredient.getIngredient()));
				}
				IListElement<V> matchingElement = matchingElementInfo.get().getElement();
				matchingElement.setVisible(false);
			}
		}

		invalidateCache();
	}

	private record SearchTokens(List<ElementPrefixParser.TokenInfo> toSearch, List<ElementPrefixParser.TokenInfo> toRemove) {}

	private SearchTokens parseSearchTokens(String filterText) {
		SearchTokens searchTokens = new SearchTokens(new ArrayList<>(), new ArrayList<>());

		if (filterText.isEmpty()) {
			return searchTokens;
		}
		Matcher filterMatcher = FILTER_SPLIT_PATTERN.matcher(filterText);
		while (filterMatcher.find()) {
			String string = filterMatcher.group(1);
			final boolean remove = string.startsWith("-");
			if (remove) {
				string = string.substring(1);
			}
			string = QUOTE_PATTERN.matcher(string).replaceAll("");
			if (string.isEmpty()) {
				continue;
			}
			this.elementPrefixParser.parseToken(string)
				.ifPresent(result -> {
					if (remove) {
						searchTokens.toRemove.add(result);
					} else {
						searchTokens.toSearch.add(result);
					}
				});
		}
		return searchTokens;
	}

	private Set<IListElementInfo<?>> getSearchResults(SearchTokens searchTokens) {
		List<Set<IListElementInfo<?>>> resultsPerToken = searchTokens.toSearch.stream()
			.map(this.elementSearch::getSearchResults)
			.toList();
		Set<IListElementInfo<?>> results = intersection(resultsPerToken);

		if (!results.isEmpty() && !searchTokens.toRemove.isEmpty()) {
			for (ElementPrefixParser.TokenInfo tokenInfo : searchTokens.toRemove) {
				Set<IListElementInfo<?>> resultsToRemove = this.elementSearch.getSearchResults(tokenInfo);
				results.removeAll(resultsToRemove);
				if (results.isEmpty()) {
					break;
				}
			}
		}
		return results;

	}

	/**
	 * Get the elements that are contained in every set.
	 */
	private static <T> Set<T> intersection(List<Set<T>> sets) {
		Set<T> smallestSet = sets.stream()
			.min(Comparator.comparing(Set::size))
			.orElseGet(Set::of);

		Set<T> results = Collections.newSetFromMap(new IdentityHashMap<>());
		results.addAll(smallestSet);

		for (Set<T> set : sets) {
			if (set == smallestSet) {
				continue;
			}
			if (results.retainAll(set) && results.isEmpty()) {
				break;
			}
		}
		return results;
	}

	@Override
	public void addSourceListChangedListener(SourceListChangedListener listener) {
		listeners.add(listener);
	}

	private void notifyListenersOfChange() {
		for (SourceListChangedListener listener : listeners) {
			listener.onSourceListChanged();
		}
	}
}
