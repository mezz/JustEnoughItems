package mezz.jei.gui.input;

import mezz.jei.common.input.IMouseOverable;

import java.util.stream.Stream;

public interface IRecipeFocusSource extends IMouseOverable {
	Stream<IClickableIngredientInternal<?>> getIngredientUnderMouse(double mouseX, double mouseY);
	Stream<IDraggableIngredientInternal<?>> getDraggableIngredientUnderMouse(double mouseX, double mouseY);

	/** Returns the ingredients selected by keyboard focus, or an empty stream when none is selected. */
	default Stream<IClickableIngredientInternal<?>> getKeyboardFocusedIngredients() {
		return Stream.empty();
	}
}
