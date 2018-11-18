package mezz.jei.input;

import javax.annotation.Nullable;
import java.awt.Rectangle;
import java.util.List;

import mezz.jei.Internal;
import mezz.jei.api.gui.IAdvancedGuiHandler;
import mezz.jei.runtime.JeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class GuiContainerWrapper implements IShowsRecipeFocuses {
	@Nullable
	@Override
	public IClickedIngredient<?> getIngredientUnderMouse(int mouseX, int mouseY) {
		GuiScreen guiScreen = Minecraft.getMinecraft().currentScreen;
		if (!(guiScreen instanceof GuiContainer)) {
			return null;
		}
		GuiContainer guiContainer = (GuiContainer) guiScreen;
		Slot slotUnderMouse = guiContainer.getSlotUnderMouse();
		if (slotUnderMouse != null) {
			ItemStack stack = slotUnderMouse.getStack();
			if (!stack.isEmpty()) {
				Rectangle slotArea = new Rectangle(slotUnderMouse.xPos, slotUnderMouse.yPos, 16, 16);
				return ClickedIngredient.create(stack, slotArea);
			}
		}
		return getAdvancedGuiHandlerIngredientUnderMouse(guiContainer, mouseX, mouseY);
	}

	@Nullable
	private <T extends GuiContainer> IClickedIngredient<?> getAdvancedGuiHandlerIngredientUnderMouse(T guiContainer, int mouseX, int mouseY) {
		JeiRuntime runtime = Internal.getRuntime();
		if (runtime != null) {
			List<IAdvancedGuiHandler<T>> activeAdvancedGuiHandlers = runtime.getActiveAdvancedGuiHandlers(guiContainer);
			for (IAdvancedGuiHandler<T> advancedGuiHandler : activeAdvancedGuiHandlers) {
				Object clicked = advancedGuiHandler.getIngredientUnderMouse(guiContainer, mouseX, mouseY);
				if (clicked != null && Internal.getIngredientRegistry().isValidIngredient(clicked)) {
					Rectangle area = null;
					Slot slotUnderMouse = guiContainer.getSlotUnderMouse();
					if (clicked instanceof ItemStack && slotUnderMouse != null && ItemStack.areItemStacksEqual(slotUnderMouse.getStack(), (ItemStack) clicked)) {
						area = new Rectangle(slotUnderMouse.xPos, slotUnderMouse.yPos, 16, 16);
					}
					return ClickedIngredient.create(clicked, area);
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
