package mezz.jei.gui.overlay;

import mezz.jei.gui.ingredients.IIngredientListElement;

import java.util.List;

public interface IIngredientGridSource {
	List<IIngredientListElement> getIngredientList();
	int size();
	void addListener(Listener listener);

	interface Listener {
		void onChange();
	}
}
