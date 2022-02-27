package mezz.jei.ingredients;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.config.IClientConfig;
import mezz.jei.config.IIngredientFilterConfig;
import mezz.jei.events.EditModeToggleEvent;
import mezz.jei.events.PlayerJoinedWorldEvent;
import mezz.jei.events.RuntimeEventSubscriptions;
import mezz.jei.gui.ingredients.IListElement;
import mezz.jei.gui.overlay.IIngredientGridSource;
import mezz.jei.search.ElementSearch;
import mezz.jei.search.ElementSearchLowMem;
import mezz.jei.search.IElementSearch;
import mezz.jei.search.PrefixInfo;
import mezz.jei.search.PrefixInfos;
import mezz.jei.util.Pair;
import mezz.jei.util.Translator;
import net.minecraft.core.NonNullList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class IngredientFilter implements IIngredientGridSource {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final Pattern QUOTE_PATTERN = Pattern.compile("\"");
	private static final Pattern FILTER_SPLIT_PATTERN = Pattern.compile("(-?\".*?(?:\"|$)|\\S+)");

	private final RegisteredIngredients registeredIngredients;
	private final IIngredientSorter sorter;
	private final IngredientVisibility ingredientVisibility;

	private final IElementSearch elementSearch;
	private final PrefixInfos prefixInfos;
	private final Set<String> modNamesForSorting = new HashSet<>();

	@Nullable
	private String filterCached;
	private List<ITypedIngredient<?>> ingredientListCached = Collections.emptyList();
	private final List<IIngredientGridSource.Listener> listeners = new ArrayList<>();

	public IngredientFilter(
		IClientConfig clientConfig,
		IIngredientFilterConfig config,
		RegisteredIngredients registeredIngredients,
		IIngredientSorter sorter,
		NonNullList<IListElement<?>> ingredients,
		IModIdHelper modIdHelper,
		IngredientVisibility ingredientVisibility
	) {
		this.registeredIngredients = registeredIngredients;
		this.sorter = sorter;
		this.ingredientVisibility = ingredientVisibility;
		this.prefixInfos = new PrefixInfos(registeredIngredients, config);

		if (clientConfig.isLowMemorySlowSearchEnabled()) {
			this.elementSearch = new ElementSearchLowMem();
		} else {
			this.elementSearch = new ElementSearch(this.prefixInfos);
		}

		LOGGER.info("Adding {} ingredients", ingredients.size());
		ingredients.stream()
			.map(i -> ListElementInfo.create(i, registeredIngredients, modIdHelper))
			.filter(Objects::nonNull)
			.forEach(this::addIngredient);
		LOGGER.info("Added {} ingredients", ingredients.size());
	}

	public void register(RuntimeEventSubscriptions subscriptions) {
		subscriptions.register(EditModeToggleEvent.class, event -> this.updateHidden());
		subscriptions.register(PlayerJoinedWorldEvent.class, event -> this.updateHidden());
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
		this.filterCached = null;
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

		return this.elementSearch.getSearchResults(lowercaseDisplayName, PrefixInfo.NO_PREFIX)
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
			this.filterCached = null;
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

	@Override
	public List<ITypedIngredient<?>> getIngredientList(String filterText) {
		filterText = filterText.toLowerCase();
		if (!filterText.equals(filterCached)) {
			ingredientListCached = getIngredientListUncached(filterText);
			filterCached = filterText;
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

	public <T> List<T> getFilteredIngredients(String filterText, IIngredientType<T> ingredientType) {
		List<ITypedIngredient<?>> ingredientList = getIngredientList(filterText);
		return ingredientList.stream()
			.map(i -> i.getIngredient(ingredientType))
			.flatMap(Optional::stream)
			.toList();
	}

	private List<ITypedIngredient<?>> getIngredientListUncached(String filterText) {
		Stream<IListElementInfo<?>> elementInfoStream;
		if (filterText.isEmpty()) {
			elementInfoStream = this.elementSearch.getAllIngredients()
				.parallelStream();
		} else {
			String[] filters = filterText.split("\\|");
			elementInfoStream = Arrays.stream(filters)
				.map(this::getSearchResults)
				.flatMap(Set::stream)
				.distinct();
		}

		return elementInfoStream
			.filter(info -> info.getElement().isVisible())
			.sorted(sorter.getComparator(this, this.registeredIngredients))
			.<ITypedIngredient<?>>map(IListElementInfo::getTypedIngredient)
			.toList();
	}

	/**
	 * Scans up and down the element list to find matches that touch the given element.
	 */
	public <T> List<ITypedIngredient<T>> searchForWildcardMatches(
		ITypedIngredient<T> typedIngredient,
		IIngredientHelper<T> ingredientHelper,
		Function<ITypedIngredient<T>, String> wildcardUidFunction
	) {
		IIngredientType<T> ingredientType = typedIngredient.getType();
		Optional<IListElementInfo<T>> searchResult = searchForMatchingElement(ingredientHelper, typedIngredient);
		if (searchResult.isEmpty()) {
			return List.of();
		}

		final String wildcardUid = wildcardUidFunction.apply(typedIngredient);
		{
			String itemUid = ingredientHelper.getUniqueId(typedIngredient.getIngredient(), UidContext.Ingredient);
			if (itemUid.equals(wildcardUid)) {
				return List.of(searchResult.get().getTypedIngredient());
			}
		}

		IntSet matchingIndexes = new IntOpenHashSet();
		List<ITypedIngredient<T>> matchingElements = new ArrayList<>();

		IListElementInfo<T> matchingElement = searchResult.get();
		List<ITypedIngredient<?>> ingredientList = this.getIngredientList("");
		final int startingIndex = ingredientList.indexOf(matchingElement.getTypedIngredient());
		matchingIndexes.add(startingIndex);
		matchingElements.add(matchingElement.getTypedIngredient());

		// scan down the list then up the list,
		// adding matches until we find something that doesn't match or that we have already

		for (int i = startingIndex - 1; i >= 0 && !matchingIndexes.contains(i); i--) {
			ITypedIngredient<?> ingredient = ingredientList.get(i);
			Optional<ITypedIngredient<T>> match = checkForMatch(ingredient, ingredientType, wildcardUid, wildcardUidFunction);
			if (match.isEmpty()) {
				break;
			}
			matchingIndexes.add(i);
			matchingElements.add(match.get());
		}

		for (int i = startingIndex + 1; i < ingredientList.size() && !matchingIndexes.contains(i); i++) {
			ITypedIngredient<?> ingredient = ingredientList.get(i);
			Optional<ITypedIngredient<T>> match = checkForMatch(ingredient, ingredientType, wildcardUid, wildcardUidFunction);
			if (match.isEmpty()) {
				break;
			}
			matchingIndexes.add(i);
			matchingElements.add(match.get());
		}
		return matchingElements;
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

	private static <T> Optional<ITypedIngredient<T>> checkForMatch(ITypedIngredient<?> typedIngredient, IIngredientType<T> ingredientType, String uid, Function<ITypedIngredient<T>, String> uidFunction) {
		return optionalCast(typedIngredient, ingredientType)
			.filter(cast -> {
				String elementUid = uidFunction.apply(cast);
				return uid.equals(elementUid);
			});
	}

	private static <T> Optional<ITypedIngredient<T>> optionalCast(ITypedIngredient<?> typedIngredient, IIngredientType<T> ingredientType) {
		if (typedIngredient.getType() == ingredientType) {
			@SuppressWarnings("unchecked")
			ITypedIngredient<T> cast = (ITypedIngredient<T>) typedIngredient;
			return Optional.of(cast);
		}
		return Optional.empty();
	}

	private Set<IListElementInfo<?>> getSearchResults(String filterText) {
		if (filterText.isEmpty()) {
			return Set.of();
		}
		Matcher filterMatcher = FILTER_SPLIT_PATTERN.matcher(filterText);

		List<String> tokens = new ArrayList<>();
		List<String> removalTokens = new ArrayList<>();

		while (filterMatcher.find()) {
			String token = filterMatcher.group(1);
			final boolean remove = token.startsWith("-");
			if (remove) {
				token = token.substring(1);
			}
			token = QUOTE_PATTERN.matcher(token).replaceAll("");
			if (token.isEmpty()) {
				continue;
			}
			if (remove) {
				removalTokens.add(token);
			} else {
				tokens.add(token);
			}
		}

		List<Set<IListElementInfo<?>>> resultsPerToken = tokens.stream()
			.map(this::getSearchResultsForToken)
			.toList();
		Set<IListElementInfo<?>> results = intersection(resultsPerToken);
		if (results.isEmpty()) {
			return results;
		}

		if (!removalTokens.isEmpty()) {
			for (String token : removalTokens) {
				Set<IListElementInfo<?>> resultsToRemove = this.getSearchResultsForToken(token);
				results.removeAll(resultsToRemove);
				if (results.isEmpty()) {
					break;
				}
			}
		}
		return results;
	}

	/**
	 * Gets the appropriate search tree for the given token, based on if the token has a prefix.
	 */
	private Set<IListElementInfo<?>> getSearchResultsForToken(String token) {
		final Pair<String, PrefixInfo> result = this.prefixInfos.parseToken(token);
		token = result.first();
		PrefixInfo prefixInfo = result.second();
		return this.elementSearch.getSearchResults(token, prefixInfo);
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
		return smallestSet;
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
