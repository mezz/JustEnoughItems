package mezz.jei.api.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;

/**
 * An IGuiItemStack displays one or more ItemStacks.
 * Use them in your IRecipeGui, create an instance with the IGuiHelper.
 *
 * ItemStacks with wildcard metadata will be displayed as multiple ItemStacks.
 * Multiple ItemStacks will be displayed in rotation.
 */
public interface IGuiItemStack {

	/**
	 *  If itemStacks contains focusStack, the focusStack will be the only one displayed.
	 *  Useful when displaying all the recipes that include the focusStack.
	 */
	void setItemStacks(Iterable<ItemStack> itemStacks, @Nullable ItemStack focusStack);
	void setItemStack(ItemStack itemStack);

	void clearItemStacks();

	/**
	 *  Returns the item stack to be displayed.
	 *  Return value may be null or time-dependent.
	 */
	@Nullable
	ItemStack getItemStack();

	boolean isMouseOver(int mouseX, int mouseY);

	void draw(Minecraft minecraft);
	void drawHovered(Minecraft minecraft, int mouseX, int mouseY);
}
