package mezz.jei.gui.ingredients;

public interface IIngredientListElement<V> {
	V getIngredient();

	int getOrderIndex();

	boolean isVisible();

	void setVisible(boolean visible);
}
