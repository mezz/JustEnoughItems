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
	 * Place input ingredients onto the crafting grid in a consistent way.
	 * For shapeless recipes, use a width and height of 0.
	 *
	 * @since 9.3.0
	 */
	<T> void setInputs(IRecipeLayoutBuilder builder, IIngredientType<T> ingredientType, List<@Nullable List<@Nullable T>> inputs, int width, int height);

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
	 * @since 9.3.0
	 */
	<T> void setOutputs(IRecipeLayoutBuilder builder, IIngredientType<T> ingredientType, @Nullable List<@Nullable T> outputs);

	/**
	 * Place inputs ingredients onto the crafting grid in a consistent way.
	 *
	 * @deprecated Use {@link #setInputs(IRecipeLayoutBuilder, IIngredientType, List, int, int)} instead.
	 */
	@SuppressWarnings("removal")
	@Deprecated(forRemoval = true, since = "9.3.0")
	<T> void setInputs(IGuiIngredientGroup<T> ingredientGroup, List<List<T>> inputs);

	/**
	 * Place inputs ingredients onto the crafting grid in a consistent way.
	 *
	 * @deprecated Use {@link #setInputs(IRecipeLayoutBuilder, IIngredientType, List, int, int)} instead.
	 */
	@SuppressWarnings("removal")
	@Deprecated(forRemoval = true, since = "9.3.0")
	<T> void setInputs(IGuiIngredientGroup<T> ingredientGroup, List<List<T>> inputs, int width, int height);
}
