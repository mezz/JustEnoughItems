package mezz.jei.api.gui;

import java.util.List;

import mezz.jei.api.IGuiHelper;
import net.minecraft.item.ItemStack;

/**
 * Helps set crafting-grid-style {@link IGuiItemStackGroup}.
 * This places smaller recipes in the grid in a consistent way.
 * Get an instance from {@link IGuiHelper#createCraftingGridHelper(int, int)}.
 */
public interface ICraftingGridHelper {

	/**
	 * Place inputs ingredients onto the crafting grid in a consistent way.
	 *
	 * @since JEI 3.11.2
	 */
	void setInputStacks(IGuiItemStackGroup guiItemStacks, List<List<ItemStack>> input);

	/**
	 * Place inputs ingredients onto the crafting grid in a consistent way.
	 *
	 * @since JEI 3.11.2
	 */
	void setInputStacks(IGuiItemStackGroup guiItemStacks, List<List<ItemStack>> input, int width, int height);

	/**
	 * Place output ingredients onto the crafting grid in a consistent way.
	 */
	void setOutput(IGuiItemStackGroup guiItemStacks, List<ItemStack> output);

	/**
	 * Place inputs ingredients onto the crafting grid in a consistent way.
	 *
	 * @deprecated since JEI 3.11.2. Use {@link #setInputStacks(IGuiItemStackGroup, List)}
	 */
	@Deprecated
	void setInput(IGuiItemStackGroup guiItemStacks, List input);

	/**
	 * Place inputs ingredients onto the crafting grid in a consistent way.
	 *
	 * @deprecated since JEI 3.11.2. Use {@link #setInputStacks(IGuiItemStackGroup, List, int, int)}
	 */
	@Deprecated
	void setInput(IGuiItemStackGroup guiItemStacks, List input, int width, int height);

}
