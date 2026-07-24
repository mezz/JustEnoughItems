package mezz.jei.gui.input;

import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IClickableIngredient;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IScreenHelper;
import mezz.jei.common.Internal;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.gui.overlay.elements.IElement;
import mezz.jei.gui.overlay.elements.IngredientElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

import java.util.Optional;
import java.util.stream.Stream;

public class GuiContainerWrapper implements IRecipeFocusSource {
	private final IScreenHelper screenHelper;

	public GuiContainerWrapper(IScreenHelper screenHelper) {
		this.screenHelper = screenHelper;
	}

	@Override
	public Stream<IClickableIngredientInternal<?>> getIngredientUnderMouse(double mouseX, double mouseY) {
		Screen guiScreen = Minecraft.getInstance().screen;
		if (guiScreen == null) {
			return Stream.empty();
		}
		return screenHelper.getClickableIngredientUnderMouse(guiScreen, mouseX, mouseY)
			.flatMap(clickableSlot -> {
				return createTypedIngredient(clickableSlot)
					.map(i -> {
						ImmutableRect2i area = new ImmutableRect2i(clickableSlot.getArea());
						IElement<?> element = new IngredientElement<>(i);
						return new ClickableIngredientInternal<>(element, area::contains, false, false);
					})
					.stream();
			});
	}

	private <T> Optional<ITypedIngredient<T>> createTypedIngredient(IClickableIngredient<T> clickableIngredient) {
		IIngredientManager ingredientManager = Internal.getJeiRuntime().getIngredientManager();
		IIngredientType<T> ingredientType = clickableIngredient.getIngredientType();
		T ingredient = clickableIngredient.getIngredient();
		return ingredientManager.createTypedIngredient(ingredientType, ingredient);
	}

	@Override
	public Stream<IDraggableIngredientInternal<?>> getDraggableIngredientUnderMouse(double mouseX, double mouseY) {
		return Stream.empty();
	}
}
