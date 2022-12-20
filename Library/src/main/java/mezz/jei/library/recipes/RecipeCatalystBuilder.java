package mezz.jei.library.recipes;

import com.google.common.collect.ImmutableListMultimap;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.library.recipes.collect.RecipeMap;

import java.util.List;

public class RecipeCatalystBuilder {
	private final ImmutableListMultimap.Builder<IRecipeCategory<?>, ITypedIngredient<?>> recipeCategoryCatalystsBuilder = ImmutableListMultimap.builder();
	private final IIngredientManager ingredientManager;
	private final RecipeMap recipeCatalystMap;

	public RecipeCatalystBuilder(IIngredientManager ingredientManager, RecipeMap recipeCatalystMap) {
		this.ingredientManager = ingredientManager;
		this.recipeCatalystMap = recipeCatalystMap;
	}

	public void addCategoryCatalysts(IRecipeCategory<?> recipeCategory, List<ITypedIngredient<?>> categoryCatalystIngredients) {
		recipeCategoryCatalystsBuilder.putAll(recipeCategory, categoryCatalystIngredients);
		for (ITypedIngredient<?> catalystIngredient : categoryCatalystIngredients) {
			addCategoryCatalyst(catalystIngredient, recipeCategory);
		}
	}

	private <T> void addCategoryCatalyst(ITypedIngredient<T> catalystIngredient, IRecipeCategory<?> recipeCategory) {
		IIngredientType<T> ingredientType = catalystIngredient.getType();
		T ingredient = catalystIngredient.getIngredient();
		IIngredientHelper<T> ingredientHelper = ingredientManager.getIngredientHelper(ingredientType);
		String ingredientUid = ingredientHelper.getUniqueId(ingredient, UidContext.Recipe);
		RecipeType<?> recipeType = recipeCategory.getRecipeType();
		recipeCatalystMap.addCatalystForCategory(recipeType, ingredientUid);
	}

	public ImmutableListMultimap<IRecipeCategory<?>, ITypedIngredient<?>> buildRecipeCategoryCatalysts() {
		return recipeCategoryCatalystsBuilder.build();
	}
}
