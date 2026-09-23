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
	 * Gets the slot index for an input in the centered 3x3 crafting grid.
	 *
	 * @param inputIndex the input's index in the recipe's ingredient list, from zero to width * height - 1
	 * @param width the recipe width, from 1 to 3
	 * @param height the recipe height, from 1 to 3
	 * @return the grid index, from 0 to 8, without the helper's input-slot offset
	 * @since JEI 4.23.0
	 */
	default int getCraftingIndex(int inputIndex, int width, int height) {
		int index;
		if (width == 1) {
			if (height == 3) {
				index = (inputIndex * 3) + 1;
			} else if (height == 2) {
				index = (inputIndex * 3) + 1;
			} else {
				index = 4;
			}
		} else if (height == 1) {
			index = inputIndex + 3;
		} else if (width == 2) {
			index = inputIndex;
			if (inputIndex > 1) {
				index++;
				if (inputIndex > 3) {
					index++;
				}
			}
		} else if (height == 2) {
			index = inputIndex + 3;
		} else {
			index = inputIndex;
		}
		return index;
	}

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
