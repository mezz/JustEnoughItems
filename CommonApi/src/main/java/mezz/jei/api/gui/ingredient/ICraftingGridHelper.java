package mezz.jei.api.gui.ingredient;

import java.util.List;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import net.minecraft.world.item.ItemStack;
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
	 * @see #createAndSetInputs(IRecipeLayoutBuilder, IIngredientType, List, int, int) to set other ingredient types.
	 * @since 11.1.1
	 */
	default List<IRecipeSlotBuilder> createAndSetInputs(IRecipeLayoutBuilder builder, List<@Nullable List<@Nullable ItemStack>> inputs, int width, int height) {
		return createAndSetInputs(builder, VanillaTypes.ITEM_STACK, inputs, width, height);
	}

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
	 * Place output ItemStacks at the right location.
	 *
	 * @see #createAndSetOutputs(IRecipeLayoutBuilder, IIngredientType, List) to set other ingredient types.
	 * @since 11.1.1
	 */
	default IRecipeSlotBuilder createAndSetOutputs(IRecipeLayoutBuilder builder, @Nullable List<@Nullable ItemStack> outputs) {
		return createAndSetOutputs(builder, VanillaTypes.ITEM_STACK, outputs);
	}

	/**
	 * Place output ingredients at the right location.
	 *
	 * @since 11.0.2
	 */
	<T> IRecipeSlotBuilder createAndSetOutputs(IRecipeLayoutBuilder builder, IIngredientType<T> ingredientType, @Nullable List<@Nullable T> outputs);
}
