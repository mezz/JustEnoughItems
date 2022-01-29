package mezz.jei.recipes;

import com.google.common.collect.ImmutableList;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.collect.ListMultiMap;
import mezz.jei.collect.Table;
import mezz.jei.ingredients.IngredientInformationUtil;
import mezz.jei.ingredients.IngredientsForType;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A RecipeMap efficiently links recipes, IRecipeCategory, and Ingredients.
 */
public class RecipeMap {
	private final Table<IRecipeCategory<?>, String, List<Object>> recipeTable = Table.hashBasedTable();
	private final ListMultiMap<String, ResourceLocation> categoryUidMap = new ListMultiMap<>();
	private final Comparator<ResourceLocation> recipeCategoryUidComparator;
	private final IIngredientManager ingredientManager;

	public RecipeMap(Comparator<ResourceLocation> recipeCategoryUidComparator, IIngredientManager ingredientManager) {
		this.recipeCategoryUidComparator = recipeCategoryUidComparator;
		this.ingredientManager = ingredientManager;
	}

	public <V> ImmutableList<ResourceLocation> getRecipeCategories(V ingredient) {
		IIngredientHelper<V> ingredientHelper = ingredientManager.getIngredientHelper(ingredient);

		Set<ResourceLocation> recipeCategories = new HashSet<>();

		for (String key : IngredientInformationUtil.getUniqueIdsWithWildcard(ingredientHelper, ingredient, UidContext.Recipe)) {
			recipeCategories.addAll(categoryUidMap.get(key));
		}

		return ImmutableList.sortedCopyOf(recipeCategoryUidComparator, recipeCategories);
	}

	public <V> void addRecipeCategory(IRecipeCategory<?> recipeCategory, V ingredient, IIngredientHelper<V> ingredientHelper) {
		String key = ingredientHelper.getUniqueId(ingredient, UidContext.Recipe);
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
		for (String key : IngredientInformationUtil.getUniqueIdsWithWildcard(ingredientHelper, ingredient, UidContext.Recipe)) {
			@SuppressWarnings("unchecked")
			List<T> recipes = (List<T>) recipesForType.get(key);
			if (recipes != null) {
				listBuilder.addAll(recipes);
			}
		}
		return listBuilder.build();
	}

	public <T> void addRecipe(T recipe, IRecipeCategory<T> recipeCategory, List<IngredientsForType<?>> ingredientsByType) {
		for (IngredientsForType<?> ingredientsForType : ingredientsByType) {
			addRecipe(recipe, recipeCategory, ingredientsForType);
		}
	}

	private <T, V> void addRecipe(T recipe, IRecipeCategory<T> recipeCategory, IngredientsForType<V> ingredientsForType) {
		IIngredientType<V> ingredientType = ingredientsForType.getIngredientType();
		IIngredientHelper<V> ingredientHelper = ingredientManager.getIngredientHelper(ingredientType);

		Map<String, List<Object>> recipesForType = recipeTable.getRow(recipeCategory);

		Set<String> uniqueIds = new HashSet<>();

		List<List<V>> ingredients = ingredientsForType.getIngredients();
		for (List<V> slot : ingredients) {
			for (V ingredient : slot) {
				if (ingredient == null) {
					continue;
				}

				String key = ingredientHelper.getUniqueId(ingredient, UidContext.Recipe);
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
}
