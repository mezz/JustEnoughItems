package mezz.jei.library.ingredients;

import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.library.gui.recipes.layout.builder.IRecipeLayoutSlotSource;

import java.util.stream.Stream;

public interface IIngredientSupplier {
	Stream<? extends IIngredientType<?>> getIngredientTypes(RecipeIngredientRole role);

	<T> Stream<T> getIngredientStream(IIngredientType<T> ingredientType, RecipeIngredientRole role);

	Stream<IRecipeLayoutSlotSource> getSlotStream(RecipeIngredientRole role);

}
