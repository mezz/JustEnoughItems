package mezz.jei.gui.input;

import mezz.jei.common.input.IMouseOverable;

import java.util.stream.Stream;

public interface IRecipeFocusSource extends IMouseOverable {
	Stream<IClickableIngredientInternal<?>> getIngredientUnderMouse(double mouseX, double mouseY);
	Stream<IDraggableIngredientInternal<?>> getDraggableIngredientUnderMouse(double mouseX, double mouseY);
}
