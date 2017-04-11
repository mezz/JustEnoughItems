package mezz.jei.ingredients;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.abahgat.suffixtree.GeneralizedSuffixTree;
import com.google.common.collect.ImmutableList;
import gnu.trove.set.TIntSet;
import mezz.jei.config.Config;
import mezz.jei.gui.ingredients.IIngredientListElement;

public class IngredientFilterInternals {
	private static final Pattern SPACE_PATTERN = Pattern.compile("\\s");
	private static final Pattern QUOTE_PATTERN = Pattern.compile("\"");
	private static final Pattern FILTER_SPLIT_PATTERN = Pattern.compile("(\".*?(?:\"|$)|\\S+)");

	private final ImmutableList<Object> baseList;
	private final GeneralizedSuffixTree searchTree;
	private final GeneralizedSuffixTree modNameTree;
	private final GeneralizedSuffixTree tooltipTree;
	private final GeneralizedSuffixTree oreDictTree;
	private final GeneralizedSuffixTree creativeTabTree;
	private final GeneralizedSuffixTree colorTree;
	private final GeneralizedSuffixTree resourceIdTree;
	private final Map<Character, GeneralizedSuffixTree> prefixedSearchTrees = new HashMap<Character, GeneralizedSuffixTree>();

	@Nullable
	private String filterCached;
	private ImmutableList<Object> ingredientListCached = ImmutableList.of();

	public IngredientFilterInternals() {
		List<IIngredientListElement> ingredientList = IngredientBaseListFactory.create();
		ImmutableList.Builder<Object> baseListBuilder = ImmutableList.builder();
		for (IIngredientListElement element : ingredientList) {
			baseListBuilder.add(element.getIngredient());
		}
		this.baseList = baseListBuilder.build();

		this.searchTree = new GeneralizedSuffixTree();

		this.prefixedSearchTrees.put('@', this.modNameTree = new GeneralizedSuffixTree());
		this.prefixedSearchTrees.put('#', this.tooltipTree = new GeneralizedSuffixTree());
		this.prefixedSearchTrees.put('$', this.oreDictTree = new GeneralizedSuffixTree());
		this.prefixedSearchTrees.put('%', this.creativeTabTree = new GeneralizedSuffixTree());
		this.prefixedSearchTrees.put('^', this.colorTree = new GeneralizedSuffixTree());
		this.prefixedSearchTrees.put('&', this.resourceIdTree = new GeneralizedSuffixTree());

		buildSuffixTrees(ingredientList);
	}

	private void buildSuffixTrees(List<IIngredientListElement> ingredientList) {
		for (int i = 0; i < ingredientList.size(); i++) {
			IIngredientListElement element = ingredientList.get(i);
			searchTree.put(element.getDisplayName(), i);

			Config.SearchMode modNameSearchMode = Config.getModNameSearchMode();
			if (modNameSearchMode != Config.SearchMode.DISABLED) {
				String modNameString = element.getModName();
				String modIdString = element.getModId();
				modNameTree.put(modNameString, i);
				modNameTree.put(modIdString, i);
				modNameTree.put(SPACE_PATTERN.matcher(modNameString).replaceAll(""), i);
				modNameTree.put(SPACE_PATTERN.matcher(modIdString).replaceAll(""), i);
				if (modNameSearchMode == Config.SearchMode.ENABLED) {
					searchTree.put(modNameString, i);
					searchTree.put(modIdString, i);
					searchTree.put(SPACE_PATTERN.matcher(modNameString).replaceAll(""), i);
					searchTree.put(SPACE_PATTERN.matcher(modIdString).replaceAll(""), i);
				}
			}

			Config.SearchMode tooltipSearchMode = Config.getTooltipSearchMode();
			if (tooltipSearchMode != Config.SearchMode.DISABLED) {
				String tooltipString = element.getTooltipString();

				tooltipTree.put(tooltipString, i);
				if (tooltipSearchMode == Config.SearchMode.ENABLED) {
					searchTree.put(tooltipString, i);
				}
			}

			Config.SearchMode oreDictSearchMode = Config.getOreDictSearchMode();
			if (oreDictSearchMode != Config.SearchMode.DISABLED) {
				String oreDictString = element.getOreDictString();
				oreDictTree.put(oreDictString, i);
				if (oreDictSearchMode == Config.SearchMode.ENABLED) {
					searchTree.put(oreDictString, i);
				}
			}

			Config.SearchMode creativeTabSearchMode = Config.getCreativeTabSearchMode();
			if (creativeTabSearchMode != Config.SearchMode.DISABLED) {
				String creativeTabsString = element.getCreativeTabsString();
				creativeTabTree.put(creativeTabsString, i);
				if (creativeTabSearchMode == Config.SearchMode.ENABLED) {
					searchTree.put(creativeTabsString, i);
				}
			}

			Config.SearchMode colorSearchMode = Config.getColorSearchMode();
			if (colorSearchMode != Config.SearchMode.DISABLED) {
				String colorString = element.getColorString();
				colorTree.put(colorString, i);
				if (colorSearchMode == Config.SearchMode.ENABLED) {
					searchTree.put(colorString, i);
				}
			}

			Config.SearchMode idSearchMode = Config.getResourceIdSearchMode();
			if (idSearchMode != Config.SearchMode.DISABLED) {
				String resourceIdString = element.getResourceId();
				resourceIdTree.put(resourceIdString, i);
				if (idSearchMode == Config.SearchMode.ENABLED) {
					searchTree.put(resourceIdString, i);
				}
			}
		}
	}

	public ImmutableList<Object> getIngredientList() {
		if (!Config.getFilterText().equals(filterCached)) {
			ingredientListCached = getIngredientListUncached();
			filterCached = Config.getFilterText();
		}
		return ingredientListCached;
	}

	private ImmutableList<Object> getIngredientListUncached() {
		String[] filters = Config.getFilterText().split("\\|");

		if (filters.length == 1) {
			String filter = filters[0];
			return getElements(filter);
		} else {
			ImmutableList.Builder<Object> ingredientList = ImmutableList.builder();
			for (String filter : filters) {
				List<Object> ingredients = getElements(filter);
				ingredientList.addAll(ingredients);
			}
			return ingredientList.build();
		}
	}

	private ImmutableList<Object> getElements(String filterText) {
		Matcher filterMatcher = FILTER_SPLIT_PATTERN.matcher(filterText);

		TIntSet matches = null;
		while (filterMatcher.find()) {
			String token = filterMatcher.group(1);
			token = QUOTE_PATTERN.matcher(token).replaceAll("");

			if (!token.isEmpty()) {
				char firstChar = token.charAt(0);
				GeneralizedSuffixTree tree = this.prefixedSearchTrees.get(firstChar);
				if (tree != null) {
					token = token.substring(1);
					if (token.isEmpty()) {
						continue;
					}
				} else {
					tree = searchTree;
				}

				TIntSet searchResults = tree.search(token);
				if (matches == null) {
					matches = searchResults;
				} else if (matches.size() > searchResults.size()) {
					searchResults.retainAll(matches);
					matches = searchResults;
				} else {
					matches.retainAll(searchResults);
				}

				if (matches.isEmpty()) {
					break;
				}
			}
		}

		if (matches == null) {
			return this.baseList;
		}

		int[] matchesList = matches.toArray();
		Arrays.sort(matchesList);
		ImmutableList.Builder<Object> matchingElements = ImmutableList.builder();
		for (Integer match : matchesList) {
			Object element = baseList.get(match);
			matchingElements.add(element);
		}
		return matchingElements.build();
	}
}
