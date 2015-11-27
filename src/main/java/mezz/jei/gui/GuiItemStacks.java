package mezz.jei.gui;

import net.minecraft.item.ItemStack;

import mezz.jei.api.gui.IGuiItemStacks;

public class GuiItemStacks extends GuiWidgets<ItemStack, GuiItemStack> implements IGuiItemStacks {
	@Override
	public void init(int index, int xPosition, int yPosition) {
		init(index, xPosition, yPosition, 1);
	}

	public void init(int index, int xPosition, int yPosition, int padding) {
		GuiItemStack guiItemStack = new GuiItemStack(xPosition, yPosition, padding);
		guiWidgets.put(index, guiItemStack);
	}
}
