package mezz.jei.gui.overlay;

import java.util.List;

import mezz.jei.gui.ingredients.IIngredientListElement;

public interface IIngredientGridSource {
	List<IIngredientListElement> getIngredientList();

	int size();

	void addListener(Listener listener);

	interface Listener {
		void onChange();
	}
}
