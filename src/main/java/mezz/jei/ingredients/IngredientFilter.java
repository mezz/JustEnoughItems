package mezz.jei.ingredients;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.config.IClientConfig;
import mezz.jei.config.IEditModeConfig;
import mezz.jei.config.IIngredientFilterConfig;
import mezz.jei.config.SearchMode;
import mezz.jei.events.EditModeToggleEvent;
import mezz.jei.events.EventBusHelper;
import mezz.jei.events.PlayerJoinedWorldEvent;
import mezz.jei.gui.ingredients.IIngredientListElement;
import mezz.jei.gui.overlay.IIngredientGridSource;
import mezz.jei.search.ElementSearch;
import mezz.jei.search.IElementSearch;
import mezz.jei.search.PrefixInfo;
import mezz.jei.search.ElementSearchLowMem;
import mezz.jei.util.Translator;
import net.minecraft.util.NonNullList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class IngredientFilter implements IIngredientGridSource {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final Pattern QUOTE_PATTERN = Pattern.compile("\"");
	private static final Pattern FILTER_SPLIT_PATTERN = Pattern.compile("(-?\".*?(?:\"|$)|\\S+)");

	private final IngredientBlacklistInternal blacklist;
	private final IEditModeConfig editModeConfig;
	private final IIngredientManager ingredientManager;
	private final IIngredientSorter sorter;

	private final IElementSearch elementSearch;
	private final Char2ObjectMap<PrefixInfo> prefixInfos = new Char2ObjectOpenHashMap<>();

	@Nullable
	private String filterCached;
	private List<IIngredientListElement<?>> ingredientListCached = Collections.emptyList();
	private final List<IIngredientGridSource.Listener> listeners = new ArrayList<>();

	public IngredientFilter(
		IngredientBlacklistInternal blacklist,
		IClientConfig clientConfig,
		IIngredientFilterConfig config,
		IEditModeConfig editModeConfig,
		IIngredientManager ingredientManager,
		IIngredientSorter sorter)
	{
		this.blacklist = blacklist;
		this.editModeConfig = editModeConfig;
		this.ingredientManager = ingredientManager;
		this.sorter = sorter;

		if (clientConfig.isLowMemorySlowSearchEnabled()) {
			this.elementSearch = new ElementSearchLowMem();
		} else {
			this.elementSearch = new ElementSearch();
		}

		this.prefixInfos.put('@', new PrefixInfo(config::getModNameSearchMode, IIngredientListElementInfo::getModNameStrings));
		this.prefixInfos.put('#', new PrefixInfo(config::getTooltipSearchMode, e -> e.getTooltipStrings(config, ingredientManager)));
		this.prefixInfos.put('$', new PrefixInfo(config::getTagSearchMode, e -> e.getTagStrings(ingredientManager)));
		this.prefixInfos.put('%', new PrefixInfo(config::getCreativeTabSearchMode, e -> e.getCreativeTabsStrings(ingredientManager)));
		this.prefixInfos.put('^', new PrefixInfo(config::getColorSearchMode, e -> e.getColorStrings(ingredientManager)));
		this.prefixInfos.put('&', new PrefixInfo(config::getResourceIdSearchMode, element -> Collections.singleton(element.getResourceId())));

		for (PrefixInfo prefixInfo : this.prefixInfos.values()) {
			this.elementSearch.registerPrefix(prefixInfo);
		}

		EventBusHelper.addListener(EditModeToggleEvent.class, editModeToggleEvent -> {
			this.filterCached = null;
			updateHidden();
		});

		EventBusHelper.addListener(PlayerJoinedWorldEvent.class, playerJoinedWorldEvent -> {
			this.filterCached = null;
			updateHidden();
		});
	}

	public void addIngredients(NonNullList<IIngredientListElement<?>> ingredients, IIngredientManager ingredientManager, IModIdHelper modIdHelper) {
		List<IIngredientListElementInfo<?>> ingredientInfo = ingredients.stream()
			.map(i -> IngredientListElementInfo.create(i, ingredientManager, modIdHelper))
			.sorted(sorter.getComparator())
			.collect(Collectors.toList());
		String currentModName = null;
		for (IIngredientListElementInfo<?> element : ingredientInfo) {
			if (LOGGER.isDebugEnabled()) {
				String modname = element.getModNameForSorting();
				if (!Objects.equals(currentModName, modname)) {
					currentModName = modname;
					LOGGER.debug("Indexing ingredients: " + modname);
				}
			}
			addIngredient(element);
		}
	}

	public <V> void addIngredient(IIngredientListElementInfo<V> info) {
		IIngredientListElement<V> element = info.getElement();
		updateHiddenState(element);

		this.elementSearch.add(info);

		invalidateCache();
	}

	public void invalidateCache() {
		this.filterCached = null;
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
		this.elementSearch.start();
		this.filterCached = null;
	}

	public void updateHidden() {
		for (IIngredientListElementInfo<?> info : this.elementSearch.getAllIngredients()) {
			IIngredientListElement<?> element = info.getElement();
			updateHiddenState(element);
		}
	}

	public <V> void updateHiddenState(IIngredientListElement<V> element) {
		V ingredient = element.getIngredient();
		IIngredientHelper<V> ingredientHelper = ingredientManager.getIngredientHelper(ingredient);
		boolean visible = !blacklist.isIngredientBlacklistedByApi(ingredient, ingredientHelper) &&
			ingredientHelper.isIngredientOnServer(ingredient) &&
			(editModeConfig.isEditModeEnabled() || !editModeConfig.isIngredientOnConfigBlacklist(ingredient, ingredientHelper));
		if (element.isVisible() != visible) {
			element.setVisible(visible);
			this.filterCached = null;
		}
	}

	@Override
	public List<IIngredientListElement<?>> getIngredientList(String filterText) {
		filterText = filterText.toLowerCase();
		if (!filterText.equals(filterCached)) {
			List<IIngredientListElementInfo<?>> ingredientList = getIngredientListUncached(filterText);
			ingredientListCached = ingredientList.stream()
				.sorted(sorter.getComparator())
				.map(IIngredientListElementInfo::getElement)
				.collect(Collectors.toList());
			filterCached = filterText;
		}
		return ingredientListCached;
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
				.collect(Collectors.toList());
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
				Object elementIngredient = info.getElement().getIngredient();
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
				Object elementIngredient = info.getElement().getIngredient();
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
