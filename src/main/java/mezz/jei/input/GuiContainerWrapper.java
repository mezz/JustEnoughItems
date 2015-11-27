package mezz.jei.input;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;

import mezz.jei.gui.Focus;
import mezz.jei.gui.RecipesGui;

public class GuiContainerWrapper implements IShowsRecipeFocuses, IKeyable {

	private final GuiContainer guiContainer;
	private final RecipesGui recipesGui;

	public GuiContainerWrapper(GuiContainer guiContainer, RecipesGui recipesGui) {
		this.guiContainer = guiContainer;
		this.recipesGui = recipesGui;
	}

	@Nullable
	@Override
	public Focus getFocusUnderMouse(int mouseX, int mouseY) {
		if (!isOpen()) {
			return null;
		}
		Slot slotUnderMouse = guiContainer.getSlotUnderMouse();
		if (slotUnderMouse != null && slotUnderMouse.getHasStack()) {
			return new Focus(slotUnderMouse.getStack());
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
