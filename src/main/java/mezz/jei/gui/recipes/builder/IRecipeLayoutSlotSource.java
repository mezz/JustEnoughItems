package mezz.jei.gui.recipes.builder;

import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.gui.Focus;
import mezz.jei.gui.recipes.RecipeLayout;

import java.util.List;
import java.util.stream.Stream;

public interface IRecipeLayoutSlotSource {
	RecipeIngredientRole getRole();
	<R> void setRecipeLayout(RecipeLayout<R> recipeLayout, List<Focus<?>> focuses);
	<T> Stream<T> getIngredients(IIngredientType<T> ingredientType);
	Stream<IIngredientType<?>> getIngredientTypes();
}
