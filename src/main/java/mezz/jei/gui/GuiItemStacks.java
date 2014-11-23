package mezz.jei.gui;

import mezz.jei.api.gui.IGuiItemStacks;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class GuiItemStacks implements IGuiItemStacks {

	@Nonnull
	private final Map<Integer, GuiItemStack> guiItemStacks = new HashMap<Integer, GuiItemStack>();

	public void initItemStack(int index, int xPosition, int yPosition) {
		initItemStack(index, xPosition, yPosition, 1);
	}

	public void initItemStack(int index, int xPosition, int yPosition, int padding) {
		GuiItemStack guiItemStack = new GuiItemStack(xPosition, yPosition, padding);
		guiItemStacks.put(index, guiItemStack);
	}

	public void setItemStack(int index, @Nonnull Iterable<ItemStack> itemStacks, @Nullable ItemStack focusStack) {
		guiItemStacks.get(index).setItemStacks(itemStacks, focusStack);
	}

	public void setItemStack(int index, @Nonnull ItemStack itemStack) {
		guiItemStacks.get(index).setItemStack(itemStack);
	}

	public void clearItemStacks() {
		for (GuiItemStack guiItemStack : guiItemStacks.values()) {
			guiItemStack.clearItemStacks();
		}
	}

	@Nullable
	public ItemStack getStackUnderMouse(int mouseX, int mouseY) {
		for (GuiItemStack item : guiItemStacks.values()) {
			if (item != null && item.isMouseOver(mouseX, mouseY))
				return item.getItemStack();
		}
		return null;
	}

	public void draw(@Nonnull Minecraft minecraft, int mouseX, int mouseY) {
		GuiItemStack hovered = null;
		for (GuiItemStack item : guiItemStacks.values()) {
			if (hovered == null && item.isMouseOver(mouseX, mouseY))
				hovered = item;
			else
				item.draw(minecraft);
		}
		if (hovered != null)
			hovered.drawHovered(minecraft, mouseX, mouseY);
	}
}
