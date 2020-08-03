package mezz.jei.gui.ghost;

import mezz.jei.gui.ingredients.IIngredientListElement;

import javax.annotation.Nullable;

public interface IGhostIngredientDragSource {
	@Nullable
	IIngredientListElement<?> getElementUnderMouse();
}
