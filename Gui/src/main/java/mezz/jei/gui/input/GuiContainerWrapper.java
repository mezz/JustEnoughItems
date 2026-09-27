package mezz.jei.gui.input;

import mezz.jei.api.gui.handlers.IGuiProperties;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IScreenHelper;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.common.util.MathUtil;
import mezz.jei.gui.overlay.elements.IElement;
import mezz.jei.gui.overlay.elements.IngredientElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

import java.util.stream.Stream;

public class GuiContainerWrapper implements IRecipeFocusSource {
	private final IScreenHelper screenHelper;

	public GuiContainerWrapper(IScreenHelper screenHelper) {
		this.screenHelper = screenHelper;
	}

	@Override
	public Stream<IClickableIngredientInternal<?>> getIngredientUnderMouse(double mouseX, double mouseY) {
		Screen guiScreen = Minecraft.getInstance().gui.screen();
		if (guiScreen == null) {
			return Stream.empty();
		}
		return screenHelper.getClickableIngredientUnderMouse(guiScreen, mouseX, mouseY)
			.map(clickableSlot -> {
				ITypedIngredient<?> typedIngredient = clickableSlot.getTypedIngredient();
				ImmutableRect2i area = new ImmutableRect2i(clickableSlot.getArea());
				IElement<?> element = new IngredientElement<>(typedIngredient);
				return new ClickableIngredientInternal<>(element, area::contains, false, false);
			});
	}

	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		Screen guiScreen = Minecraft.getInstance().gui.screen();
		if (guiScreen == null) {
			return false;
		}
		IGuiProperties guiProperties = screenHelper.getGuiProperties(guiScreen).orElse(null);
		if (guiProperties != null && MathUtil.contains(guiProperties, mouseX, mouseY)) {
			return true;
		}
		return screenHelper.getGuiExclusionAreas(guiScreen)
			.anyMatch(area -> MathUtil.contains(area, mouseX, mouseY));
	}

	@Override
	public Stream<IDraggableIngredientInternal<?>> getDraggableIngredientUnderMouse(double mouseX, double mouseY) {
		return Stream.empty();
	}
}
