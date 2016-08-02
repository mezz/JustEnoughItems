package mezz.jei.api.gui;

import javax.annotation.Nonnull;
import java.util.List;

import mezz.jei.api.IGuiHelper;
import net.minecraft.item.ItemStack;

/**
 * Helps set crafting-grid-style {@link IGuiItemStackGroup}.
 * This places smaller recipes in the grid in a consistent way.
 * Get an instance from {@link IGuiHelper#createCraftingGridHelper(int, int)}.
 */
public interface ICraftingGridHelper {

	void setInput(@Nonnull IGuiItemStackGroup guiItemStacks, @Nonnull List input);

	void setInput(@Nonnull IGuiItemStackGroup guiItemStacks, @Nonnull List input, int width, int height);

	void setOutput(@Nonnull IGuiItemStackGroup guiItemStacks, @Nonnull List<ItemStack> output);

}
