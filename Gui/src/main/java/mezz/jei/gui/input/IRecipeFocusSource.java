package mezz.jei.gui.input;

import mezz.jei.common.input.IClickableIngredientInternal;

import java.util.stream.Stream;

public interface IRecipeFocusSource {
	Stream<IClickableIngredientInternal<?>> getIngredientUnderMouse(double mouseX, double mouseY);
}
