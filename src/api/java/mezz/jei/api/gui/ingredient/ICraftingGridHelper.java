package mezz.jei.api.gui.ingredient;

import java.util.List;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;

/**
 * Helps set crafting-grid-style {@link IGuiIngredientGroup}.
 * This places smaller recipes in the grid in a consistent way.
 *
 * This is passed to plugins that implement
 * {@link ICraftingCategoryExtension#setRecipe(IRecipeLayoutBuilder, ICraftingGridHelper, List)}
 * to help them override the default behavior.
 */
public interface ICraftingGridHelper {
	/**
	 * Place input ingredients onto the crafting grid in a consistent way.
	 * For shapeless recipes, use a width and height of 0.
	 *
	 * @since JEI 9.3.0
	 */
	<T> void setInputs(IRecipeLayoutBuilder builder, IIngredientType<T> ingredientType, List<List<T>> inputs, int width, int height);

	/**
	 * Place output ingredients at the right location.
	 *
	 * @since JEI 9.3.0
	 */
	<T> void setOutputs(IRecipeLayoutBuilder builder, IIngredientType<T> ingredientType, List<T> outputs);

	/**
	 * Place inputs ingredients onto the crafting grid in a consistent way.
	 *
	 * @deprecated since JEI 9.3.0.
	 * Use {@link #setInputs(IRecipeLayoutBuilder, IIngredientType, List, int, int)} instead.
	 */
	@Deprecated
	<T> void setInputs(IGuiIngredientGroup<T> ingredientGroup, List<List<T>> inputs);

	/**
	 * Place inputs ingredients onto the crafting grid in a consistent way.
	 */
	@Deprecated
	<T> void setInputs(IGuiIngredientGroup<T> ingredientGroup, List<List<T>> inputs, int width, int height);
}
