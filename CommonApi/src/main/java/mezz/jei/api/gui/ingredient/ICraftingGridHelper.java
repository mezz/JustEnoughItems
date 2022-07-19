package mezz.jei.api.gui.ingredient;

import java.util.List;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import org.jetbrains.annotations.Nullable;

/**
 * Helps set crafting-grid-style layouts.
 * This places smaller recipes in the grid in a consistent way.
 *
 * This is passed to plugins that implement
 * {@link ICraftingCategoryExtension#setRecipe(IRecipeLayoutBuilder, ICraftingGridHelper, IFocusGroup)}
 * to help them override the default behavior.
 */
public interface ICraftingGridHelper {
	/**
	 * Create and place input ingredients onto the crafting grid in a consistent way.
	 * For shapeless recipes, use a width and height of 0.
	 *
	 * @since 11.0.2
	 */
	<T> List<IRecipeSlotBuilder> createAndSetInputs(IRecipeLayoutBuilder builder, IIngredientType<T> ingredientType, List<@Nullable List<@Nullable T>> inputs, int width, int height);

	/**
	 * Place input ingredients onto the slot builders in a consistent way.
	 * For shapeless recipes, use a width and height of 0.
	 *
	 * @since 9.3.2
	 */
	<T> void setInputs(List<IRecipeSlotBuilder> slotBuilders, IIngredientType<T> ingredientType, List<@Nullable List<@Nullable T>> inputs, int width, int height);

	/**
	 * Place output ingredients at the right location.
	 *
	 * @since 11.0.2
	 */
	<T> IRecipeSlotBuilder createAndSetOutputs(IRecipeLayoutBuilder builder, IIngredientType<T> ingredientType, @Nullable List<@Nullable T> outputs);

	/**
	 * Place input ingredients onto the crafting grid in a consistent way.
	 * For shapeless recipes, use a width and height of 0.
	 *
	 * @since 9.3.0
	 * @deprecated Use {@link #createAndSetInputs(IRecipeLayoutBuilder, IIngredientType, List, int, int)} instead.
	 */
	@Deprecated(since = "11.0.2", forRemoval = true)
	<T> void setInputs(IRecipeLayoutBuilder builder, IIngredientType<T> ingredientType, List<@Nullable List<@Nullable T>> inputs, int width, int height);

	/**
	 * Place output ingredients at the right location.
	 *
	 * @deprecated Use {@link #createAndSetOutputs(IRecipeLayoutBuilder, IIngredientType, List)} instead.
	 */
	@Deprecated(since = "11.0.2", forRemoval = true)
	<T> void setOutputs(IRecipeLayoutBuilder builder, IIngredientType<T> ingredientType, @Nullable List<@Nullable T> outputs);
}
