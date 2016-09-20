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

	void setInput(IGuiItemStackGroup guiItemStacks, List input);

	void setInput(IGuiItemStackGroup guiItemStacks, List input, int width, int height);

	void setOutput(IGuiItemStackGroup guiItemStacks, List<ItemStack> output);

}
