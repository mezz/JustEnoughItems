package mezz.jei.common.input;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.common.gui.GuiScreenHelper;
import mezz.jei.common.ingredients.RegisteredIngredients;
import mezz.jei.common.ingredients.TypedIngredient;
import mezz.jei.common.platform.IPlatformScreenHelper;
import mezz.jei.common.platform.Services;
import mezz.jei.common.util.ImmutableRect2i;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;
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
		if (guiScreen == null) {
			return Stream.empty();
		}
		return Stream.concat(
			guiScreenHelper.getPluginsIngredientUnderMouse(guiScreen, mouseX, mouseY),
			getSlotIngredientUnderMouse(guiScreen).stream()
		);
	}

	private Optional<IClickedIngredient<?>> getSlotIngredientUnderMouse(Screen guiScreen) {
		if (!(guiScreen instanceof AbstractContainerScreen<?> guiContainer)) {
			return Optional.empty();
		}
		IPlatformScreenHelper screenHelper = Services.PLATFORM.getScreenHelper();
		return Optional.ofNullable(screenHelper.getSlotUnderMouse(guiContainer))
			.flatMap(slot -> getClickedIngredient(slot, guiContainer));
	}

	private Optional<IClickedIngredient<?>> getClickedIngredient(Slot slot, AbstractContainerScreen<?> guiContainer) {
		ItemStack stack = slot.getItem();
		return TypedIngredient.createTyped(this.registeredIngredients, VanillaTypes.ITEM_STACK, stack)
			.map(typedIngredient -> {
				IPlatformScreenHelper screenHelper = Services.PLATFORM.getScreenHelper();
				ImmutableRect2i slotArea = new ImmutableRect2i(
					screenHelper.getGuiLeft(guiContainer) + slot.x,
					screenHelper.getGuiTop(guiContainer) + slot.y,
					16,
					16
				);
				return new ClickedIngredient<>(typedIngredient, slotArea, false, false);
			});
	}
}
