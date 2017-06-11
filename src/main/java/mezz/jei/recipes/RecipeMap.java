package mezz.jei.recipes;

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
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.ingredients.IngredientUtil;

/**
 * A RecipeMap efficiently links IRecipeWrappers, IRecipeCategory, and Ingredients.
 */
public class RecipeMap {
	private final Table<IRecipeCategory, String, List<IRecipeWrapper>> recipeWrapperTable = HashBasedTable.create();
	private final ArrayListMultimap<String, String> categoryUidMap = ArrayListMultimap.create();
	private final Ordering<String> recipeCategoryOrdering;
	private final IIngredientRegistry ingredientRegistry;

	public RecipeMap(final RecipeCategoryComparator recipeCategoryComparator, IIngredientRegistry ingredientRegistry) {
		this.recipeCategoryOrdering = Ordering.from(recipeCategoryComparator);
		this.ingredientRegistry = ingredientRegistry;
	}

	public <V> List<String> getRecipeCategories(V ingredient) {
		IIngredientHelper<V> ingredientHelper = ingredientRegistry.getIngredientHelper(ingredient);

		Set<String> recipeCategories = new HashSet<>();

		for (String key : IngredientUtil.getUniqueIdsWithWildcard(ingredientHelper, ingredient)) {
			recipeCategories.addAll(categoryUidMap.get(key));
		}

		return recipeCategoryOrdering.immutableSortedCopy(recipeCategories);
	}

	public <V> void addRecipeCategory(IRecipeCategory recipeCategory, V ingredient) {
		IIngredientHelper<V> ingredientHelper = ingredientRegistry.getIngredientHelper(ingredient);

		String key = ingredientHelper.getUniqueId(ingredient);
		List<String> recipeCategories = categoryUidMap.get(key);
		String recipeCategoryUid = recipeCategory.getUid();
		if (!recipeCategories.contains(recipeCategoryUid)) {
			recipeCategories.add(recipeCategoryUid);
		}
	}

	public <T extends IRecipeWrapper, V> ImmutableList<T> getRecipeWrappers(IRecipeCategory<T> recipeCategory, V ingredient) {
		IIngredientHelper<V> ingredientHelper = ingredientRegistry.getIngredientHelper(ingredient);

		//noinspection unchecked
		Map<String, List<T>> recipesForType = (Map<String, List<T>>) (Object) recipeWrapperTable.row(recipeCategory);

		ImmutableList.Builder<T> listBuilder = ImmutableList.builder();
		for (String key : IngredientUtil.getUniqueIdsWithWildcard(ingredientHelper, ingredient)) {
			List<T> recipes = recipesForType.get(key);
			if (recipes != null) {
				listBuilder.addAll(recipes);
			}
		}
		return listBuilder.build();
	}

	public <T extends IRecipeWrapper> void addRecipe(T recipeWrapper, IRecipeCategory<T> recipeCategory, Map<Class, List> ingredientsByType) {
		for (Map.Entry<Class, List> entry : ingredientsByType.entrySet()) {
			if (entry != null) {
				addRecipe(recipeWrapper, recipeCategory, entry.getKey(), entry.getValue());
			}
		}
	}

	private <T extends IRecipeWrapper, V> void addRecipe(T recipeWrapper, IRecipeCategory<T> recipeCategory, Class<V> ingredientClass, List<V> ingredients) {
		IIngredientHelper<V> ingredientHelper = ingredientRegistry.getIngredientHelper(ingredientClass);

		//noinspection unchecked
		Map<String, List<T>> recipesWrappersForType = (Map<String, List<T>>) (Object) recipeWrapperTable.row(recipeCategory);

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

			List<T> recipeWrappers = recipesWrappersForType.computeIfAbsent(key, k -> Lists.newArrayList());

			recipeWrappers.add(recipeWrapper);

			addRecipeCategory(recipeCategory, ingredient);
		}
	}

	public <T extends IRecipeWrapper> void removeRecipe(T recipeWrapper, IRecipeCategory<T> recipeCategory, Map<Class, List> ingredientsByType) {
		for (Map.Entry<Class, List> entry : ingredientsByType.entrySet()) {
			if (entry != null) {
				removeRecipe(recipeWrapper, recipeCategory, entry.getKey(), entry.getValue());
			}
		}
	}

	private <T extends IRecipeWrapper, V> void removeRecipe(T recipeWrapper, IRecipeCategory<T> recipeCategory, Class<V> ingredientClass, List<V> ingredients) {
		IIngredientHelper<V> ingredientHelper = ingredientRegistry.getIngredientHelper(ingredientClass);

		//noinspection unchecked
		Map<String, List<T>> recipesWrappersForType = (Map<String, List<T>>) (Object) recipeWrapperTable.row(recipeCategory);

		ingredients = ingredientHelper.expandSubtypes(ingredients);

		for (V ingredient : ingredients) {
			if (ingredient == null) {
				continue;
			}

			List<String> uniqueIdsWithWildcard = IngredientUtil.getUniqueIdsWithWildcard(ingredientHelper, ingredient);
			for (String key : uniqueIdsWithWildcard) {
				List<T> recipeWrappers = recipesWrappersForType.get(key);
				if (recipeWrappers != null) {
					recipeWrappers.remove(recipeWrapper);
					if (recipeWrappers.isEmpty()) {
						categoryUidMap.remove(key, recipeCategory.getUid());
						recipesWrappersForType.remove(key);
					}
				}
			}
		}
	}
}
