package mezz.jei.gui;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

import mezz.jei.api.gui.IGuiItemStacks;

public class GuiItemStacks implements IGuiItemStacks {

	@Nonnull
	private final Map<Integer, GuiItemStack> guiItemStacks = new HashMap<>();
	private ItemStack focusStack;

	public void init(int index, int xPosition, int yPosition) {
		init(index, xPosition, yPosition, 1);
	}

	public void init(int index, int xPosition, int yPosition, int padding) {
		GuiItemStack guiItemStack = new GuiItemStack(xPosition, yPosition, padding);
		guiItemStacks.put(index, guiItemStack);
	}

	/**
	 * If focusStack is set and any of the guiItemStacks contains focusStack,
	 * they will only display focusStack instead of rotating through all their values.
	 */
	public void setFocusStack(@Nullable ItemStack focusStack) {
		this.focusStack = focusStack;
	}

	public void set(int index, @Nonnull Collection<ItemStack> itemStacks) {
		guiItemStacks.get(index).set(itemStacks, focusStack);
	}

	public void set(int index, @Nonnull ItemStack itemStack) {
		guiItemStacks.get(index).set(itemStack, focusStack);
	}

	public void clearItemStacks() {
		for (GuiItemStack guiItemStack : guiItemStacks.values()) {
			guiItemStack.clear();
		}
	}

	@Nullable
	public ItemStack getStackUnderMouse(int mouseX, int mouseY) {
		for (GuiItemStack item : guiItemStacks.values()) {
			if (item != null && item.isMouseOver(mouseX, mouseY)) {
				return item.get();
			}
		}
		return null;
	}

	public void draw(@Nonnull Minecraft minecraft, int mouseX, int mouseY) {
		GuiItemStack hovered = null;
		for (GuiItemStack item : guiItemStacks.values()) {
			if (hovered == null && item.isMouseOver(mouseX, mouseY)) {
				hovered = item;
			} else {
				item.draw(minecraft);
			}
		}
		if (hovered != null) {
			hovered.drawHovered(minecraft, mouseX, mouseY);
		}
	}
}
