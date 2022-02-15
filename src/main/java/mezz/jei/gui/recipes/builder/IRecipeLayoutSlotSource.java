package mezz.jei.gui.recipes.builder;

import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.gui.recipes.RecipeLayout;

import java.util.stream.Stream;

public interface IRecipeLayoutSlotSource {
	RecipeIngredientRole getRole();
	<R> void setRecipeLayout(RecipeLayout<R> recipeLayout, IFocusGroup focuses);
	<T> Stream<T> getIngredients(IIngredientType<T> ingredientType);
	Stream<IIngredientType<?>> getIngredientTypes();
}
