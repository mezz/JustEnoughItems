package mezz.jei.input;

import mezz.jei.gui.Focus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;

import javax.annotation.Nullable;

public class GuiContainerWrapper implements IShowsRecipeFocuses {
	@Nullable
	@Override
	public Focus getFocusUnderMouse(int mouseX, int mouseY) {
		GuiScreen guiScreen = Minecraft.getMinecraft().currentScreen;
		if (!(guiScreen instanceof GuiContainer)) {
			return null;
		}
		GuiContainer guiContainer = (GuiContainer) guiScreen;
		Slot slotUnderMouse = guiContainer.getSlotUnderMouse();
		if (slotUnderMouse != null && slotUnderMouse.getHasStack()) {
			return new Focus(slotUnderMouse.getStack());
		}
		return null;
	}

	@Override
	public boolean canSetFocusWithMouse() {
		return false;
	}
}
