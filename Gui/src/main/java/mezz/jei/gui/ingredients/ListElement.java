package mezz.jei.gui.ingredients;

import mezz.jei.api.ingredients.ITypedIngredient;

public class ListElement<V> implements IListElement<V> {
	private final ITypedIngredient<V> ingredient;
	private int sortIndex;
	private boolean visible = true;

	public ListElement(ITypedIngredient<V> ingredient, int sortIndex) {
		this.ingredient = ingredient;
		this.sortIndex = sortIndex;
	}

	@Override
	public final ITypedIngredient<V> getTypedIngredient() {
		return ingredient;
	}

	@Override
	public int getSortedIndex() {
		return sortIndex;
	}

	@Override
	public void setSortedIndex(int sortIndex) {
		this.sortIndex = sortIndex;
	}

	@Override
	public boolean isVisible() {
		return visible;
	}

	@Override
	public void setVisible(boolean visible) {
		this.visible = visible;
	}
}
