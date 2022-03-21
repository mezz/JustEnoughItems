package mezz.jei.gui.recipes.builder;

import it.unimi.dsi.fastutil.ints.IntSet;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.gui.recipes.RecipeLayout;

import java.util.stream.Stream;

public interface IRecipeLayoutSlotSource {
	RecipeIngredientRole getRole();
	<R> void setRecipeLayout(RecipeLayout<R> recipeLayout, IntSet focusMatches);
	<T> Stream<T> getIngredients(IIngredientType<T> ingredientType);
	Stream<IIngredientType<?>> getIngredientTypes();
	IntSet getMatches(IFocusGroup focuses);
	int getIngredientCount();
}
