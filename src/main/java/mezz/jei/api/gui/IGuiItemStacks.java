package mezz.jei.api.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * IGuiItemStacks helps with displaying ItemStacks in a gui.
 * To use one in your IRecipeGui, create an instance with the IGuiHelper.
 *
 * Multiple ItemStacks will be displayed in rotation.
 * ItemStacks with subtypes and wildcard metadata will be displayed as multiple ItemStacks.
 */
public interface IGuiItemStacks {

	/**
	 * ItemStacks must be initialized once, and then can be set many times.
	 * Default padding is 1.
	 */
	void initItemStack(int index, int xPosition, int yPosition);
	void initItemStack(int index, int xPosition, int yPosition, int padding);

	/**
	 *  If itemStacks contains focusStack, the focusStack will be the only one displayed.
	 *  Useful when displaying all the recipes that include the focusStack.
	 */
	void setItemStack(int index, @Nonnull Iterable<ItemStack> itemStacks, @Nullable ItemStack focusStack);
	void setItemStack(int index, @Nonnull ItemStack itemStack, @Nullable ItemStack focusStack);

	void clearItemStacks();

	@Nullable
	ItemStack getStackUnderMouse(int mouseX, int mouseY);

	void draw(@Nonnull Minecraft minecraft, int mouseX, int mouseY);
}
