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
			if (!stack.func_190926_b()) {
				return new ClickedIngredient<ItemStack>(stack);
			}
		}

		JeiRuntime runtime = Internal.getRuntime();
		if (runtime != null) {
			List<IAdvancedGuiHandler<?>> activeAdvancedGuiHandlers = runtime.getActiveAdvancedGuiHandlers(guiScreen);
			for (IAdvancedGuiHandler advancedGuiHandler : activeAdvancedGuiHandlers) {
				//noinspection unchecked
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
