package mezz.jei.gui.ingredients;

import mezz.jei.api.ingredients.ITypedIngredient;

public interface IListElement<V> {
	ITypedIngredient<V> getTypedIngredient();

	int getSortedIndex();

	void setSortedIndex(int sortIndex);

	int getCreatedIndex();

	boolean isVisible();

	void setVisible(boolean visible);
}
