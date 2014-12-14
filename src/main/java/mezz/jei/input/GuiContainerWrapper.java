package mezz.jei.input;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import mezz.jei.gui.RecipesGui;

public class GuiContainerWrapper implements IShowsItemStacks, IKeyable {

	private final GuiContainer guiContainer;
	private final RecipesGui recipesGui;

	public GuiContainerWrapper(GuiContainer guiContainer, RecipesGui recipesGui) {
		this.guiContainer = guiContainer;
		this.recipesGui = recipesGui;
	}

	@Nullable
	@Override
	public ItemStack getStackUnderMouse(int mouseX, int mouseY) {
		Slot theSlot = guiContainer.theSlot;
		if (theSlot != null && theSlot.getHasStack()) {
			return theSlot.getStack();
		}
		return null;
	}

	@Override
	public boolean hasKeyboardFocus() {
		return false;
	}

	@Override
	public void setKeyboardFocus(boolean keyboardFocus) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean onKeyPressed(int keyCode) {
		return false;
	}

	@Override
	public void open() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void close() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isOpen() {
		return (guiContainer == Minecraft.getMinecraft().currentScreen) && !recipesGui.isOpen();
	}
}
