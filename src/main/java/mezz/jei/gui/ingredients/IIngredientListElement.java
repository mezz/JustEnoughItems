package mezz.jei.gui.ingredients;

import mezz.jei.api.ingredients.ITypedIngredient;

public interface IIngredientListElement<V> {
	ITypedIngredient<V> getTypedIngredient();

	int getOrderIndex();

	boolean isVisible();

	void setVisible(boolean visible);
}
