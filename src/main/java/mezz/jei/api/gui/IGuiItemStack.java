package mezz.jei.api.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

/**
 * An IGuiItemStack displays one or more ItemStacks.
 * Use them in your IRecipeGui, create an instance with the IGuiHelper.
 *
 * ItemStacks with wildcard metadata will be displayed as multiple ItemStacks.
 * Multiple ItemStacks will be displayed in rotation.
 */
public interface IGuiItemStack {

	void setItemStack(ItemStack itemStack);

	/**
	 *  If itemStacks contains focusStack, the focusStack will be the only one displayed.
	 *  Useful when displaying all the recipes that include the focusStack.
	 */
	void setItemStacks(Iterable itemStacks, ItemStack focusStack);
	void setItemStacks(Object itemStacks, ItemStack focusStack);

	void clearItemStacks();

	boolean isMouseOver(int mouseX, int mouseY);
	ItemStack getItemStack();

	void draw(Minecraft minecraft);
	void drawHovered(Minecraft minecraft, int mouseX, int mouseY);
}
