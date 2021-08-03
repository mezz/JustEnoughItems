package mezz.jei.recipes;

import java.util.List;

import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.resources.ResourceLocation;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMultimap;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.ingredients.IngredientManager;

public class RecipeCatalystBuilder {
	private final ImmutableListMultimap.Builder<IRecipeCategory<?>, Object> recipeCatalystsBuilder = ImmutableListMultimap.builder();
	private final ImmutableMultimap.Builder<String, ResourceLocation> categoriesForRecipeCatalystKeysBuilder = ImmutableMultimap.builder();
	private final IngredientManager ingredientManager;

	public RecipeCatalystBuilder(IngredientManager ingredientManager) {
		this.ingredientManager = ingredientManager;
	}

	public void addCatalysts(IRecipeCategory<?> recipeCategory, List<Object> catalystIngredients, RecipeMap recipeInputMap) {
		recipeCatalystsBuilder.putAll(recipeCategory, catalystIngredients);
		for (Object catalystIngredient : catalystIngredients) {
			addCatalyst(catalystIngredient, recipeCategory, recipeInputMap);
		}
	}

	private <T> void addCatalyst(T catalystIngredient, IRecipeCategory<?> recipeCategory, RecipeMap recipeInputMap) {
		IIngredientType<T> ingredientType = ingredientManager.getIngredientType(catalystIngredient);
		IIngredientHelper<T> ingredientHelper = ingredientManager.getIngredientHelper(ingredientType);
		recipeInputMap.addRecipeCategory(recipeCategory, catalystIngredient, ingredientHelper);
		String catalystIngredientKey = getUniqueId(catalystIngredient, ingredientManager);
		categoriesForRecipeCatalystKeysBuilder.put(catalystIngredientKey, recipeCategory.getUid());
	}

	private static <T> String getUniqueId(T ingredient, IIngredientManager ingredientManager) {
		IIngredientHelper<T> ingredientHelper = ingredientManager.getIngredientHelper(ingredient);
		return ingredientHelper.getUniqueId(ingredient, UidContext.Recipe);
	}

	public ImmutableListMultimap<IRecipeCategory<?>, Object> buildRecipeCatalysts() {
		return recipeCatalystsBuilder.build();
	}

	public ImmutableMultimap<String, ResourceLocation> buildCategoriesForRecipeCatalystKeys() {
		return categoriesForRecipeCatalystKeysBuilder.build();
	}
}
