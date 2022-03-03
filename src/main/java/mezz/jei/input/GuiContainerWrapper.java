package mezz.jei.input;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.gui.GuiScreenHelper;
import mezz.jei.ingredients.RegisteredIngredients;

import mezz.jei.ingredients.TypedIngredient;
import mezz.jei.util.ImmutableRect2i;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.item.ItemStack;

import java.util.stream.Stream;

public class GuiContainerWrapper implements IRecipeFocusSource {
	private final RegisteredIngredients registeredIngredients;
	private final GuiScreenHelper guiScreenHelper;

	public GuiContainerWrapper(RegisteredIngredients registeredIngredients, GuiScreenHelper guiScreenHelper) {
		this.registeredIngredients = registeredIngredients;
		this.guiScreenHelper = guiScreenHelper;
	}

	@Override
	public Stream<IClickedIngredient<?>> getIngredientUnderMouse(double mouseX, double mouseY) {
		Screen guiScreen = Minecraft.getInstance().screen;
		if (!(guiScreen instanceof AbstractContainerScreen<?> guiContainer)) {
			return Stream.empty();
		}
		return Stream.concat(
			guiScreenHelper.getPluginsIngredientUnderMouse(guiContainer, mouseX, mouseY),
			getSlotIngredientUnderMouse(guiContainer)
		);
	}

	private Stream<IClickedIngredient<?>> getSlotIngredientUnderMouse(AbstractContainerScreen<?> guiContainer) {
		return Stream.ofNullable(guiContainer.getSlotUnderMouse())
			.flatMap(slot -> {
				ItemStack stack = slot.getItem();
				return TypedIngredient.createTyped(this.registeredIngredients, VanillaTypes.ITEM, stack)
					.map(typedIngredient -> {
						ImmutableRect2i slotArea = new ImmutableRect2i(slot.x, slot.y, 16, 16);
						return new ClickedIngredient<>(typedIngredient, slotArea, false, false);
					})
					.stream();
			});
	}
}
