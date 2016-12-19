package mezz.jei;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.abahgat.suffixtree.GeneralizedSuffixTree;
import com.google.common.collect.ImmutableList;
import gnu.trove.set.TIntSet;
import mezz.jei.config.Config;
import mezz.jei.gui.ingredients.IIngredientListElement;
import mezz.jei.util.Log;

public class ItemFilterInternals {

	private ImmutableList<Object> baseList;
	private GeneralizedSuffixTree searchTree;
	private GeneralizedSuffixTree modNameTree;
	private GeneralizedSuffixTree tooltipTree;
	private GeneralizedSuffixTree oreDictTree;
	private GeneralizedSuffixTree creativeTabTree;
	private GeneralizedSuffixTree colorTree;
	private Map<Character, GeneralizedSuffixTree> prefixedSearchTrees = new HashMap<Character, GeneralizedSuffixTree>();

	@Nullable
	private String filterCached;
	private ImmutableList<Object> ingredientListCached = ImmutableList.of();

	public ItemFilterInternals() {
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

		buildSuffixTrees(ingredientList);
	}

	private void buildSuffixTrees(List<IIngredientListElement> ingredientList) {
		Pattern spaces = Pattern.compile(" ");

		for (int i = 0; i < ingredientList.size(); i++) {
			IIngredientListElement element = ingredientList.get(i);
			putSplit(searchTree, element.getDisplayName(), i);

			Config.SearchMode modNameSearchMode = Config.getModNameSearchMode();
			if (modNameSearchMode != Config.SearchMode.DISABLED) {
				String modNameString = element.getModName();
				String modIdString = element.getModId();
				putSplit(modNameTree, modNameString, i);
				putSplit(modNameTree, modIdString, i);
				putSplit(modNameTree, spaces.matcher(modNameString).replaceAll(""), i);
				putSplit(modNameTree, spaces.matcher(modIdString).replaceAll(""), i);
				if (modNameSearchMode == Config.SearchMode.ENABLED) {
					putSplit(searchTree, modNameString, i);
					putSplit(searchTree, modIdString, i);
					putSplit(searchTree, spaces.matcher(modNameString).replaceAll(""), i);
					putSplit(searchTree, spaces.matcher(modIdString).replaceAll(""), i);
				}
			}

			Config.SearchMode tooltipSearchMode = Config.getTooltipSearchMode();
			if (tooltipSearchMode != Config.SearchMode.DISABLED) {
				String tooltipString = element.getTooltipString();

				putSplit(tooltipTree, tooltipString, i);
				if (tooltipSearchMode == Config.SearchMode.ENABLED) {
					putSplit(searchTree, tooltipString, i);
				}
			}

			Config.SearchMode oreDictSearchMode = Config.getOreDictSearchMode();
			if (oreDictSearchMode != Config.SearchMode.DISABLED) {
				String oreDictString = element.getOreDictString();
				putSplit(oreDictTree, oreDictString, i);
				if (oreDictSearchMode == Config.SearchMode.ENABLED) {
					putSplit(searchTree, oreDictString, i);
				}
			}

			Config.SearchMode creativeTabSearchMode = Config.getCreativeTabSearchMode();
			if (creativeTabSearchMode != Config.SearchMode.DISABLED) {
				String creativeTabsString = element.getCreativeTabsString();
				putSplit(creativeTabTree, creativeTabsString, i);
				if (creativeTabSearchMode == Config.SearchMode.ENABLED) {
					putSplit(searchTree, creativeTabsString, i);
				}
			}

			Config.SearchMode colorSearchMode = Config.getColorSearchMode();
			if (colorSearchMode != Config.SearchMode.DISABLED) {
				String colorString = element.getColorString();
				putSplit(colorTree, colorString, i);
				if (colorSearchMode == Config.SearchMode.ENABLED) {
					putSplit(searchTree, colorString, i);
				}
			}
		}
	}

	private static void putSplit(GeneralizedSuffixTree tree, String key, int index) {
		String[] strings = key.split(" ");
		for (String string : strings) {
			if (!string.isEmpty()) {
				tree.put(string, index);
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
		String[] tokens = filterText.split(" ");
		TIntSet matches = null;

		for (String token : tokens) {
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
