package mezz.jei.input;

import javax.annotation.Nullable;

import mezz.jei.gui.Focus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class GuiContainerWrapper implements IShowsRecipeFocuses {
	@Nullable
	@Override
	public Focus<?> getFocusUnderMouse(int mouseX, int mouseY) {
		GuiScreen guiScreen = Minecraft.getMinecraft().currentScreen;
		if (!(guiScreen instanceof GuiContainer)) {
			return null;
		}
		GuiContainer guiContainer = (GuiContainer) guiScreen;
		Slot slotUnderMouse = guiContainer.getSlotUnderMouse();
		if (slotUnderMouse != null && slotUnderMouse.getHasStack()) {
			return new Focus<ItemStack>(slotUnderMouse.getStack());
		}
		return null;
	}

	@Override
	public boolean canSetFocusWithMouse() {
		return false;
	}
}
