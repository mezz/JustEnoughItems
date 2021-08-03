package mezz.jei.input;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import mezz.jei.gui.GuiScreenHelper;

public class GuiContainerWrapper implements IShowsRecipeFocuses {
	private final GuiScreenHelper guiScreenHelper;

	public GuiContainerWrapper(GuiScreenHelper guiScreenHelper) {
		this.guiScreenHelper = guiScreenHelper;
	}

	@Nullable
	@Override
	public IClickedIngredient<?> getIngredientUnderMouse(double mouseX, double mouseY) {
		Screen guiScreen = Minecraft.getInstance().screen;
		if (!(guiScreen instanceof AbstractContainerScreen)) {
			return null;
		}
		AbstractContainerScreen<?> guiContainer = (AbstractContainerScreen<?>) guiScreen;
		Slot slotUnderMouse = guiContainer.getSlotUnderMouse();
		if (slotUnderMouse != null) {
			ItemStack stack = slotUnderMouse.getItem();
			if (!stack.isEmpty()) {
				Rect2i slotArea = new Rect2i(slotUnderMouse.x, slotUnderMouse.y, 16, 16);
				return ClickedIngredient.create(stack, slotArea);
			}
		}
		return guiScreenHelper.getPluginsIngredientUnderMouse(guiContainer, mouseX, mouseY);
	}

	@Override
	public boolean canSetFocusWithMouse() {
		return false;
	}
}
