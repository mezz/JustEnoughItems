package mezz.jei.ingredients;

import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.config.IClientConfig;
import mezz.jei.config.IEditModeConfig;
import mezz.jei.config.IIngredientFilterConfig;
import mezz.jei.config.IWorldConfig;
import mezz.jei.config.SearchMode;
import mezz.jei.events.EditModeToggleEvent;
import mezz.jei.events.EventBusHelper;
import mezz.jei.events.PlayerJoinedWorldEvent;
import mezz.jei.gui.ingredients.IIngredientListElement;
import mezz.jei.gui.overlay.IIngredientGridSource;
import mezz.jei.search.ElementSearch;
import mezz.jei.search.ElementSearchLowMem;
import mezz.jei.search.IElementSearch;
import mezz.jei.search.PrefixInfo;
import mezz.jei.util.LoggedTimer;
import mezz.jei.util.Translator;
import net.minecraft.core.NonNullList;
import org.apache.logging.log4j.LogManager;

import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class IngredientFilter implements IIngredientGridSource {
	private static final Pattern QUOTE_PATTERN = Pattern.compile("\"");
	private static final Pattern FILTER_SPLIT_PATTERN = Pattern.compile("(-?\".*?(?:\"|$)|\\S+)");

	private final IngredientBlacklistInternal blacklist;
	private final IWorldConfig worldConfig;
	private final IEditModeConfig editModeConfig;
	private final IIngredientManager ingredientManager;
	private final IIngredientSorter sorter;
	private final boolean debugMode;

	private final IElementSearch elementSearch;
	private final Char2ObjectMap<PrefixInfo> prefixInfos = new Char2ObjectOpenHashMap<>();
	private final Set<String> modNamesForSorting = new HashSet<>();

	@Nullable
	private String filterCached;
	private List<?> ingredientListCached = Collections.emptyList();
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
		IModIdHelper modIdHelper) {
		this.blacklist = blacklist;
		this.worldConfig = worldConfig;
		this.editModeConfig = editModeConfig;
		this.ingredientManager = ingredientManager;
		this.sorter = sorter;

		if (clientConfig.isLowMemorySlowSearchEnabled()) {
			this.elementSearch = new ElementSearchLowMem();
		} else {
			this.elementSearch = new ElementSearch();
		}
		this.debugMode = clientConfig.isDebugModeEnabled();

		this.prefixInfos.put('@', new PrefixInfo(config::getModNameSearchMode, IIngredientListElementInfo::getModNameStrings));
		this.prefixInfos.put('#', new PrefixInfo(config::getTooltipSearchMode, e -> e.getTooltipStrings(config, ingredientManager)));
		this.prefixInfos.put('$', new PrefixInfo(config::getTagSearchMode, e -> e.getTagStrings(ingredientManager)));
		this.prefixInfos.put('%', new PrefixInfo(config::getCreativeTabSearchMode, e -> e.getCreativeTabsStrings(ingredientManager)));
		this.prefixInfos.put('^', new PrefixInfo(config::getColorSearchMode, e -> e.getColorStrings(ingredientManager)));
		this.prefixInfos.put('&', new PrefixInfo(config::getResourceLocationSearchMode, element -> Collections.singleton(element.getResourceLocation().toString())));

		for (PrefixInfo prefixInfo : this.prefixInfos.values()) {
			this.elementSearch.registerPrefix(prefixInfo);
		}

		EventBusHelper.registerWeakListener(this, EditModeToggleEvent.class, (ingredientFilter, editModeToggleEvent) -> {
			ingredientFilter.updateHidden();
		});

		EventBusHelper.registerWeakListener(this, PlayerJoinedWorldEvent.class, (ingredientFilter, playerJoinedWorldEvent) -> {
			ingredientFilter.updateHidden();
		});

		ingredients.stream()
			.map(i -> IngredientListElementInfo.create(i, ingredientManager, modIdHelper))
			.filter(Objects::nonNull)
			.forEach(this::addIngredient);
	}

	public <V> void addIngredient(IIngredientListElementInfo<V> info) {
		IIngredientListElement<V> element = info.getElement();
		updateHiddenState(element);

		this.elementSearch.add(info);

		String modNameForSorting = info.getModNameForSorting();
		this.modNamesForSorting.add(modNameForSorting);

		invalidateCache();
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
		final IntSet matchingIndexes = this.elementSearch.getSearchResults(Translator.toLowercaseWithLocale(displayName), PrefixInfo.NO_PREFIX);
		if (matchingIndexes == null) {
			return matchingElements;
		}
		final IntIterator iterator = matchingIndexes.iterator();
		while (iterator.hasNext()) {
			int index = iterator.nextInt();
			IIngredientListElementInfo<?> matchingElementInfo = this.elementSearch.get(index);
			Object matchingIngredient = matchingElementInfo.getIngredient();
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
		this.elementSearch.start();
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

	@Override
	public List<?> getIngredientList(String filterText) {
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
				.map(IIngredientListElementInfo::getIngredient)
				.collect(Collectors.toList());
			if (debugMode) {
				filterTimer.stop();
				LogManager.getLogger().info("Filter has " + ingredientListCached.size() + " of " + ingredientList.size());
			}
			filterCached = filterText;
		}
		return ingredientListCached;
	}

	//This is used to allow the sorting function to set all item's indexes, precomputing the master sort order.
	public List<IIngredientListElementInfo<?>> getIngredientListPreSort(Comparator<IIngredientListElementInfo<?>> directComparator) {
		//First step is to get the full list.
		List<IIngredientListElementInfo<?>> ingredientList = elementSearch.getAllIngredients();
		LoggedTimer filterTimer = new LoggedTimer();
		if (debugMode) {
			filterTimer.start("Pre-Sorting.");
		}
		//Then we sort it.
		List<IIngredientListElementInfo<?>> fullSortedList = ingredientList.stream()
			.sorted(directComparator)
			.collect(Collectors.toList());
		if (debugMode) {
			filterTimer.stop();
			LogManager.getLogger().info("Sort has " + ingredientList.size());
		}
		return fullSortedList;
	}


	public Set<String> getModNamesForSorting() {
		return Collections.unmodifiableSet(this.modNamesForSorting);
	}

	@SuppressWarnings("unchecked")
	public <T> List<T> getFilteredIngredients(String filterText, IIngredientType<T> ingredientType) {
		Class<? extends T> ingredientClass = ingredientType.getIngredientClass();
		List<?> ingredientList = getIngredientList(filterText);
		return (List<T>) ingredientList.stream()
			.filter(ingredientClass::isInstance)
			.toList();
	}

	private List<IIngredientListElementInfo<?>> getIngredientListUncached(String filterText) {
		String[] filters = filterText.split("\\|");

		IntSet matches = null;

		for (String filter : filters) {
			IntSet elements = getElements(filter);
			if (elements != null) {
				if (matches == null) {
					matches = elements;
				} else {
					matches.addAll(elements);
				}
			}
		}

		if (matches == null) {
			return this.elementSearch.getAllIngredients()
				.parallelStream()
				.filter(info -> {
					IIngredientListElement<?> element = info.getElement();
					return element.isVisible();
				})
				.toList();
		}

		List<IIngredientListElementInfo<?>> matchingIngredients = new ArrayList<>();
		int[] matchesList = matches.toIntArray();
		Arrays.sort(matchesList);
		for (int match : matchesList) {
			IIngredientListElementInfo<?> info = this.elementSearch.get(match);
			IIngredientListElement<?> element = info.getElement();
			if (element.isVisible()) {
				matchingIngredients.add(info);
			}
		}
		return matchingIngredients;
	}

	/**
	 * Scans up and down the element list to find wildcard matches that touch the given element.
	 */
	public <T> List<IIngredientListElementInfo<T>> getMatches(T ingredient, IIngredientHelper<T> ingredientHelper, Function<T, String> uidFunction) {
		final String uid = uidFunction.apply(ingredient);
		@SuppressWarnings("unchecked")
		Class<? extends T> ingredientClass = (Class<? extends T>) ingredient.getClass();
		List<IIngredientListElementInfo<T>> matchingElements = findMatchingElements(ingredientHelper, ingredient);
		IntSet matchingIndexes = new IntOpenHashSet(50);
		IntSet startingIndexes = new IntOpenHashSet(matchingElements.size());
		for (IIngredientListElementInfo<?> matchingElement : matchingElements) {
			int index = this.elementSearch.indexOf(matchingElement);
			startingIndexes.add(index);
			matchingIndexes.add(index);
		}

		IntIterator iterator = startingIndexes.iterator();
		while (iterator.hasNext()) {
			int startingIndex = iterator.nextInt();
			for (int i = startingIndex - 1; i >= 0 && !matchingIndexes.contains(i); i--) {
				IIngredientListElementInfo<?> info = this.elementSearch.get(i);
				Object elementIngredient = info.getIngredient();
				if (elementIngredient.getClass() != ingredientClass) {
					break;
				}
				String elementWildcardId = uidFunction.apply(ingredientClass.cast(elementIngredient));
				if (!uid.equals(elementWildcardId)) {
					break;
				}
				matchingIndexes.add(i);
				@SuppressWarnings("unchecked")
				IIngredientListElementInfo<T> castInfo = (IIngredientListElementInfo<T>) info;
				matchingElements.add(castInfo);
			}
			for (int i = startingIndex + 1; i < this.elementSearch.size() && !matchingIndexes.contains(i); i++) {
				IIngredientListElementInfo<?> info = this.elementSearch.get(i);
				Object elementIngredient = info.getIngredient();
				if (elementIngredient.getClass() != ingredientClass) {
					break;
				}
				String elementWildcardId = uidFunction.apply(ingredientClass.cast(elementIngredient));
				if (!uid.equals(elementWildcardId)) {
					break;
				}
				matchingIndexes.add(i);
				@SuppressWarnings("unchecked")
				IIngredientListElementInfo<T> castElement = (IIngredientListElementInfo<T>) info;
				matchingElements.add(castElement);
			}
		}
		return matchingElements;
	}

	@Nullable
	private IntSet getElements(String filterText) {
		Matcher filterMatcher = FILTER_SPLIT_PATTERN.matcher(filterText);

		IntSet matches = null;
		IntSet removeMatches = null;
		while (filterMatcher.find()) {
			String token = filterMatcher.group(1);
			final boolean remove = token.startsWith("-");
			if (remove) {
				token = token.substring(1);
			}
			token = QUOTE_PATTERN.matcher(token).replaceAll("");

			IntSet searchResults = getSearchResults(token);
			if (searchResults != null) {
				if (remove) {
					if (removeMatches == null) {
						removeMatches = searchResults;
					} else {
						removeMatches.addAll(searchResults);
					}
				} else {
					if (matches == null) {
						matches = searchResults;
					} else {
						matches = intersection(matches, searchResults);
					}
					if (matches.isEmpty()) {
						break;
					}
				}
			}
		}

		if (matches != null && removeMatches != null) {
			matches.removeAll(removeMatches);
		}

		return matches;
	}

	/**
	 * Gets the appropriate search tree for the given token, based on if the token has a prefix.
	 */
	@Nullable
	private IntSet getSearchResults(String token) {
		if (token.isEmpty()) {
			return null;
		}
		final char firstChar = token.charAt(0);
		final PrefixInfo prefixInfo = this.prefixInfos.get(firstChar);
		if (prefixInfo != null && prefixInfo.getMode() != SearchMode.DISABLED) {
			token = token.substring(1);
			if (token.isEmpty()) {
				return null;
			}
			return this.elementSearch.getSearchResults(token, prefixInfo);
		} else {
			return this.elementSearch.getSearchResults(token, PrefixInfo.NO_PREFIX);
		}
	}

	/**
	 * Efficiently get the elements contained in both sets.
	 * Note that this implementation will alter the original sets.
	 */
	private static IntSet intersection(IntSet set1, IntSet set2) {
		if (set1.size() > set2.size()) {
			set2.retainAll(set1);
			return set2;
		} else {
			set1.retainAll(set2);
			return set1;
		}
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
}
