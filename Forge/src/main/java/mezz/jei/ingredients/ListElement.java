package mezz.jei.ingredients;

import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.gui.ingredients.IListElement;

public class ListElement<V> implements IListElement<V> {
	private final ITypedIngredient<V> ingredient;
	private final int orderIndex;
	private boolean visible = true;

	public ListElement(ITypedIngredient<V> ingredient, int orderIndex) {
		this.ingredient = ingredient;
		this.orderIndex = orderIndex;
	}

	@Override
	public final ITypedIngredient<V> getTypedIngredient() {
		return ingredient;
	}

	@Override
	public int getOrderIndex() {
		return orderIndex;
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
