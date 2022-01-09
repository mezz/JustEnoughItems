package mezz.jei.input;

import javax.annotation.Nullable;
import java.awt.Rectangle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import mezz.jei.gui.GuiScreenHelper;

public class GuiContainerWrapper implements IShowsRecipeFocuses {
	private final GuiScreenHelper guiScreenHelper;

	public GuiContainerWrapper(GuiScreenHelper guiScreenHelper) {
		this.guiScreenHelper = guiScreenHelper;
	}

	@Nullable
	@Override
	public IClickedIngredient<?> getIngredientUnderMouse(int mouseX, int mouseY) {
		GuiScreen guiScreen = Minecraft.getMinecraft().currentScreen;
		if (guiScreen == null) {
			return null;
		}
		IClickedIngredient<?> pluginsIngredientUnderMouse = guiScreenHelper.getPluginsIngredientUnderMouse(guiScreen, mouseX, mouseY);
		if (pluginsIngredientUnderMouse != null) {
			return pluginsIngredientUnderMouse;
		}
		if (guiScreen instanceof GuiContainer) {
			GuiContainer guiContainer = (GuiContainer) guiScreen;
			Slot slotUnderMouse = guiContainer.getSlotUnderMouse();
			if (slotUnderMouse != null) {
				ItemStack stack = slotUnderMouse.getStack();
				if (!stack.isEmpty()) {
					Rectangle slotArea = new Rectangle(
						guiContainer.getGuiLeft() + slotUnderMouse.xPos,
						guiContainer.getGuiTop() + slotUnderMouse.yPos,
						16,
						16
					);
					return ClickedIngredient.create(stack, slotArea);
				}
			}
		}
		return null;
	}

	@Override
	public boolean canSetFocusWithMouse() {
		return false;
	}
}
