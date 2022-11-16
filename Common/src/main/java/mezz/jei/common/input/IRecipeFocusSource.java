package mezz.jei.common.input;

import mezz.jei.api.runtime.IClickedIngredient;

import java.util.stream.Stream;

public interface IRecipeFocusSource {
	Stream<IClickedIngredient<?>> getIngredientUnderMouse(double mouseX, double mouseY);
}
