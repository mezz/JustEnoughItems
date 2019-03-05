package mezz.jei.recipes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.util.ResourceLocation;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.collect.ListMultiMap;
import mezz.jei.collect.Table;
import mezz.jei.ingredients.IngredientInformation;

/**
 * A RecipeMap efficiently links recipes, IRecipeCategory, and Ingredients.
 */
public class RecipeMap {
	private final Table<IRecipeCategory, String, List<Object>> recipeTable = Table.hashBasedTable();
	private final ListMultiMap<String, ResourceLocation> categoryUidMap = new ListMultiMap<>();
	private final Ordering<ResourceLocation> recipeCategoryOrdering;
	private final IIngredientManager ingredientManager;

	public RecipeMap(final RecipeCategoryComparator recipeCategoryComparator, IIngredientManager ingredientManager) {
		this.recipeCategoryOrdering = Ordering.from(recipeCategoryComparator);
		this.ingredientManager = ingredientManager;
	}

	public <V> List<ResourceLocation> getRecipeCategories(V ingredient) {
		IIngredientHelper<V> ingredientHelper = ingredientManager.getIngredientHelper(ingredient);

		Set<ResourceLocation> recipeCategories = new HashSet<>();

		for (String key : IngredientInformation.getUniqueIdsWithWildcard(ingredientHelper, ingredient)) {
			recipeCategories.addAll(categoryUidMap.get(key));
		}

		return recipeCategoryOrdering.immutableSortedCopy(recipeCategories);
	}

	public <V> void addRecipeCategory(IRecipeCategory recipeCategory, V ingredient, IIngredientHelper<V> ingredientHelper) {
		String key = ingredientHelper.getUniqueId(ingredient);
		List<ResourceLocation> recipeCategories = categoryUidMap.get(key);
		ResourceLocation recipeCategoryUid = recipeCategory.getUid();
		if (!recipeCategories.contains(recipeCategoryUid)) {
			recipeCategories.add(recipeCategoryUid);
		}
	}

	public <T, V> ImmutableList<T> getRecipes(IRecipeCategory<T> recipeCategory, V ingredient) {
		IIngredientHelper<V> ingredientHelper = ingredientManager.getIngredientHelper(ingredient);

		Map<String, List<Object>> recipesForType = recipeTable.getRow(recipeCategory);

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

	public <T> void addRecipe(T recipe, IRecipeCategory<T> recipeCategory, Map<IIngredientType, List> ingredientsByType) {
		for (Map.Entry<IIngredientType, List> entry : ingredientsByType.entrySet()) {
			if (entry != null) {
				//noinspection unchecked
				addRecipe(recipe, recipeCategory, entry.getKey(), entry.getValue());
			}
		}
	}

	private <T, V> void addRecipe(T recipe, IRecipeCategory<T> recipeCategory, IIngredientType<V> ingredientType, List<V> ingredients) {
		IIngredientHelper<V> ingredientHelper = ingredientManager.getIngredientHelper(ingredientType);

		Map<String, List<Object>> recipesForType = recipeTable.getRow(recipeCategory);

		Set<String> uniqueIds = new HashSet<>();

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

			@SuppressWarnings("unchecked")
			List<T> recipes = (List<T>) recipesForType.computeIfAbsent(key, k -> new ArrayList<>());

			recipes.add(recipe);

			addRecipeCategory(recipeCategory, ingredient, ingredientHelper);
		}
	}
}
