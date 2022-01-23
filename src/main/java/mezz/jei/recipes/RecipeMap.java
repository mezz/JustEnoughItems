package mezz.jei.recipes;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.collect.SetMultiMap;
import mezz.jei.ingredients.IngredientsForType;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A RecipeMap efficiently links recipes, IRecipeCategory, and Ingredients.
 */
public class RecipeMap {
	private final RecipeIngredientTable recipeTable = new RecipeIngredientTable();
	private final SetMultiMap<String, ResourceLocation> ingredientUidToCategoryMap = new SetMultiMap<>();
	private final Comparator<ResourceLocation> recipeCategoryUidComparator;
	private final IIngredientManager ingredientManager;

	public RecipeMap(Comparator<ResourceLocation> recipeCategoryUidComparator, IIngredientManager ingredientManager) {
		this.recipeCategoryUidComparator = recipeCategoryUidComparator;
		this.ingredientManager = ingredientManager;
	}

	public <V> List<ResourceLocation> getRecipeCategories(V ingredient) {
		IIngredientHelper<V> ingredientHelper = ingredientManager.getIngredientHelper(ingredient);

		String ingredientUid = ingredientHelper.getUniqueId(ingredient, UidContext.Recipe);
		Collection<ResourceLocation> recipeCategoryUids = ingredientUidToCategoryMap.get(ingredientUid);
		return recipeCategoryUids.stream()
			.sorted(recipeCategoryUidComparator)
			.toList();
	}

	public void addRecipeCategory(IRecipeCategory<?> recipeCategory, Set<String> ingredientUids) {
		ResourceLocation recipeCategoryUid = recipeCategory.getUid();
		for (String uid : ingredientUids) {
			ingredientUidToCategoryMap.put(uid, recipeCategoryUid);
		}
	}

	public <T, V> List<T> getRecipes(IRecipeCategory<T> recipeCategory, V ingredient) {
		IIngredientHelper<V> ingredientHelper = ingredientManager.getIngredientHelper(ingredient);
		String ingredientUniqueId = ingredientHelper.getUniqueId(ingredient, UidContext.Recipe);
		return recipeTable.get(recipeCategory, ingredientUniqueId);
	}

	public <T> void addRecipe(T recipe, IRecipeCategory<T> recipeCategory, List<IngredientsForType<?>> ingredientsByType) {
		for (IngredientsForType<?> ingredientsForType : ingredientsByType) {
			addRecipe(recipe, recipeCategory, ingredientsForType);
		}
	}

	private <T, V> void addRecipe(T recipe, IRecipeCategory<T> recipeCategory, IngredientsForType<V> ingredientsForType) {
		IIngredientType<V> ingredientType = ingredientsForType.getIngredientType();
		IIngredientHelper<V> ingredientHelper = ingredientManager.getIngredientHelper(ingredientType);
		List<List<V>> ingredients = ingredientsForType.getIngredients();

		Set<String> ingredientUids = ingredients.stream()
			.filter(Objects::nonNull)
			.flatMap(Collection::stream)
			.filter(Objects::nonNull)
			.filter(ingredientHelper::isValidIngredient)
			.map(i -> ingredientHelper.getUniqueId(i, UidContext.Recipe))
			.collect(Collectors.toSet());

		addRecipeCategory(recipeCategory, ingredientUids);
		recipeTable.add(recipe, recipeCategory, ingredientUids);
	}
}
