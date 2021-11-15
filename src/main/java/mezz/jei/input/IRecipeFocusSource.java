package mezz.jei.input;

import javax.annotation.Nullable;

public interface IRecipeFocusSource {
	@Nullable
	IClickedIngredient<?> getIngredientUnderMouse(double mouseX, double mouseY);
}
