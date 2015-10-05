package mezz.jei.api.gui;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;

/**
 * IGuiItemStacks displays ItemStacks in a gui.
 *
 * Multiple ItemStacks will be displayed in rotation.
 * ItemStacks with subtypes and wildcard metadata will be displayed as multiple ItemStacks.
 */
public interface IGuiItemStacks {

	/**
	 * ItemStacks must be initialized once, and then can be set many times.
	 */
	void initItemStack(int index, int xPosition, int yPosition);

	void setItemStack(int index, @Nonnull Iterable<ItemStack> itemStacks);

	void setItemStack(int index, @Nonnull ItemStack itemStack);

}
