package mezz.jei.ingredients;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.ImmutableList;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.map.TCharObjectMap;
import gnu.trove.map.hash.TCharObjectHashMap;
import gnu.trove.set.TIntSet;
import mezz.jei.Internal;
import mezz.jei.api.IIngredientFilter;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.config.Config;
import mezz.jei.config.EditModeToggleEvent;
import mezz.jei.gui.ingredients.IIngredientListElement;
import mezz.jei.gui.overlay.IngredientListOverlay;
import mezz.jei.runtime.JeiHelpers;
import mezz.jei.runtime.JeiRuntime;
import mezz.jei.suffixtree.CombinedSearchTrees;
import mezz.jei.suffixtree.GeneralizedSuffixTree;
import mezz.jei.suffixtree.ISearchTree;
import mezz.jei.util.ErrorUtil;
import mezz.jei.util.Translator;
import net.minecraftforge.fml.common.ProgressManager;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class IngredientFilter implements IIngredientFilter {
	private static final Pattern QUOTE_PATTERN = Pattern.compile("\"");
	private static final Pattern FILTER_SPLIT_PATTERN = Pattern.compile("(\".*?(?:\"|$)|\\S+)");

	private final JeiHelpers helpers;
	/**
	 * indexed list of ingredients for use with the suffix trees
	 * includes all elements (even hidden ones) for use when rebuilding
	 */
	private final List<IIngredientListElement> elementList;
	private final GeneralizedSuffixTree searchTree;
	private final TCharObjectMap<PrefixedSearchTree> prefixedSearchTrees = new TCharObjectHashMap<PrefixedSearchTree>();
	private final List<PrefixedSearchTree> earlyLoadSearchTrees = new ArrayList<PrefixedSearchTree>();
	private CombinedSearchTrees combinedSearchTrees;

	@Nullable
	private String filterCached;
	private List<IIngredientListElement> ingredientListCached = Collections.emptyList();

	public IngredientFilter(JeiHelpers helpers) {
		this.helpers = helpers;
		this.elementList = new ArrayList<IIngredientListElement>();
		this.searchTree = new GeneralizedSuffixTree();
		PrefixedSearchTree modNameSearchTree = createPrefixedSearchTree('@', new PrefixedSearchTree.IModeGetter() {
			@Override
			public Config.SearchMode getMode() {
				return Config.getModNameSearchMode();
			}
		}, new PrefixedSearchTree.IStringsGetter() {
			@Override
			public Collection<String> getStrings(IIngredientListElement<?> element) {
				return element.getModNameStrings();
			}
		});
		PrefixedSearchTree tooltipSearchTree = createPrefixedSearchTree('#', new PrefixedSearchTree.IModeGetter() {
			@Override
			public Config.SearchMode getMode() {
				return Config.getTooltipSearchMode();
			}
		}, new PrefixedSearchTree.IStringsGetter() {
			@Override
			public Collection<String> getStrings(IIngredientListElement<?> element) {
				return element.getTooltipStrings();
			}
		});
		PrefixedSearchTree oreDictSearchTree = createPrefixedSearchTree('$', new PrefixedSearchTree.IModeGetter() {
			@Override
			public Config.SearchMode getMode() {
				return Config.getOreDictSearchMode();
			}
		}, new PrefixedSearchTree.IStringsGetter() {
			@Override
			public Collection<String> getStrings(IIngredientListElement<?> element) {
				return element.getOreDictStrings();
			}
		});
		PrefixedSearchTree creativeTabSearchTree = createPrefixedSearchTree('%', new PrefixedSearchTree.IModeGetter() {
			@Override
			public Config.SearchMode getMode() {
				return Config.getCreativeTabSearchMode();
			}
		}, new PrefixedSearchTree.IStringsGetter() {
			@Override
			public Collection<String> getStrings(IIngredientListElement<?> element) {
				return element.getCreativeTabsStrings();
			}
		});
		PrefixedSearchTree colorSearchTree = createPrefixedSearchTree('^', new PrefixedSearchTree.IModeGetter() {
			@Override
			public Config.SearchMode getMode() {
				return Config.getColorSearchMode();
			}
		}, new PrefixedSearchTree.IStringsGetter() {
			@Override
			public Collection<String> getStrings(IIngredientListElement<?> element) {
				return element.getColorStrings();
			}
		});
		PrefixedSearchTree resourceIdSearchTree = createPrefixedSearchTree('&', new PrefixedSearchTree.IModeGetter() {
			@Override
			public Config.SearchMode getMode() {
				return Config.getResourceIdSearchMode();
			}
		}, new PrefixedSearchTree.IStringsGetter() {
			@Override
			public Collection<String> getStrings(IIngredientListElement<?> element) {
				return Collections.singleton(element.getResourceId());
			}
		});

		this.earlyLoadSearchTrees.add(modNameSearchTree);
		this.earlyLoadSearchTrees.add(oreDictSearchTree);
		this.earlyLoadSearchTrees.add(creativeTabSearchTree);
		this.earlyLoadSearchTrees.add(resourceIdSearchTree);
		// tooltip and color search trees get loaded in onTick

		this.combinedSearchTrees = buildCombinedSearchTrees(this.searchTree, this.prefixedSearchTrees.valueCollection());
	}

	private static CombinedSearchTrees buildCombinedSearchTrees(ISearchTree searchTree, Collection<PrefixedSearchTree> prefixedSearchTrees) {
		CombinedSearchTrees combinedSearchTrees = new CombinedSearchTrees();
		combinedSearchTrees.addSearchTree(searchTree);
		for (PrefixedSearchTree prefixedTree : prefixedSearchTrees) {
			if (prefixedTree.getMode() == Config.SearchMode.ENABLED) {
				combinedSearchTrees.addSearchTree(prefixedTree.getTree());
			}
		}
		return combinedSearchTrees;
	}

	private PrefixedSearchTree createPrefixedSearchTree(char prefix, PrefixedSearchTree.IModeGetter modeGetter, PrefixedSearchTree.IStringsGetter stringsGetter) {
		GeneralizedSuffixTree tree = new GeneralizedSuffixTree();
		PrefixedSearchTree prefixedTree = new PrefixedSearchTree(tree, stringsGetter, modeGetter);
		this.prefixedSearchTrees.put(prefix, prefixedTree);
		return prefixedTree;
	}

	public void addIngredients(Collection<IIngredientListElement> ingredients) {
		ProgressManager.ProgressBar progressBar = ProgressManager.push("Indexing ingredients", ingredients.size());
		for (IIngredientListElement<?> element : ingredients) {
			progressBar.step(element.getDisplayName());
			addIngredient(element);
		}
		filterCached = null;
		ProgressManager.pop(progressBar);
	}

	private <V> void addIngredient(@Nullable IIngredientListElement<V> element) {
		if (element == null) {
			return;
		}
		V ingredient = element.getIngredient();

		IngredientBlacklist ingredientBlacklist = helpers.getIngredientBlacklist();
		if (ingredientBlacklist.isIngredientBlacklistedByApi(ingredient)) {
			return;
		}

		boolean hidden = Config.isIngredientOnConfigBlacklist(ingredient, element.getIngredientHelper());
		element.setHidden(hidden);

		final int index = elementList.size();
		elementList.add(element);
		searchTree.put(Translator.toLowercaseWithLocale(element.getDisplayName()), index);

		for (PrefixedSearchTree prefixedSearchTree : this.earlyLoadSearchTrees) {
			Config.SearchMode searchMode = prefixedSearchTree.getMode();
			if (searchMode != Config.SearchMode.DISABLED) {
				Collection<String> strings = prefixedSearchTree.getStringsGetter().getStrings(element);
				for (String string : strings) {
					prefixedSearchTree.getTree().put(string, index);
				}
			}
		}
	}

	public void removeIngredients(Collection<IIngredientListElement> ingredients) {
		for (IIngredientListElement element : ingredients) {
			removeIngredient(element);
		}
		filterCached = null;
	}

	private <V> void removeIngredient(IIngredientListElement<V> element) {
		final IIngredientHelper<V> ingredientHelper = element.getIngredientHelper();
		final V ingredient = element.getIngredient();
		final String ingredientUid = ingredientHelper.getUniqueId(ingredient);
		//noinspection unchecked
		final Class<? extends V> ingredientClass = (Class<? extends V>) ingredient.getClass();

		final TIntSet matchingIndexes = searchTree.search(Translator.toLowercaseWithLocale(element.getDisplayName()));
		final TIntIterator iterator = matchingIndexes.iterator();
		while (iterator.hasNext()) {
			int index = iterator.next();
			IIngredientListElement matchingElement = this.elementList.get(index);
			if (matchingElement != null) {
				Object matchingIngredient = matchingElement.getIngredient();
				if (ingredientClass.isInstance(matchingIngredient)) {
					V castMatchingIngredient = ingredientClass.cast(matchingIngredient);
					String matchingUid = ingredientHelper.getUniqueId(castMatchingIngredient);
					if (ingredientUid.equals(matchingUid)) {
						this.elementList.set(index, null);
					}
				}
			}
		}
	}

	public void modesChanged() {
		this.combinedSearchTrees = buildCombinedSearchTrees(this.searchTree, this.prefixedSearchTrees.valueCollection());
		onClientTick(10000);
		this.filterCached = null;
	}

	public void onClientTick(final int timeoutMs) {
		final long startTime = System.currentTimeMillis();
		for (PrefixedSearchTree prefixedTree : this.prefixedSearchTrees.valueCollection()) {
			Config.SearchMode mode = prefixedTree.getMode();
			if (mode != Config.SearchMode.DISABLED) {
				PrefixedSearchTree.IStringsGetter stringsGetter = prefixedTree.getStringsGetter();
				GeneralizedSuffixTree tree = prefixedTree.getTree();
				for (int i = tree.getHighestIndex() + 1; i < this.elementList.size(); i++) {
					IIngredientListElement element = elementList.get(i);
					if (element != null) {
						Collection<String> strings = stringsGetter.getStrings(element);
						if (strings.isEmpty()) {
							tree.put("", i);
						} else {
							for (String string : strings) {
								tree.put(string, i);
							}
						}
					}
					if (System.currentTimeMillis() - startTime >= timeoutMs) {
						return;
					}
				}
			}
		}
	}

	@SubscribeEvent
	public void onEditModeToggleEvent(EditModeToggleEvent event) {
		this.filterCached = null;
	}

	public List<IIngredientListElement> getIngredientList() {
		String filterText = Translator.toLowercaseWithLocale(Config.getFilterText());
		if (!filterText.equals(filterCached)) {
			List<IIngredientListElement> ingredientList = getIngredientListUncached(filterText);
			Collections.sort(ingredientList, IngredientListElementComparator.INSTANCE);
			ingredientListCached = Collections.unmodifiableList(ingredientList);
			filterCached = filterText;
		}
		return ingredientListCached;
	}

	@Override
	public ImmutableList<Object> getFilteredIngredients() {
		List<IIngredientListElement> elements = getIngredientList();
		ImmutableList.Builder<Object> builder = ImmutableList.builder();
		for (IIngredientListElement element : elements) {
			Object ingredient = element.getIngredient();
			builder.add(ingredient);
		}
		return builder.build();
	}

	@Override
	public String getFilterText() {
		return Config.getFilterText();
	}

	@Override
	public void setFilterText(String filterText) {
		ErrorUtil.checkNotNull(filterText, "filterText");
		if (Config.setFilterText(filterText)) {
			JeiRuntime runtime = Internal.getRuntime();
			if (runtime != null) {
				IngredientListOverlay ingredientListOverlay = runtime.getIngredientListOverlay();
				ingredientListOverlay.onSetFilterText(filterText);
			}
		}
	}

	private List<IIngredientListElement> getIngredientListUncached(String filterText) {
		String[] filters = filterText.split("\\|");

		if (filters.length == 1) {
			String filter = filters[0];
			return getElements(filter);
		} else {
			List<IIngredientListElement> ingredientList = new ArrayList<IIngredientListElement>();
			for (String filter : filters) {
				List<IIngredientListElement> ingredients = getElements(filter);
				ingredientList.addAll(ingredients);
			}
			return ingredientList;
		}
	}

	private List<IIngredientListElement> getElements(String filterText) {
		Matcher filterMatcher = FILTER_SPLIT_PATTERN.matcher(filterText);

		TIntSet matches = null;
		while (filterMatcher.find()) {
			String token = filterMatcher.group(1);
			token = QUOTE_PATTERN.matcher(token).replaceAll("");

			TIntSet searchResults = getSearchResults(token);
			if (searchResults != null) {
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

		List<IIngredientListElement> matchingIngredients = new ArrayList<IIngredientListElement>();

		if (matches == null) {
			for (IIngredientListElement element : elementList) {
				if (element != null && (!element.isHidden() || Config.isEditModeEnabled())) {
					matchingIngredients.add(element);
				}
			}
		} else {
			int[] matchesList = matches.toArray();
			Arrays.sort(matchesList);
			for (Integer match : matchesList) {
				IIngredientListElement<?> element = elementList.get(match);
				if (element != null && (!element.isHidden() || Config.isEditModeEnabled())) {
					matchingIngredients.add(element);
				}
			}
		}
		return matchingIngredients;
	}

	/**
	 * Gets the appropriate search tree for the given token, based on if the token has a prefix.
	 */
	@Nullable
	private TIntSet getSearchResults(String token) {
		if (token.isEmpty()) {
			return null;
		}
		final char firstChar = token.charAt(0);
		final PrefixedSearchTree prefixedSearchTree = this.prefixedSearchTrees.get(firstChar);
		if (prefixedSearchTree != null && prefixedSearchTree.getMode() != Config.SearchMode.DISABLED) {
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
	private static TIntSet intersection(TIntSet set1, TIntSet set2) {
		if (set1.size() > set2.size()) {
			set2.retainAll(set1);
			return set2;
		} else {
			set1.retainAll(set2);
			return set1;
		}
	}

	public int size() {
		return getIngredientList().size();
	}
}
