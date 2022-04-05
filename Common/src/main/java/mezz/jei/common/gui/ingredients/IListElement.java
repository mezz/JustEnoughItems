package mezz.jei.common.gui.ingredients;

import mezz.jei.api.ingredients.ITypedIngredient;

public interface IListElement<V> {
	ITypedIngredient<V> getTypedIngredient();

	int getOrderIndex();

	boolean isVisible();

	void setVisible(boolean visible);
}
