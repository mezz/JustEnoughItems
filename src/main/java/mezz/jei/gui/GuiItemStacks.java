package mezz.jei.gui;

import mezz.jei.api.gui.IGuiItemStack;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;

public class GuiItemStacks {

	@Nonnull
	private final ArrayList<IGuiItemStack> guiItemStacks = new ArrayList<IGuiItemStack>();

	public void addItem(@Nonnull IGuiItemStack guiItemStack) {
		guiItemStacks.add(guiItemStack);
	}

	public void setItems(int index, @Nonnull Iterable<ItemStack> itemStacks, @Nullable ItemStack focusStack) {
		guiItemStacks.get(index).setItemStacks(itemStacks, focusStack);
	}

	public void setItem(int index, @Nonnull ItemStack item) {
		guiItemStacks.get(index).setItemStack(item);
	}

	public void clear() {
		for (IGuiItemStack guiItemStack : guiItemStacks) {
			guiItemStack.clearItemStacks();
		}
	}

	@Nullable
	public ItemStack getStackUnderMouse(int mouseX, int mouseY) {
		for (IGuiItemStack item : guiItemStacks) {
			if (item != null && item.isMouseOver(mouseX, mouseY))
				return item.getItemStack();
		}
		return null;
	}

	public void draw(@Nonnull Minecraft minecraft, int mouseX, int mouseY) {
		IGuiItemStack hovered = null;
		for (IGuiItemStack item : guiItemStacks) {
			if (hovered == null && item.isMouseOver(mouseX, mouseY))
				hovered = item;
			else
				item.draw(minecraft);
		}
		if (hovered != null)
			hovered.drawHovered(minecraft, mouseX, mouseY);
	}
}
