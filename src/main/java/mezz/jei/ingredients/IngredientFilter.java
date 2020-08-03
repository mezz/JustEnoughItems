package mezz.jei.ingredients;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import net.minecraft.util.NonNullList;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.config.IEditModeConfig;
import mezz.jei.config.IIngredientFilterConfig;
import mezz.jei.config.SearchMode;
import mezz.jei.events.EditModeToggleEvent;
import mezz.jei.events.EventBusHelper;
import mezz.jei.events.PlayerJoinedWorldEvent;
import mezz.jei.gui.ingredients.IIngredientListElement;
import mezz.jei.gui.ingredients.IIngredientListElementInfo;
import mezz.jei.gui.overlay.IIngredientGridSource;
import mezz.jei.suffixtree.CombinedSearchTrees;
import mezz.jei.suffixtree.GeneralizedSuffixTree;
import mezz.jei.suffixtree.ISearchTree;
import mezz.jei.util.Translator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class IngredientFilter implements IIngredientGridSource {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final Pattern QUOTE_PATTERN = Pattern.compile("\"");
	private static final Pattern FILTER_SPLIT_PATTERN = Pattern.compile("(-?\".*?(?:\"|$)|\\S+)");

	private final IngredientBlacklistInternal blacklist;
	private final IEditModeConfig editModeConfig;
	private final IIngredientManager ingredientManager;
	/**
	 * indexed list of ingredients for use with the suffix trees
	 * includes all elements (even hidden ones) for use when rebuilding
	 */
	private final NonNullList<IIngredientListElement<?>> elementList;
	private final GeneralizedSuffixTree searchTree;
	private final Char2ObjectMap<PrefixedSearchTree> prefixedSearchTrees = new Char2ObjectOpenHashMap<>();
	private final IngredientFilterBackgroundBuilder backgroundBuilder;
	private CombinedSearchTrees combinedSearchTrees;

	@Nullable
	private String filterCached;
	private List<IIngredientListElement<?>> ingredientListCached = Collections.emptyList();
	private final List<IIngredientGridSource.Listener> listeners = new ArrayList<>();

	public IngredientFilter(
		IngredientBlacklistInternal blacklist,
		IIngredientFilterConfig config,
		IEditModeConfig editModeConfig,
		IIngredientManager ingredientManager,
		IModIdHelper modIdHelper
	) {
		this.blacklist = blacklist;
		this.editModeConfig = editModeConfig;
		this.ingredientManager = ingredientManager;
		this.elementList = NonNullList.create();
		this.searchTree = new GeneralizedSuffixTree();
		createPrefixedSearchTree('@', config::getModNameSearchMode, IIngredientListElementInfo::getModNameStrings);
		createPrefixedSearchTree('#', config::getTooltipSearchMode, (e) -> e.getTooltipStrings(config));
		createPrefixedSearchTree('$', config::getTagSearchMode, IIngredientListElementInfo::getTagStrings);
		createPrefixedSearchTree('%', config::getCreativeTabSearchMode, IIngredientListElementInfo::getCreativeTabsStrings);
		createPrefixedSearchTree('^', config::getColorSearchMode, IIngredientListElementInfo::getColorStrings);
		createPrefixedSearchTree('&', config::getResourceIdSearchMode, element -> Collections.singleton(element.getResourceId()));

		this.combinedSearchTrees = buildCombinedSearchTrees(this.searchTree, this.prefixedSearchTrees.values());
		this.backgroundBuilder = new IngredientFilterBackgroundBuilder(prefixedSearchTrees, elementList, ingredientManager, modIdHelper);

		EventBusHelper.addListener(EditModeToggleEvent.class, editModeToggleEvent -> {
			this.filterCached = null;
			updateHidden();
		});

		EventBusHelper.addListener(PlayerJoinedWorldEvent.class, playerJoinedWorldEvent -> {
			this.filterCached = null;
			updateHidden();
		});
	}

	private static CombinedSearchTrees buildCombinedSearchTrees(ISearchTree searchTree, Collection<PrefixedSearchTree> prefixedSearchTrees) {
		CombinedSearchTrees combinedSearchTrees = new CombinedSearchTrees();
		combinedSearchTrees.addSearchTree(searchTree);
		for (PrefixedSearchTree prefixedTree : prefixedSearchTrees) {
			if (prefixedTree.getMode() == SearchMode.ENABLED) {
				combinedSearchTrees.addSearchTree(prefixedTree.getTree());
			}
		}
		return combinedSearchTrees;
	}

	private void createPrefixedSearchTree(char prefix, PrefixedSearchTree.IModeGetter modeGetter, PrefixedSearchTree.IStringsGetter stringsGetter) {
		GeneralizedSuffixTree tree = new GeneralizedSuffixTree();
		PrefixedSearchTree prefixedTree = new PrefixedSearchTree(tree, stringsGetter, modeGetter);
		this.prefixedSearchTrees.put(prefix, prefixedTree);
	}

	public void addIngredients(NonNullList<IIngredientListElement<?>> ingredients, IIngredientManager ingredientManager, IModIdHelper modIdHelper) {
		List<IIngredientListElementInfo<?>> ingredientInfo = ingredients.stream()
			.map(i -> IngredientListElementInfo.create(i, ingredientManager, modIdHelper))
			.sorted(IngredientListElementComparator.INSTANCE)
			.collect(Collectors.toList());
		String currentModName = null;
		for (IIngredientListElementInfo<?> element : ingredientInfo) {
			String modname = element.getModNameForSorting();
			if (!Objects.equals(currentModName, modname)) {
				currentModName = modname;
				LOGGER.debug("Indexing ingredients: " + modname);
			}
			addIngredient(element);
		}
	}

	public <V> void addIngredient(IIngredientListElementInfo<V> info) {
		IIngredientListElement<V> element = info.getElement();
		updateHiddenState(element);
		final int index = elementList.size();
		elementList.add(element);
		searchTree.put(Translator.toLowercaseWithLocale(info.getDisplayName()), index);

		for (PrefixedSearchTree prefixedSearchTree : this.prefixedSearchTrees.values()) {
			SearchMode searchMode = prefixedSearchTree.getMode();
			if (searchMode != SearchMode.DISABLED) {
				Collection<String> strings = prefixedSearchTree.getStringsGetter().getStrings(info);
				for (String string : strings) {
					prefixedSearchTree.getTree().put(string, index);
				}
			}
		}
		filterCached = null;
	}

	public void invalidateCache() {
		this.filterCached = null;
	}

	public <V> List<IIngredientListElement<V>> findMatchingElements(IIngredientHelper<V> ingredientHelper, V ingredient) {
		final String ingredientUid = ingredientHelper.getUniqueId(ingredient);
		final String displayName = ingredientHelper.getDisplayName(ingredient);
		@SuppressWarnings("unchecked") final Class<? extends V> ingredientClass = (Class<? extends V>) ingredient.getClass();

		final List<IIngredientListElement<V>> matchingElements = new ArrayList<>();
		final IntSet matchingIndexes = searchTree.search(Translator.toLowercaseWithLocale(displayName));
		final IntIterator iterator = matchingIndexes.iterator();
		while (iterator.hasNext()) {
			int index = iterator.nextInt();
			IIngredientListElement<?> matchingElement = this.elementList.get(index);
			Object matchingIngredient = matchingElement.getIngredient();
			if (ingredientClass.isInstance(matchingIngredient)) {
				V castMatchingIngredient = ingredientClass.cast(matchingIngredient);
				String matchingUid = ingredientHelper.getUniqueId(castMatchingIngredient);
				if (ingredientUid.equals(matchingUid)) {
					@SuppressWarnings({"unchecked", "CastCanBeRemovedNarrowingVariableType"})
					IIngredientListElement<V> matchingElementCast = (IIngredientListElement<V>) matchingElement;
					matchingElements.add(matchingElementCast);
				}
			}
		}
		return matchingElements;
	}

	public void modesChanged() {
		this.combinedSearchTrees = buildCombinedSearchTrees(this.searchTree, this.prefixedSearchTrees.values());
		this.backgroundBuilder.start();
		this.filterCached = null;
	}

	public void updateHidden() {
		for (IIngredientListElement<?> element : elementList) {
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
			List<IIngredientListElement<?>> ingredientList = getIngredientListUncached(filterText);
			ingredientListCached = Collections.unmodifiableList(ingredientList);
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

	private List<IIngredientListElement<?>> getIngredientListUncached(String filterText) {
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

		List<IIngredientListElement<?>> matchingIngredients = new ArrayList<>();

		if (matches == null) {
			for (IIngredientListElement<?> element : elementList) {
				if (element.isVisible()) {
					matchingIngredients.add(element);
				}
			}
		} else {
			int[] matchesList = matches.toIntArray();
			Arrays.sort(matchesList);
			for (int match : matchesList) {
				IIngredientListElement<?> element = elementList.get(match);
				if (element.isVisible()) {
					matchingIngredients.add(element);
				}
			}
		}
		return matchingIngredients;
	}

	/**
	 * Scans up and down the element list to find wildcard matches that touch the given element.
	 */
	public <T> List<IIngredientListElement<T>> getMatches(T ingredient, IIngredientHelper<T> ingredientHelper, Function<T, String> uidFunction) {
		final String uid = uidFunction.apply(ingredient);
		@SuppressWarnings("unchecked")
		Class<? extends T> ingredientClass = (Class<? extends T>) ingredient.getClass();
		List<IIngredientListElement<T>> matchingElements = findMatchingElements(ingredientHelper, ingredient);
		IntSet matchingIndexes = new IntOpenHashSet(50);
		IntSet startingIndexes = new IntOpenHashSet(matchingElements.size());
		for (IIngredientListElement<?> matchingElement : matchingElements) {
			int index = this.elementList.indexOf(matchingElement);
			startingIndexes.add(index);
			matchingIndexes.add(index);
		}

		IntIterator iterator = startingIndexes.iterator();
		while (iterator.hasNext()) {
			int startingIndex = iterator.nextInt();
			for (int i = startingIndex - 1; i >= 0 && !matchingIndexes.contains(i); i--) {
				IIngredientListElement<?> element = this.elementList.get(i);
				Object elementIngredient = element.getIngredient();
				if (elementIngredient.getClass() != ingredientClass) {
					break;
				}
				String elementWildcardId = uidFunction.apply(ingredientClass.cast(elementIngredient));
				if (!uid.equals(elementWildcardId)) {
					break;
				}
				matchingIndexes.add(i);
				@SuppressWarnings({"unchecked", "CastCanBeRemovedNarrowingVariableType"})
				IIngredientListElement<T> castElement = (IIngredientListElement<T>) element;
				matchingElements.add(castElement);
			}
			for (int i = startingIndex + 1; i < this.elementList.size() && !matchingIndexes.contains(i); i++) {
				IIngredientListElement<?> element = this.elementList.get(i);
				Object elementIngredient = element.getIngredient();
				if (elementIngredient.getClass() != ingredientClass) {
					break;
				}
				String elementWildcardId = uidFunction.apply(ingredientClass.cast(elementIngredient));
				if (!uid.equals(elementWildcardId)) {
					break;
				}
				matchingIndexes.add(i);
				@SuppressWarnings({"unchecked", "CastCanBeRemovedNarrowingVariableType"})
				IIngredientListElement<T> castElement = (IIngredientListElement<T>) element;
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
		final PrefixedSearchTree prefixedSearchTree = this.prefixedSearchTrees.get(firstChar);
		if (prefixedSearchTree != null && prefixedSearchTree.getMode() != SearchMode.DISABLED) {
			token = token.substring(1);
			if (token.isEmpty()) {
				return null;
			}
			GeneralizedSuffixTree tree = prefixedSearchTree.getTree();
			return tree.search(token);
		} else {
			return combinedSearchTrees.search(token);
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
