package mezz.jei.ingredients;

import mezz.jei.gui.ingredients.IIngredientListElement;

public class IngredientListElement<V> implements IIngredientListElement<V> {
	private final V ingredient;
	private final int orderIndex;
	private boolean visible = true;

	public IngredientListElement(V ingredient, int orderIndex) {
		this.ingredient = ingredient;
		this.orderIndex = orderIndex;
	}

	@Override
	public final V getIngredient() {
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
