package mezz.jei.library.recipes;

import com.google.common.collect.ImmutableListMultimap;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.library.recipes.collect.RecipeIngredientRoleMap;

import java.util.List;

public class CraftingStationBuilder {
	private final ImmutableListMultimap.Builder<IRecipeCategory<?>, ITypedIngredient<?>> recipeCategoryCatalystsBuilder = ImmutableListMultimap.builder();
	private final RecipeIngredientRoleMap craftingStationMap;

	public CraftingStationBuilder(RecipeIngredientRoleMap craftingStationMap) {
		this.craftingStationMap = craftingStationMap;
	}

	public void addCategoryCatalysts(IRecipeCategory<?> recipeCategory, List<ITypedIngredient<?>> categoryCatalystIngredients) {
		recipeCategoryCatalystsBuilder.putAll(recipeCategory, categoryCatalystIngredients);
		for (ITypedIngredient<?> catalystIngredient : categoryCatalystIngredients) {
			addCategoryCatalyst(catalystIngredient, recipeCategory);
		}
	}

	private <T> void addCategoryCatalyst(ITypedIngredient<T> catalystIngredient, IRecipeCategory<?> recipeCategory) {
		IRecipeType<?> recipeType = recipeCategory.getRecipeType();
		craftingStationMap.addCraftingStationForCategory(recipeType, catalystIngredient);
	}

	public ImmutableListMultimap<IRecipeCategory<?>, ITypedIngredient<?>> build() {
		return recipeCategoryCatalystsBuilder.build();
	}
}
