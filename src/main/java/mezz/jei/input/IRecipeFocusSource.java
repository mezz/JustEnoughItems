package mezz.jei.input;

import java.util.Optional;

public interface IRecipeFocusSource {
	Optional<IClickedIngredient<?>> getIngredientUnderMouse(double mouseX, double mouseY);
}
