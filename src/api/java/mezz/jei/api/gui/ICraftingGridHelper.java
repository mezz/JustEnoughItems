package mezz.jei.api.gui;

import java.util.List;

import net.minecraft.item.ItemStack;

import mezz.jei.api.IGuiHelper;

/**
 * Helps set crafting-grid-style {@link IGuiIngredientGroup}.
 * This places smaller recipes in the grid in a consistent way.
 * Get an instance from {@link IGuiHelper#createCraftingGridHelper(int, int)}.
 */
public interface ICraftingGridHelper {

	/**
	 * Place inputs ingredients onto the crafting grid in a consistent way.
	 *
	 * @since JEI 4.0.2
	 */
	<T> void setInputs(IGuiIngredientGroup<T> ingredientGroup, List<List<T>> inputs);

	/**
	 * Place inputs ingredients onto the crafting grid in a consistent way.
	 *
	 * @since JEI 4.0.2
	 */
	<T> void setInputs(IGuiIngredientGroup<T> ingredientGroup, List<List<T>> inputs, int width, int height);

	/**
	 * Place inputs ingredients onto the crafting grid in a consistent way.
	 *
	 * @since JEI 3.11.2
	 * @deprecated since JEI 4.0.2, use {@link #setInputs(IGuiIngredientGroup, List)}
	 */
	@Deprecated
	void setInputStacks(IGuiItemStackGroup guiItemStacks, List<List<ItemStack>> input);

	/**
	 * Place inputs ingredients onto the crafting grid in a consistent way.
	 *
	 * @since JEI 3.11.2
	 * @deprecated since JEI 4.0.2, use {@link #setInputs(IGuiIngredientGroup, List, int, int)}
	 */
	@Deprecated
	void setInputStacks(IGuiItemStackGroup guiItemStacks, List<List<ItemStack>> input, int width, int height);

	/**
	 * Place output ingredients onto the crafting grid in a consistent way.
	 *
	 * @deprecated since JEI 4.0.2, use {@link IGuiItemStackGroup#set(int, List)}
	 */
	@Deprecated
	void setOutput(IGuiItemStackGroup guiItemStacks, List<ItemStack> output);

}
