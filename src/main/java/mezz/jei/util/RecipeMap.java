package mezz.jei.util;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.common.collect.Table;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRegistry;
import mezz.jei.api.recipe.IRecipeCategory;

/**
 * A RecipeMap efficiently links Recipes, IRecipeCategory, and ItemStacks.
 */
public class RecipeMap {

	private final Table<IRecipeCategory, String, List<Object>> recipeTable = HashBasedTable.create();
	private final ArrayListMultimap<String, IRecipeCategory> categoryMap = ArrayListMultimap.create();
	private final Ordering<IRecipeCategory> recipeCategoryOrdering;
	private final IIngredientRegistry ingredientRegistry;

	public RecipeMap(final RecipeCategoryComparator recipeCategoryComparator, IIngredientRegistry ingredientRegistry) {
		this.recipeCategoryOrdering = Ordering.from(recipeCategoryComparator);
		this.ingredientRegistry = ingredientRegistry;
	}

	public <V> ImmutableList<IRecipeCategory> getRecipeCategories(V ingredient) {
		IIngredientHelper<V> ingredientHelper = ingredientRegistry.getIngredientHelper(ingredient);

		Set<IRecipeCategory> recipeCategories = new HashSet<IRecipeCategory>();

		for (String key : IngredientUtil.getUniqueIdsWithWildcard(ingredientHelper, ingredient)) {
			recipeCategories.addAll(categoryMap.get(key));
		}

		return recipeCategoryOrdering.immutableSortedCopy(recipeCategories);
	}

	public <V> void addRecipeCategory(IRecipeCategory recipeCategory, V ingredient) {
		IIngredientHelper<V> ingredientHelper = ingredientRegistry.getIngredientHelper(ingredient);

		String key = ingredientHelper.getUniqueId(ingredient);
		List<IRecipeCategory> recipeCategories = categoryMap.get(key);
		if (!recipeCategories.contains(recipeCategory)) {
			recipeCategories.add(recipeCategory);
		}
	}

	public <V> ImmutableList<Object> getRecipes(IRecipeCategory recipeCategory, V ingredient) {
		IIngredientHelper<V> ingredientHelper = ingredientRegistry.getIngredientHelper(ingredient);

		Map<String, List<Object>> recipesForType = recipeTable.row(recipeCategory);

		ImmutableList.Builder<Object> listBuilder = ImmutableList.builder();
		for (String key : IngredientUtil.getUniqueIdsWithWildcard(ingredientHelper, ingredient)) {
			List<Object> recipes = recipesForType.get(key);
			if (recipes != null) {
				listBuilder.addAll(recipes);
			}
		}
		return listBuilder.build();
	}

	public void addRecipe(Object recipe, IRecipeCategory recipeCategory, Map<Class, List> ingredientsByType) {
		for (Map.Entry<Class, List> entry : ingredientsByType.entrySet()) {
			if (entry != null) {
				addRecipe(recipe, recipeCategory, entry.getKey(), entry.getValue());
			}
		}
	}

	private <V> void addRecipe(Object recipe, IRecipeCategory recipeCategory, Class<V> ingredientClass, List<V> ingredients) {
		IIngredientHelper<V> ingredientHelper = ingredientRegistry.getIngredientHelper(ingredientClass);

		Map<String, List<Object>> recipesForType = recipeTable.row(recipeCategory);

		Set<String> uniqueIds = new HashSet<String>();

		ingredients = ingredientHelper.expandSubtypes(ingredients);

		for (V ingredient : ingredients) {
			if (ingredient == null) {
				continue;
			}

			String key = ingredientHelper.getUniqueId(ingredient);
			if (uniqueIds.contains(key)) {
				continue;
			} else {
				uniqueIds.add(key);
			}

			List<Object> recipes = recipesForType.get(key);
			if (recipes == null) {
				recipes = Lists.newArrayList();
				recipesForType.put(key, recipes);
			}
			recipes.add(recipe);

			addRecipeCategory(recipeCategory, ingredient);
		}
	}
}
