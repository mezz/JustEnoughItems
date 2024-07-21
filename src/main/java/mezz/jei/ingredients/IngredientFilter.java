package mezz.jei.ingredients;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.config.IClientConfig;
import mezz.jei.config.IEditModeConfig;
import mezz.jei.config.IIngredientFilterConfig;
import mezz.jei.config.IWorldConfig;
import mezz.jei.events.EditModeToggleEvent;
import mezz.jei.events.EventBusHelper;
import mezz.jei.events.PlayerJoinedWorldEvent;
import mezz.jei.gui.ingredients.IIngredientListElement;
import mezz.jei.gui.overlay.IIngredientGridSource;
import mezz.jei.search.ElementPrefixParser;
import mezz.jei.search.ElementSearch;
import mezz.jei.search.ElementSearchLowMem;
import mezz.jei.search.IElementSearch;
import mezz.jei.util.LoggedTimer;
import mezz.jei.util.Translator;
import net.minecraft.util.NonNullList;
import org.apache.logging.log4j.LogManager;

import javax.annotation.Nullable;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class IngredientFilter implements IIngredientGridSource {
	private static final Pattern QUOTE_PATTERN = Pattern.compile("\"");
	private static final Pattern FILTER_SPLIT_PATTERN = Pattern.compile("(-?\".*?(?:\"|$)|\\S+)");

	private final IClientConfig clientConfig;
	private final IngredientBlacklistInternal blacklist;
	private final IWorldConfig worldConfig;
	private final IEditModeConfig editModeConfig;
	private final IIngredientManager ingredientManager;
	private final IIngredientSorter sorter;
	private final boolean debugMode;

	private final ElementPrefixParser elementPrefixParser;
	private final Set<String> modNamesForSorting = new HashSet<>();
	private IElementSearch elementSearch;

	@Nullable
	private String filterCached;
	private List<IIngredientListElement<?>> ingredientListCached = Collections.emptyList();
	private final List<IIngredientGridSource.Listener> listeners = new ArrayList<>();

	public IngredientFilter(
			IngredientBlacklistInternal blacklist,
			IWorldConfig worldConfig,
			IClientConfig clientConfig,
			IIngredientFilterConfig config,
			IEditModeConfig editModeConfig,
			IIngredientManager ingredientManager,
			IIngredientSorter sorter,
			NonNullList<IIngredientListElement<?>> ingredients,
			IModIdHelper modIdHelper
	) {
		this.blacklist = blacklist;
		this.worldConfig = worldConfig;
		this.clientConfig = clientConfig;
		this.editModeConfig = editModeConfig;
		this.ingredientManager = ingredientManager;
		this.sorter = sorter;
		this.elementPrefixParser = new ElementPrefixParser(ingredientManager, config);

		this.elementSearch = createElementSearch(clientConfig, elementPrefixParser);
		this.debugMode = clientConfig.isDebugModeEnabled();

		EventBusHelper.registerWeakListener(this, EditModeToggleEvent.class, (ingredientFilter, editModeToggleEvent) -> {
			ingredientFilter.updateHidden();
		});

		EventBusHelper.registerWeakListener(this, PlayerJoinedWorldEvent.class, (ingredientFilter, playerJoinedWorldEvent) -> {
			ingredientFilter.updateHidden();
		});

		List<IIngredientListElementInfo<?>> ingredientInfo = ingredients.stream()
			.map(i -> IngredientListElementInfo.create(i, ingredientManager, modIdHelper))
			.collect(Collectors.toList());

		for (IIngredientListElementInfo<?> element : ingredientInfo) {
			addIngredient(element);
		}
	}

	private static IElementSearch createElementSearch(IClientConfig clientConfig, ElementPrefixParser elementPrefixParser) {
		if (clientConfig.isLowMemorySlowSearchEnabled()) {
			return new ElementSearchLowMem();
		} else {
			return new ElementSearch(elementPrefixParser);
		}
	}

	public <V> void addIngredient(IIngredientListElementInfo<V> info) {
		IIngredientListElement<V> element = info.getElement();
		updateHiddenState(element);

		this.elementSearch.add(info);

		String modNameForSorting = info.getModNameForSorting();
		this.modNamesForSorting.add(modNameForSorting);

		invalidateCache();
	}

	public void rebuildItemFilter() {
		this.invalidateCache();
		Collection<IIngredientListElementInfo<?>> ingredients = this.elementSearch.getAllIngredients();
		this.elementSearch = createElementSearch(this.clientConfig, this.elementPrefixParser);
		this.elementSearch.addAll(ingredients);
	}

	public void invalidateCache() {
		this.filterCached = null;
		sorter.invalidateCache();
	}

	public <V> List<IIngredientListElementInfo<V>> findMatchingElements(IIngredientHelper<V> ingredientHelper, V ingredient) {
		final String ingredientUid = ingredientHelper.getUniqueId(ingredient, UidContext.Ingredient);
		final String displayName = ingredientHelper.getDisplayName(ingredient);
		@SuppressWarnings("unchecked") final Class<? extends V> ingredientClass = (Class<? extends V>) ingredient.getClass();

		final List<IIngredientListElementInfo<V>> matchingElements = new ArrayList<>();
		ElementPrefixParser.TokenInfo tokenInfo = new ElementPrefixParser.TokenInfo(Translator.toLowercaseWithLocale(displayName), ElementPrefixParser.NO_PREFIX);
		Set<IIngredientListElementInfo<?>> searchResults = this.elementSearch.getSearchResults(tokenInfo);
		if (searchResults.isEmpty()) {
			return matchingElements;
		}
		for (IIngredientListElementInfo<?> matchingElementInfo : searchResults) {
			Object matchingIngredient = matchingElementInfo.getElement().getIngredient();
			if (ingredientClass.isInstance(matchingIngredient)) {
				V castMatchingIngredient = ingredientClass.cast(matchingIngredient);
				String matchingUid = ingredientHelper.getUniqueId(castMatchingIngredient, UidContext.Ingredient);
				if (ingredientUid.equals(matchingUid)) {
					@SuppressWarnings("unchecked")
					IIngredientListElementInfo<V> matchingElementInfoCast = (IIngredientListElementInfo<V>) matchingElementInfo;
					matchingElements.add(matchingElementInfoCast);
				}
			}
		}
		return matchingElements;
	}

	public void modesChanged() {
		this.filterCached = null;
	}

	public void updateHidden() {
		boolean changed = false;
		for (IIngredientListElementInfo<?> info : this.elementSearch.getAllIngredients()) {
			IIngredientListElement<?> element = info.getElement();
			changed |= updateHiddenState(element);
		}
		if (changed) {
			this.filterCached = null;
			notifyListenersOfChange();
		}
	}

	@Override
	public List<IIngredientListElement<?>> getIngredientList(String filterText) {
		filterText = filterText.toLowerCase();
		if (!filterText.equals(filterCached)) {
			//First step is to get the filtered unsorted list.
			List<IIngredientListElementInfo<?>> ingredientList = getIngredientListUncached(filterText);
			LoggedTimer filterTimer = new LoggedTimer();
			if (debugMode) {
				filterTimer.start("Filtering and Sorting: " + filterText);
			}
			//Then we sort it.
			ingredientListCached = ingredientList.stream()
				.sorted(sorter.getComparator(this, this.ingredientManager))
				.map(IIngredientListElementInfo::getElement)
				.collect(Collectors.toList());
			if (debugMode) {
				filterTimer.stop();
				LogManager.getLogger().info("Filter has " + ingredientListCached.size() + " of " + ingredientList.size());
			}
			filterCached = filterText;
		}
		return ingredientListCached;
	}

	//This is used to allow the sorting function to set all item's indexes, precomuting master sort order.
	public List<IIngredientListElementInfo<?>> getIngredientListPreSort(Comparator<IIngredientListElementInfo<?>> directComparator) {
		//First step is to get the full list.
		Collection<IIngredientListElementInfo<?>> ingredientList = elementSearch.getAllIngredients();
		LoggedTimer filterTimer = new LoggedTimer();
		filterTimer.start("Pre-Sorting.");
		//Then we sort it.
		List<IIngredientListElementInfo<?>> fullSortedList = ingredientList.stream()
			.sorted(directComparator)
			.collect(Collectors.toList());
		filterTimer.stop();
		LogManager.getLogger().info("Sort has " + ingredientList.size());
		return fullSortedList;
	}


	public Set<String> getModNamesForSorting() {
		return Collections.unmodifiableSet(this.modNamesForSorting);
	}

	public ImmutableList<Object> getFilteredIngredients(String filterText) {
		List<IIngredientListElement<?>> elements = getIngredientList(filterText);
		ImmutableList.Builder<Object> builder = ImmutableList.builder();
		for (IIngredientListElement<?> element : elements) {
			Object ingredient = element.getIngredient();
			builder.add(ingredient);
		}
		return builder.build();
	}

	private List<IIngredientListElementInfo<?>> getIngredientListUncached(String filterText) {
		String[] filters = filterText.split("\\|");
		List<SearchTokens> searchTokens = Arrays.stream(filters)
				.map(this::parseSearchTokens)
				.filter(s -> !s.toSearch.isEmpty())
				.collect(Collectors.toList());

		Stream<IIngredientListElementInfo<?>> elementInfoStream;
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
				.collect(Collectors.toList());
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

	public <V> Stream<IIngredientListElementInfo<V>> searchForMatchingElement(
			V ingredient,
			IIngredientHelper<V> ingredientHelper,
			Function<V, String> uidFunction
	) {
		String ingredientUid = uidFunction.apply(ingredient);
		String displayName = ingredientHelper.getDisplayName(ingredient);
		String lowercaseDisplayName = Translator.toLowercaseWithLocale(displayName);

		ElementPrefixParser.TokenInfo tokenInfo = new ElementPrefixParser.TokenInfo(lowercaseDisplayName, ElementPrefixParser.NO_PREFIX);
		@SuppressWarnings("unchecked")
		Class<V> ingredientClass = (Class<V>) ingredient.getClass();
		return this.elementSearch.getSearchResults(tokenInfo)
				.stream()
				.map(elementInfo -> checkForMatch(elementInfo, ingredientClass, ingredientUid, uidFunction))
				.filter(Optional::isPresent)
				.map(Optional::get);
	}

	private static <T> Optional<IIngredientListElementInfo<T>> checkForMatch(IIngredientListElementInfo<?> info, Class<T> ingredientClass, String uid, Function<T, String> uidFunction) {
		return optionalCast(info, ingredientClass)
			.filter(cast -> {
				String elementUid = uidFunction.apply(cast.getElement().getIngredient());
				return uid.equals(elementUid);
			});
	}

	private static <T> Optional<IIngredientListElementInfo<T>> optionalCast(IIngredientListElementInfo<?> info, Class<T> ingredientClass) {
		Object ingredient = info.getElement().getIngredient();
		if (ingredientClass.isInstance(ingredient)) {
			@SuppressWarnings("unchecked")
			IIngredientListElementInfo<T> cast = (IIngredientListElementInfo<T>) info;
			return Optional.of(cast);
		}
		return Optional.empty();
	}

	private static final class SearchTokens {
		private final List<ElementPrefixParser.TokenInfo> toSearch;
		private final List<ElementPrefixParser.TokenInfo> toRemove;

		private SearchTokens(List<ElementPrefixParser.TokenInfo> toSearch, List<ElementPrefixParser.TokenInfo> toRemove) {
			this.toSearch = toSearch;
			this.toRemove = toRemove;
		}
	}

	/**
	 * Gets the appropriate search tree for the given token, based on if the token has a prefix.
	 */
	private Set<IIngredientListElementInfo<?>> getSearchResults(SearchTokens searchTokens) {
		List<Set<IIngredientListElementInfo<?>>> resultsPerToken = searchTokens.toSearch.stream()
				.map(this.elementSearch::getSearchResults)
				.collect(ImmutableList.toImmutableList());
		Set<IIngredientListElementInfo<?>> results = intersection(resultsPerToken);

		if (!results.isEmpty() && !searchTokens.toRemove.isEmpty()) {
			for (ElementPrefixParser.TokenInfo tokenInfo : searchTokens.toRemove) {
				Set<IIngredientListElementInfo<?>> resultsToRemove = this.elementSearch.getSearchResults(tokenInfo);
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
				.orElseGet(ImmutableSet::of);

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
	public void addListener(IIngredientGridSource.Listener listener) {
		listeners.add(listener);
	}

	public void notifyListenersOfChange() {
		for (IIngredientGridSource.Listener listener : listeners) {
			listener.onChange();
		}
	}

	public <V> boolean updateHiddenState(IIngredientListElement<V> element) {
		V ingredient = element.getIngredient();
		boolean visible = isIngredientVisible(ingredient);
		if (element.isVisible() != visible) {
			element.setVisible(visible);
			return true;
		}
		return false;
	}

	public <V> boolean isIngredientVisible(V ingredient) {
		IIngredientHelper<V> ingredientHelper = ingredientManager.getIngredientHelper(ingredient);
		return isIngredientVisible(ingredient, ingredientHelper);
	}

	public <V> boolean isIngredientVisible(V ingredient, IIngredientHelper<V> ingredientHelper) {
		if (blacklist.isIngredientBlacklistedByApi(ingredient, ingredientHelper)) {
			return false;
		}
		if (!ingredientHelper.isIngredientOnServer(ingredient)) {
			return false;
		}
		return worldConfig.isEditModeEnabled() || !editModeConfig.isIngredientOnConfigBlacklist(ingredient, ingredientHelper);
	}
}
