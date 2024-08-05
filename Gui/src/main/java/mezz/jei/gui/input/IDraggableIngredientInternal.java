package mezz.jei.gui.input;

import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.gui.overlay.elements.IElement;


public interface IDraggableIngredientInternal<T> {
	ITypedIngredient<T> getTypedIngredient();

	IElement<T> getElement();

	ImmutableRect2i getArea();
}
