package mezz.jei.input;

import javax.annotation.Nullable;
import java.util.List;

import mezz.jei.Internal;
import mezz.jei.JeiRuntime;
import mezz.jei.api.gui.IAdvancedGuiHandler;
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
				return new ClickedIngredient<ItemStack>(stack);
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
				if (clicked != null) {
					return new ClickedIngredient<Object>(clicked);
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
