package mezz.jei.input;

import mezz.jei.gui.GuiScreenHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public class GuiContainerWrapper implements IRecipeFocusSource {
	private final GuiScreenHelper guiScreenHelper;

	public GuiContainerWrapper(GuiScreenHelper guiScreenHelper) {
		this.guiScreenHelper = guiScreenHelper;
	}

	@Override
	public Optional<IClickedIngredient<?>> getIngredientUnderMouse(double mouseX, double mouseY) {
		Screen guiScreen = Minecraft.getInstance().screen;
		if (!(guiScreen instanceof AbstractContainerScreen<?> guiContainer)) {
			return Optional.empty();
		}
		return guiScreenHelper.getPluginsIngredientUnderMouse(guiContainer, mouseX, mouseY)
			.or(() -> {
				Slot slotUnderMouse = guiContainer.getSlotUnderMouse();
				if (slotUnderMouse != null) {
					ItemStack stack = slotUnderMouse.getItem();
					if (!stack.isEmpty()) {
						Rect2i slotArea = new Rect2i(slotUnderMouse.x, slotUnderMouse.y, 16, 16);
						return ClickedIngredient.create(stack, slotArea, false, false);
					}
				}
				return Optional.empty();
			});
	}
}
