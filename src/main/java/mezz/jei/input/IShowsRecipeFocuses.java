package mezz.jei.input;

import javax.annotation.Nullable;

public interface IShowsRecipeFocuses {

	@Nullable
	IClickedIngredient<?> getIngredientUnderMouse(int mouseX, int mouseY);

	boolean canSetFocusWithMouse();

}
