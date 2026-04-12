package mezz.jei.gui.ingredients;

import mezz.jei.api.helpers.IColorHelper;
import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IIngredientVisibility;
import mezz.jei.common.config.DebugConfig;
import mezz.jei.common.config.IClientConfig;
import mezz.jei.common.config.IClientToggleState;
import mezz.jei.common.config.IIngredientFilterConfig;
import mezz.jei.common.config.IngredientGroupConfig;
import mezz.jei.common.ingredients.group.IngredientGroupInfo;
import mezz.jei.gui.config.GroupExpandStateConfig;
import mezz.jei.gui.filter.IFilterTextSource;
import mezz.jei.gui.overlay.IIngredientGridSource;
import mezz.jei.gui.overlay.elements.GroupElement;
import mezz.jei.gui.overlay.elements.GroupElementOverlay;
import mezz.jei.gui.overlay.elements.GroupMemberElement;
import mezz.jei.gui.overlay.elements.IElement;
import mezz.jei.gui.overlay.elements.IngredientElement;
import mezz.jei.gui.search.ElementPrefixParser;
import mezz.jei.gui.search.ElementSearch;
import mezz.jei.gui.search.ElementSearchLowMem;
import mezz.jei.gui.search.IElementSearch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class IngredientFilter implements
							  IIngredientGridSource,
							  IIngredientManager.IIngredientListener,
							  IIngredientVisibility.IListener,
							  IClientToggleState.IEditModeListener {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final Pattern QUOTE_PATTERN = Pattern.compile("\"");
	private static final Pattern FILTER_SPLIT_PATTERN = Pattern.compile("(-?\".*?(?:\"|$)|\\S+)");

	private final IClientConfig clientConfig;
	private final IFilterTextSource filterTextSource;
	private final IIngredientManager ingredientManager;
	private final Comparator<IListElement> ingredientComparator;
	private final IngredientGroupConfig ingredientGroupConfig;
    private final GroupExpandStateConfig groupStateConfig;
	private final IModIdHelper modIdHelper;
	private final IIngredientVisibility ingredientVisibility;

	private final ElementPrefixParser elementPrefixParser;
	private IElementSearch elementSearch;

	@Nullable
	private List<IListElement> searchResultCached;
	@Nullable
	private List<IElement> ingredientListCached;
	private final List<SourceListChangedListener> listeners = new ArrayList<>();

	public IngredientFilter(
		IFilterTextSource filterTextSource,
		IClientConfig clientConfig,
		IIngredientFilterConfig config,
		IIngredientManager ingredientManager,
		Comparator<IListElement> ingredientComparator,
		List<IListElementInfo> ingredients,
		IngredientGroupConfig groupConfig,
        GroupExpandStateConfig groupStateConfig,
		IModIdHelper modIdHelper,
		IIngredientVisibility ingredientVisibility,
		IColorHelper colorHelper,
		IClientToggleState clientToggleState
	) {
		this.filterTextSource = filterTextSource;
		this.clientConfig = clientConfig;
		this.ingredientManager = ingredientManager;
		this.ingredientComparator = ingredientComparator;
		this.ingredientGroupConfig = groupConfig;
        this.groupStateConfig = groupStateConfig;
		this.modIdHelper = modIdHelper;
		this.ingredientVisibility = ingredientVisibility;
		this.elementPrefixParser = new ElementPrefixParser(ingredientManager, config, colorHelper, modIdHelper);

		this.elementSearch = createElementSearch(clientConfig, elementPrefixParser);

		LOGGER.info("Adding {} ingredients", ingredients.size());
		for (IListElementInfo ingredient : ingredients) {
			addIngredient(ingredient);
		}

		LOGGER.info("Added {} ingredients", ingredients.size());
		if (DebugConfig.isLogSuffixTreeStatsEnabled()) {
			this.elementSearch.logStatistics();
		}

		this.filterTextSource.addListener(filterText -> {
			invalidateCache();
			notifyListenersOfChange();
		});

		clientToggleState.addEditModeToggleListener(this);
	}

	private static IElementSearch createElementSearch(IClientConfig clientConfig, ElementPrefixParser elementPrefixParser) {
		if (clientConfig.isLowMemorySlowSearchEnabled()) {
			return new ElementSearchLowMem();
		} else {
			return new ElementSearch(elementPrefixParser);
		}
	}

	public void addIngredient(IListElementInfo info) {
		IListElement element = info.getElement();
		updateHiddenState(element);

		this.elementSearch.add(info, ingredientManager);

		invalidateCache();
	}

	public void invalidateCache() {
		searchResultCached = null;
		ingredientListCached = null;
	}

	public void rebuildItemFilter() {
		this.invalidateCache();
		Collection<IListElement> ingredients = this.elementSearch.getAllIngredients();
		this.elementSearch = createElementSearch(this.clientConfig, this.elementPrefixParser);
		List<IListElementInfo> elementInfos = IngredientListElementFactory.rebuildList(ingredientManager, ingredients, modIdHelper);
		this.elementSearch.addAll(elementInfos, ingredientManager);
	}

	@Override
	public void onEditModeChanged() {
		updateHidden();
	}

	public void updateHidden() {
		boolean changed = false;
		for (IListElement element : this.elementSearch.getAllIngredients()) {
			changed |= updateHiddenState(element);
		}
		if (changed) {
			invalidateCache();
			notifyListenersOfChange();
		}
	}

	private boolean updateHiddenState(IListElement element) {
		if (element.isGroup()) {
			return false;
		}
		ITypedIngredient<?> typedIngredient = element.getTypedIngredient();
		boolean visible = this.ingredientVisibility.isIngredientVisible(typedIngredient);
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
		IListElement match = this.elementSearch.findElement(ingredient, ingredientHelper);
		if (match != null && match.isVisible() != visible) {
			match.setVisible(visible);
			invalidateCache();
			notifyListenersOfChange();
		}
	}

	@Override
	public List<IElement> getElements() {
		if (searchResultCached == null) {
			String filterText = this.filterTextSource.getFilterText().toLowerCase();
			searchResultCached = getIngredientListUncached(filterText)
					.toList();
		}
		if (ingredientListCached == null) {
			List<IListElement> listElements = new ArrayList<>();
			Map<IngredientGroupInfo, ListGroupElement> groupElements = new HashMap<>();
			for (IngredientGroupInfo groupInfo : ingredientGroupConfig.getIngredientGroups().values()) {
				groupElements.put(groupInfo, new ListGroupElement(groupInfo));
			}
			for (IListElement element : searchResultCached) {
				switch (element) {
					case ListGroupElement groupElement -> listElements.add(groupElements.get(groupElement.getGroupInfo()));
					case ListElement<?> listElement -> {
						boolean inGroup = false;
						for (Map.Entry<IngredientGroupInfo, ListGroupElement> entry : groupElements.entrySet()) {
							IngredientGroupInfo groupInfo = entry.getKey();
							if (groupInfo.isMember(listElement.getTypedIngredient(), ingredientManager)) {
								entry.getValue().addMember(listElement);
								inGroup = true;

							}
						}
						if (!inGroup) {
							listElements.add(listElement);
						}
					}
				}
			}
			listElements.sort(ingredientComparator);
			Runnable onGroupStateChange = () -> {
				ingredientListCached = null;
				notifyListenersOfChange();
			};
			List<IElement> results = new ArrayList<>();
			int groupIndex = 0;
			for (IListElement listElement : listElements) {
				switch (listElement) {
					case ListGroupElement groupElement -> {
						if (groupElement.getMembers().isEmpty()) {
							continue;
						}
                        IngredientGroupInfo groupInfo = groupElement.getGroupInfo();
						if (groupStateConfig.isExpanded(groupInfo)) {
							GroupElementOverlay overlay = new GroupElementOverlay(groupIndex);
							for (IListElement member : groupElement.getMembers()) {
								results.add(new GroupMemberElement<>(
										member.getTypedIngredient(),
										groupInfo,
										onGroupStateChange,
                                        groupStateConfig,
										overlay
								));
							}
						} else {
							results.add(new GroupElement(
									groupElement,
									onGroupStateChange,
                                    groupStateConfig,
									new GroupElementOverlay(groupIndex)
							));
						}
						groupIndex++;
					}
					case ListElement<?> element -> results.add(new IngredientElement<>(element.getTypedIngredient()));
				}
			}

			ingredientListCached = results;
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

	private Stream<IListElement> getIngredientListUncached(String filterText) {
		String[] filters = filterText.split("\\|");
		List<SearchTokens> searchTokens = Arrays.stream(filters)
			.map(this::parseSearchTokens)
			.filter(s -> !s.isEmpty())
			.toList();

		if (searchTokens.isEmpty()) {
			return this.elementSearch.getAllIngredients()
									 .parallelStream()
									 .filter(IListElement::isVisible);
		}

		return searchTokens.stream()
						   .map(this::getSearchResults)
						   .flatMap(Set::stream)
						   .filter(IListElement::isVisible)
						   .distinct();
	}

	@Override
	public <V> void onIngredientsAdded(IIngredientHelper<V> ingredientHelper, Collection<ITypedIngredient<V>> ingredients) {
		for (ITypedIngredient<V> value : ingredients) {
			IListElement matchingElement = this.elementSearch.findElement(value, ingredientHelper);
			if (matchingElement != null) {
				updateHiddenState(matchingElement);
				if (DebugConfig.isDebugModeEnabled()) {
					LOGGER.debug("Updated ingredient: {}", ingredientHelper.getErrorInfo(value.getIngredient()));
				}
			} else {
				IListElementInfo listElementInfo = ListElementInfo.create(value, this.ingredientManager, modIdHelper);
				if (listElementInfo != null) {
					addIngredient(listElementInfo);
					if (DebugConfig.isDebugModeEnabled()) {
						LOGGER.debug("Added ingredient: {}", ingredientHelper.getErrorInfo(value.getIngredient()));
					}
				}
			}
		}
		invalidateCache();
	}

	@Override
	public <V> void onIngredientsRemoved(IIngredientHelper<V> ingredientHelper, Collection<ITypedIngredient<V>> ingredients) {
		// ignore this, it's handled by onIngredientVisibilityChanged
	}

	private record SearchTokens(List<ElementPrefixParser.TokenInfo> toSearch,
								List<ElementPrefixParser.TokenInfo> toRemove) {
		public boolean isEmpty() {
			return toSearch.isEmpty() && toRemove.isEmpty();
		}
	}

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

	private Set<IListElement> getSearchResults(SearchTokens searchTokens) {
		List<Set<IListElement>> resultsPerToken = searchTokens.toSearch.stream()
			.map(this.elementSearch::getSearchResults)
			.toList();
		Set<IListElement> results = intersection(resultsPerToken);

		if (results.isEmpty() && !searchTokens.toRemove.isEmpty()) {
			results.addAll(this.elementSearch.getAllIngredients());
		}

		if (!results.isEmpty() && !searchTokens.toRemove.isEmpty()) {
			for (ElementPrefixParser.TokenInfo tokenInfo : searchTokens.toRemove) {
				Set<IListElement> resultsToRemove = this.elementSearch.getSearchResults(tokenInfo);
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
