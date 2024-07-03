package mezz.jei.gui.input;

import java.util.stream.Stream;

public interface IRecipeFocusSource {
	Stream<IClickableIngredientInternal<?>> getIngredientUnderMouse(double mouseX, double mouseY);
}
