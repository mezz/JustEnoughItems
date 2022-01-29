package mezz.jei.recipes;

import java.util.List;

import mezz.jei.api.ingredients.subtypes.UidContext;

import com.google.common.collect.ImmutableListMultimap;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.ingredients.IngredientManager;

public class RecipeCatalystBuilder {
	private final ImmutableListMultimap.Builder<IRecipeCategory<?>, Object> recipeCategoryCatalystsBuilder = ImmutableListMultimap.builder();
	private final IngredientManager ingredientManager;
	private final RecipeMap recipeCatalystMap;

	public RecipeCatalystBuilder(IngredientManager ingredientManager, RecipeMap recipeCatalystMap) {
		this.ingredientManager = ingredientManager;
		this.recipeCatalystMap = recipeCatalystMap;
	}

	public void addCategoryCatalysts(IRecipeCategory<?> recipeCategory, List<Object> categoryCatalystIngredients) {
		recipeCategoryCatalystsBuilder.putAll(recipeCategory, categoryCatalystIngredients);
		for (Object catalystIngredient : categoryCatalystIngredients) {
			addCategoryCatalyst(catalystIngredient, recipeCategory);
		}
	}

	private <T> void addCategoryCatalyst(T catalystIngredient, IRecipeCategory<?> recipeCategory) {
		IIngredientType<T> ingredientType = ingredientManager.getIngredientType(catalystIngredient);
		IIngredientHelper<T> ingredientHelper = ingredientManager.getIngredientHelper(ingredientType);
		String ingredientUid = ingredientHelper.getUniqueId(catalystIngredient, UidContext.Recipe);
		recipeCatalystMap.addCatalystForCategory(recipeCategory, ingredientUid);
	}

	public ImmutableListMultimap<IRecipeCategory<?>, Object> buildRecipeCategoryCatalysts() {
		return recipeCategoryCatalystsBuilder.build();
	}
}
