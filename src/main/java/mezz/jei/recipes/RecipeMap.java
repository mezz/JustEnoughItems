package mezz.jei.recipes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRegistry;
import mezz.jei.api.recipe.IIngredientType;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.collect.ListMultiMap;
import mezz.jei.collect.Table;
import mezz.jei.ingredients.IngredientInformation;

/**
 * A RecipeMap efficiently links IRecipeWrappers, IRecipeCategory, and Ingredients.
 */
public class RecipeMap {
	private final Table<IRecipeCategory, String, List<IRecipeWrapper>> recipeWrapperTable = Table.hashBasedTable();
	private final ListMultiMap<String, String> categoryUidMap = new ListMultiMap<>();
	private final Ordering<String> recipeCategoryOrdering;
	private final IIngredientRegistry ingredientRegistry;

	public RecipeMap(final RecipeCategoryComparator recipeCategoryComparator, IIngredientRegistry ingredientRegistry) {
		this.recipeCategoryOrdering = Ordering.from(recipeCategoryComparator);
		this.ingredientRegistry = ingredientRegistry;
	}

	public <V> List<String> getRecipeCategories(V ingredient) {
		IIngredientHelper<V> ingredientHelper = ingredientRegistry.getIngredientHelper(ingredient);

		Set<String> recipeCategories = new HashSet<>();

		for (String key : IngredientInformation.getUniqueIdsWithWildcard(ingredientHelper, ingredient)) {
			recipeCategories.addAll(categoryUidMap.get(key));
		}

		return recipeCategoryOrdering.immutableSortedCopy(recipeCategories);
	}

	public <V> void addRecipeCategory(IRecipeCategory recipeCategory, V ingredient, IIngredientHelper<V> ingredientHelper) {
		String key = ingredientHelper.getUniqueId(ingredient);
		List<String> recipeCategories = categoryUidMap.get(key);
		String recipeCategoryUid = recipeCategory.getUid();
		if (!recipeCategories.contains(recipeCategoryUid)) {
			recipeCategories.add(recipeCategoryUid);
		}
	}

	public <T extends IRecipeWrapper, V> ImmutableList<T> getRecipeWrappers(IRecipeCategory<T> recipeCategory, V ingredient) {
		IIngredientHelper<V> ingredientHelper = ingredientRegistry.getIngredientHelper(ingredient);

		Map<String, List<IRecipeWrapper>> recipesForType = recipeWrapperTable.getRow(recipeCategory);

		ImmutableList.Builder<T> listBuilder = ImmutableList.builder();
		for (String key : IngredientInformation.getUniqueIdsWithWildcard(ingredientHelper, ingredient)) {
			@SuppressWarnings("unchecked")
			List<T> recipes = (List<T>) recipesForType.get(key);
			if (recipes != null) {
				listBuilder.addAll(recipes);
			}
		}
		return listBuilder.build();
	}

	public <T extends IRecipeWrapper> void addRecipe(T recipeWrapper, IRecipeCategory<T> recipeCategory, Map<IIngredientType, List> ingredientsByType) {
		for (Map.Entry<IIngredientType, List> entry : ingredientsByType.entrySet()) {
			if (entry != null) {
				//noinspection unchecked
				addRecipe(recipeWrapper, recipeCategory, entry.getKey(), entry.getValue());
			}
		}
	}

	private <T extends IRecipeWrapper, V> void addRecipe(T recipeWrapper, IRecipeCategory<T> recipeCategory, IIngredientType<V> ingredientType, List<V> ingredients) {
		IIngredientHelper<V> ingredientHelper = ingredientRegistry.getIngredientHelper(ingredientType);

		Map<String, List<IRecipeWrapper>> recipesWrappersForType = recipeWrapperTable.getRow(recipeCategory);

		Set<String> uniqueIds = new HashSet<>();

		List<V> expandedIngredients = ingredientHelper.expandSubtypes(ingredients);

		for (V ingredient : expandedIngredients) {
			if (ingredient == null) {
				continue;
			}

			String key = ingredientHelper.getUniqueId(ingredient);
			if (uniqueIds.contains(key)) {
				continue;
			} else {
				uniqueIds.add(key);
			}

			@SuppressWarnings("unchecked")
			List<T> recipeWrappers = (List<T>) recipesWrappersForType.computeIfAbsent(key, k -> new ArrayList<>());

			recipeWrappers.add(recipeWrapper);

			addRecipeCategory(recipeCategory, ingredient, ingredientHelper);
		}
	}
}
