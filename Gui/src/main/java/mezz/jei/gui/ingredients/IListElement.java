package mezz.jei.gui.ingredients;

import mezz.jei.api.ingredients.ITypedIngredient;

public sealed interface IListElement permits ListElement, ListGroupElement {

	boolean isGroup();

	ITypedIngredient<?> getTypedIngredient();

	int getSortedIndex();

	void setSortedIndex(int sortIndex);

	int getCreatedIndex();

	boolean isVisible();

	void setVisible(boolean visible);
}
