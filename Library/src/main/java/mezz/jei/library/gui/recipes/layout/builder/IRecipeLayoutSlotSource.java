package mezz.jei.library.gui.recipes.layout.builder;

import it.unimi.dsi.fastutil.ints.IntSet;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IIngredientVisibility;
import mezz.jei.library.gui.ingredients.RecipeSlots;

import java.util.stream.Stream;

public interface IRecipeLayoutSlotSource {
	RecipeIngredientRole getRole();
	void setRecipeSlots(RecipeSlots recipeSlots, IntSet focusMatches, IIngredientVisibility ingredientVisibility);
	<T> Stream<T> getIngredients(IIngredientType<T> ingredientType);
	Stream<IIngredientType<?>> getIngredientTypes();
	IntSet getMatches(IFocusGroup focuses);
	int getIngredientCount();
}
