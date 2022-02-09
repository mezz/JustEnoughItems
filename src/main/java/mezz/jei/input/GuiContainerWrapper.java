package mezz.jei.input;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.gui.GuiScreenHelper;
import mezz.jei.ingredients.TypedIngredient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public class GuiContainerWrapper implements IRecipeFocusSource {
	private final IIngredientManager ingredientManager;
	private final GuiScreenHelper guiScreenHelper;

	public GuiContainerWrapper(IIngredientManager ingredientManager, GuiScreenHelper guiScreenHelper) {
		this.ingredientManager = ingredientManager;
		this.guiScreenHelper = guiScreenHelper;
	}

	@Override
	public Optional<IClickedIngredient<?>> getIngredientUnderMouse(double mouseX, double mouseY) {
		Screen guiScreen = Minecraft.getInstance().screen;
		if (!(guiScreen instanceof AbstractContainerScreen<?> guiContainer)) {
			return Optional.empty();
		}
		return guiScreenHelper.getPluginsIngredientUnderMouse(guiContainer, mouseX, mouseY)
			.or(() -> getSlotIngredientUnderMouse(guiContainer));
	}

	private Optional<IClickedIngredient<?>> getSlotIngredientUnderMouse(AbstractContainerScreen<?> guiContainer) {
		return Optional.ofNullable(guiContainer.getSlotUnderMouse())
			.flatMap(slot -> {
				ItemStack stack = slot.getItem();
				return TypedIngredient.createTyped(this.ingredientManager, VanillaTypes.ITEM, stack)
					.map(typedIngredient -> {
						Rect2i slotArea = new Rect2i(slot.x, slot.y, 16, 16);
						return new ClickedIngredient<>(typedIngredient, slotArea, false, false);
					});
			});
	}
}
