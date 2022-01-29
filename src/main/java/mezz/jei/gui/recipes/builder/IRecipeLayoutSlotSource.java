package mezz.jei.gui.recipes.builder;

import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.gui.recipes.RecipeLayout;
import mezz.jei.ingredients.IngredientsForTypeMap;

public interface IRecipeLayoutSlotSource {
	RecipeIngredientRole getRole();
	IngredientsForTypeMap getIngredientsForTypeMap();
	<R> void setRecipeLayout(RecipeLayout<R> recipeLayout);
}
