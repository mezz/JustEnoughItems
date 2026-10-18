package mezz.jei.gui.ingredients;

import mezz.jei.api.helpers.IColorHelper;
import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IIngredientVisibility;
import mezz.jei.api.search.ISearchStorageBuilderFactory;
import mezz.jei.common.Internal;
import mezz.jei.common.config.DebugConfig;
import mezz.jei.common.config.IClientConfig;
import mezz.jei.common.config.IClientToggleState;
import mezz.jei.common.config.IIngredientFilterConfig;
import mezz.jei.common.search.PrefixInfo;
import mezz.jei.common.util.Translator;
import mezz.jei.gui.config.IngredientTypeSortingConfig;
import mezz.jei.gui.filter.IFilterTextSource;
import mezz.jei.gui.overlay.elements.IElement;
import mezz.jei.gui.overlay.elements.IngredientElement;
import mezz.jei.gui.overlay.ingredients.IIngredientGridSource;
import mezz.jei.gui.search.ElementPrefixParser;
import mezz.jei.gui.search.ElementSearch;
import mezz.jei.gui.search.ElementSearchIndex;
import mezz.jei.gui.search.IElementSearch;
import mezz.jei.gui.search.ISearchCompletionProvider;
import mezz.jei.gui.search.SearchTokenizer;
import mezz.jei.gui.search.Token;
import net.mezzdev.config.api.value.IConfigValue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

public class IngredientFilter
	implements
		IIngredientGridSource,
		IIngredientManager.IIngredientListener,
		IIngredientVisibility.IListener,
		IClientToggleState.IEditModeListener,
		ISearchCompletionProvider {
	private static final Logger LOGGER = LogManager.getLogger();

	private final IIngredientFilterConfig config;
	private final IFilterTextSource filterTextSource;
	private final IIngredientManager ingredientManager;
	private final Comparator<IListElement<?>> ingredientComparator;
	private final IModIdHelper modIdHelper;
	private final IIngredientVisibility ingredientVisibility;
	private final IngredientTypeSortingConfig ingredientTypeSortingConfig;
	private final Function<List<IListElementInfo<?>>, Comparator<IListElement<?>>> sortIndexUpdater;

	private final ElementPrefixParser elementPrefixParser;
	private IElementSearch elementSearch;

	@Nullable
	private List<IElement<?>> ingredientListCached;
	private final List<SourceListChangedListener> listeners = new ArrayList<>();
	private final Map<IListElement<?>, IListElementInfo<?>> elementInfoCache = new LinkedHashMap<>();
	private final Set<IListElement<?>> dirtyIngredients = Collections.newSetFromMap(new IdentityHashMap<>());
	private long completionRevision;
	private final Set<ElementSearchIndex> dirtySearchIndexes = EnumSet.noneOf(ElementSearchIndex.class);
	private boolean ingredientAliasesDirty;
	private boolean sortIndexesDirty;

	public IngredientFilter(
		IFilterTextSource filterTextSource,
		IClientConfig clientConfig,
		IIngredientFilterConfig config,
		IIngredientManager ingredientManager,
		Function<List<IListElementInfo<?>>, Comparator<IListElement<?>>> sortIndexUpdater,
		List<IListElementInfo<?>> ingredients,
		IModIdHelper modIdHelper,
		IIngredientVisibility ingredientVisibility,
		IngredientTypeSortingConfig ingredientTypeSortingConfig,
		IColorHelper colorHelper,
		ISearchStorageBuilderFactory searchStorageBuilderFactory,
		IClientToggleState clientToggleState
	) {
		this.filterTextSource = filterTextSource;
		this.config = config;
		this.ingredientManager = ingredientManager;
		this.ingredientComparator = sortIndexUpdater.apply(ingredients);
		this.modIdHelper = modIdHelper;
		this.ingredientVisibility = ingredientVisibility;
		this.ingredientTypeSortingConfig = ingredientTypeSortingConfig;
		this.sortIndexUpdater = sortIndexUpdater;
		this.elementPrefixParser = new ElementPrefixParser(ingredientManager, config, colorHelper, searchStorageBuilderFactory);
		this.elementSearch = new ElementSearch(elementPrefixParser, ingredients, ingredientManager);
		addConfigListeners(clientConfig, config);

		LOGGER.info("Adding {} ingredients", ingredients.size());
		cacheElementInfos(ingredients);

		for (IListElementInfo<?> ingredient : ingredients) {
			updateHiddenState(ingredient.getElement());
		}
		invalidateCache();
		LOGGER.info("Added {} ingredients", ingredients.size());
		if (DebugConfig.isLogSuffixTreeStatsEnabled()) {
			this.elementSearch.logStatistics();
		}

		this.filterTextSource.addListener((oldFilterText, newFilterText) -> {
			invalidateCache();
			notifyListenersOfChange();
		});

		clientToggleState.addEditModeToggleListener(this);
	}

	private void addConfigListeners(IClientConfig clientConfig, IIngredientFilterConfig config) {
		Internal.registerRuntimeListenerRemoval(clientConfig.ingredientSorterStages().addListener(v -> markSortIndexesDirty()));

		addSearchIndexConfigListener(config.modNameSearchMode(), ElementSearchIndex.MOD_NAMES);
		addSearchIndexConfigListener(config.tooltipSearchMode(), ElementSearchIndex.TOOLTIPS);
		addSearchIndexConfigListener(config.tagSearchMode(), ElementSearchIndex.TAGS);
		addSearchIndexConfigListener(config.colorSearchMode(), ElementSearchIndex.COLORS);
		addSearchIndexConfigListener(config.resourceLocationSearchMode(), ElementSearchIndex.IDENTIFIERS);
		addSearchIndexConfigListener(config.creativeTabSearchMode(), ElementSearchIndex.CREATIVE_TABS);
		addSearchIndexConfigListener(config.searchAdvancedTooltips(), ElementSearchIndex.TOOLTIPS);
		addSearchIndexConfigListener(config.searchModIds(), ElementSearchIndex.MOD_NAMES);
		addSearchIndexConfigListener(config.searchModAliases(), ElementSearchIndex.MOD_NAMES);
		addSearchIndexConfigListener(config.searchShortModNames(), ElementSearchIndex.MOD_NAMES);
		Internal.registerRuntimeListenerRemoval(config.searchIngredientAliases().addListener(v -> {
			this.ingredientAliasesDirty = true;
			markSearchIndexDirty(ElementSearchIndex.NAMES_AND_ALIASES);
		}));
	}

	private <T> void addSearchIndexConfigListener(IConfigValue<T> configValue, ElementSearchIndex searchIndex) {
		Internal.registerRuntimeListenerRemoval(configValue.addListener(v -> markSearchIndexDirty(searchIndex)));
	}

	public <V> void addIngredient(IListElementInfo<V> info) {
		IListElement<V> element = info.getElement();
		updateHiddenState(element);

		this.elementSearch.add(info, ingredientManager);
		this.elementInfoCache.put(element, info);

		invalidateCache();
		invalidateCandidateCache();
	}

	public void invalidateCache() {
		ingredientListCached = null;
	}

	public void rebuildItemFilter() {
		this.invalidateCache();
		invalidateCandidateCache();
		Collection<IListElement<?>> ingredients = this.elementSearch.getAllIngredients();
		List<IListElementInfo<?>> elementInfos = IngredientListElementFactory.rebuildList(ingredientManager, ingredients, config, modIdHelper);
		this.elementInfoCache.clear();
		cacheElementInfos(elementInfos);
		this.sortIndexUpdater.apply(elementInfos);
		this.elementSearch = new ElementSearch(this.elementPrefixParser, elementInfos, ingredientManager);
		this.dirtyIngredients.clear();
		this.dirtySearchIndexes.clear();
		this.ingredientAliasesDirty = false;
		this.sortIndexesDirty = false;
	}

	private void markSearchIndexDirty(ElementSearchIndex searchIndex) {
		this.dirtySearchIndexes.add(searchIndex);
		notifyListenersOfChange();
	}

	private void markSortIndexesDirty() {
		this.sortIndexesDirty = true;
		notifyListenersOfChange();
	}

	public void onIngredientSortOrderConfigChanged() {
		markSortIndexesDirty();
	}

	public void onIngredientTypeSortOrderConfigChanged() {
		markSortIndexesDirty();
		updateHidden();
	}

	private void updateDirtyState() {
		if (ingredientAliasesDirty || !dirtyIngredients.isEmpty() || !dirtySearchIndexes.isEmpty()) {
			refreshSearch();
		}

		if (sortIndexesDirty) {
			this.sortIndexUpdater.apply(new ArrayList<>(elementInfoCache.values()));
			this.sortIndexesDirty = false;
			invalidateCache();
		}
	}

	private void refreshSearch() {
		if (ingredientAliasesDirty) {
			for (IListElementInfo<?> info : elementInfoCache.values()) {
				if (!dirtyIngredients.contains(info.getElement())) {
					info.refreshIngredientAliases(ingredientManager, config);
				}
			}
		}

		List<IListElementInfo<?>> updatedInfos = IngredientListElementFactory.rebuildList(ingredientManager, dirtyIngredients, config, modIdHelper);
		cacheElementInfos(updatedInfos);
		this.elementSearch.update(updatedInfos, dirtySearchIndexes, elementInfoCache.values());

		this.sortIndexesDirty |= !dirtyIngredients.isEmpty();
		this.dirtyIngredients.clear();
		this.dirtySearchIndexes.clear();
		this.ingredientAliasesDirty = false;
		invalidateCache();
		invalidateCandidateCache();
	}

	private void cacheElementInfos(Collection<IListElementInfo<?>> infos) {
		for (IListElementInfo<?> info : infos) {
			this.elementInfoCache.put(info.getElement(), info);
		}
	}

	@Override
	public void onEditModeChanged() {
		updateHidden();
	}

	public void updateHidden() {
		boolean changed = false;
		for (IListElement<?> element : this.elementSearch.getAllIngredients()) {
			changed |= updateHiddenState(element);
		}
		if (changed) {
			invalidateCache();
			notifyListenersOfChange();
		}
	}

	private <V> boolean updateHiddenState(IListElement<V> element) {
		ITypedIngredient<V> typedIngredient = element.getTypedIngredient();
		boolean visible = this.ingredientVisibility.isIngredientVisible(
			typedIngredient,
			UidContext.Ingredient
		) && isIngredientTypeVisible(typedIngredient.getType());
		if (element.isVisible() != visible) {
			element.setVisible(visible);
			return true;
		}
		return false;
	}

	@Override
	public <V> void onIngredientVisibilityChanged(ITypedIngredient<V> ingredient, boolean visible) {
		IIngredientType<V> ingredientType = ingredient.getType();
		IIngredientHelper<V> ingredientHelper = ingredientManager.getIngredientHelper(ingredientType);
		IListElement<V> match = this.elementSearch.findElement(ingredient, ingredientHelper);
		if (match != null && updateHiddenState(match)) {
			invalidateCache();
			notifyListenersOfChange();
		}
	}

	@Override
	public <V> void onIngredientsVisibilityChanged(Collection<ITypedIngredient<V>> ingredients, boolean visible) {
		boolean changed = false;
		for (ITypedIngredient<V> ingredient : ingredients) {
			IIngredientType<V> ingredientType = ingredient.getType();
			IIngredientHelper<V> ingredientHelper = ingredientManager.getIngredientHelper(ingredientType);
			IListElement<V> match = this.elementSearch.findElement(ingredient, ingredientHelper);
			if (match != null) {
				changed |= updateHiddenState(match);
			}
		}
		if (changed) {
			invalidateCache();
			notifyListenersOfChange();
		}
	}

	private boolean isIngredientTypeVisible(IIngredientType<?> ingredientType) {
		return this.ingredientTypeSortingConfig.isIngredientTypeVisible(
			ingredientManager.getRegisteredIngredientTypes(),
			ingredientType
		);
	}

	@Override
	public List<IElement<?>> getElements() {
		updateDirtyState();
		String filterText = this.filterTextSource.getFilterText();
		filterText = Translator.toLowercaseWithLocale(filterText);
		if (ingredientListCached == null) {
			ingredientListCached = getIngredientListUncached(filterText)
				.<IElement<?>>map(IngredientElement::new)
				.toList();
		}
		return ingredientListCached;
	}

	public <T> List<T> getFilteredIngredients(IIngredientType<T> ingredientType) {
		return getElements()
			.stream()
			.map(IElement::getTypedIngredient)
			.map(i -> i.getIngredient(ingredientType))
			.flatMap(Optional::stream)
			.toList();
	}

	private Stream<ITypedIngredient<?>> getIngredientListUncached(String filterText) {
		List<Token> tokens = SearchTokenizer.tokenize(filterText);
		List<SearchTokens> searchTokens = SearchTokenizer.splitByOperators(tokens).stream()
			.map(this::parseSearchTokens)
			.filter(s -> !s.isEmpty())
			.toList();

		Stream<IListElement<?>> elementStream;
		if (searchTokens.isEmpty()) {
			elementStream = this.elementSearch.getAllIngredients()
				.parallelStream();
		} else {
			elementStream = searchTokens.stream()
				.map(this::getSearchResults)
				.flatMap(Set::stream)
				.distinct();
		}

		return elementStream
			.filter(IListElement::isVisible)
			.sorted(ingredientComparator)
			.map(IListElement::getTypedIngredient);
	}

	@Override
	public <V> void onIngredientsAdded(IIngredientHelper<V> ingredientHelper, Collection<ITypedIngredient<V>> ingredients) {
		for (ITypedIngredient<V> value : ingredients) {
			IListElement<V> matchingElement = this.elementSearch.findElement(value, ingredientHelper);
			if (matchingElement != null) {
				updateHiddenState(matchingElement);
				if (DebugConfig.isDebugIngredientsEnabled()) {
					LOGGER.debug("Updated ingredient: {}", ingredientHelper.getErrorInfo(value.getIngredient()));
				}
			} else {
				IListElementInfo<V> listElementInfo = ListElementInfo.create(value, this.ingredientManager, config, modIdHelper);
				if (listElementInfo != null) {
					addIngredient(listElementInfo);
					if (DebugConfig.isDebugIngredientsEnabled()) {
						LOGGER.debug("Added ingredient: {}", ingredientHelper.getErrorInfo(value.getIngredient()));
					}
				}
			}
		}
		invalidateCache();
		invalidateCandidateCache();
	}

	@Override
	public <V> void onIngredientsUpdated(IIngredientHelper<V> ingredientHelper, Collection<ITypedIngredient<V>> ingredients) {
		boolean changed = false;
		for (ITypedIngredient<V> ingredient : ingredients) {
			IListElement<V> element = this.elementSearch.findElement(ingredient, ingredientHelper);
			if (element != null) {
				element.setTypedIngredient(ingredient);
				changed |= dirtyIngredients.add(element);
			}
		}
		if (changed) {
			invalidateCache();
			invalidateCandidateCache();
			notifyListenersOfChange();
		}
	}

	@Override
	public <V> void onIngredientsRemoved(IIngredientHelper<V> ingredientHelper, Collection<ITypedIngredient<V>> ingredients) {
		// ignore this, it's handled by onIngredientVisibilityChanged
	}

	private record SearchTokens(List<ElementPrefixParser.TokenInfo> toSearch, List<ElementPrefixParser.TokenInfo> toRemove) {
		public boolean isEmpty() {
			return toSearch.isEmpty() && toRemove.isEmpty();
		}
	}

	private SearchTokens parseSearchTokens(List<Token> tokens) {
		SearchTokens searchTokens = new SearchTokens(new ArrayList<>(), new ArrayList<>());
		for (Token token : tokens) {
			if (token.isEmpty()) {
				continue;
			}
			this.elementPrefixParser.parseToken(token.text())
				.ifPresent(result -> {
					if (token.exclusion()) {
						searchTokens.toRemove.add(result);
					} else {
						searchTokens.toSearch.add(result);
					}
				});
		}
		return searchTokens;
	}

	private Set<IListElement<?>> getSearchResults(SearchTokens searchTokens) {
		List<Set<IListElement<?>>> resultsPerToken = searchTokens.toSearch.stream()
			.map(this.elementSearch::getSearchResults)
			.toList();
		Set<IListElement<?>> results = intersection(resultsPerToken);

		if (results.isEmpty() && !searchTokens.toRemove.isEmpty()) {
			results.addAll(this.elementSearch.getAllIngredients());
		}

		if (!results.isEmpty() && !searchTokens.toRemove.isEmpty()) {
			for (ElementPrefixParser.TokenInfo tokenInfo : searchTokens.toRemove) {
				Set<IListElement<?>> resultsToRemove = this.elementSearch.getSearchResults(tokenInfo);
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

	@Override
	public Collection<PrefixInfo<IListElementInfo<?>, IListElement<?>>> getAllPrefixInfos() {
		return elementPrefixParser.allPrefixInfos();
	}

	@Override
	public Collection<IListElementInfo<?>> getAllElementInfos() {
		updateDirtyState();
		return Collections.unmodifiableCollection(elementInfoCache.values());
	}

	private void invalidateCandidateCache() {
		completionRevision++;
	}

	@Override
	public long getCompletionRevision() {
		return completionRevision;
	}
}
