package mezz.jei.gui.ghost;

import mezz.jei.input.IClickedIngredient;

import javax.annotation.Nullable;

public interface IGhostIngredientDragSource {
	@Nullable
	IClickedIngredient<?> getIngredientUnderMouse(double mouseX, double mouseY);
}
