package mezz.jei.input;

import javax.annotation.Nullable;

public interface IShowsRecipeFocuses {

	@Nullable
	IClickedIngredient<?> getIngredientUnderMouse(double mouseX, double mouseY);

	boolean canSetFocusWithMouse();

}
