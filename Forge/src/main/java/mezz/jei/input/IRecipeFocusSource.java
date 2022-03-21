package mezz.jei.input;

import java.util.stream.Stream;

public interface IRecipeFocusSource {
	Stream<IClickedIngredient<?>> getIngredientUnderMouse(double mouseX, double mouseY);
}
