package mezz.jei.api.gui;

import javax.annotation.Nonnull;
import java.util.List;

import net.minecraft.item.ItemStack;

/**
 * Helps set crafting-grid-style GuiItemStacks.
 * Get an instance from IGuiHelper.
 */
public interface ICraftingGridHelper {

	void setInput(@Nonnull IGuiItemStacks guiItemStacks, @Nonnull List input);

	void setInput(@Nonnull IGuiItemStacks guiItemStacks, @Nonnull List input, int width, int height);

	void setOutput(@Nonnull IGuiItemStacks guiItemStacks, @Nonnull List<ItemStack> output);

}
