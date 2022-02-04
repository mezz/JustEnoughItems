package mezz.jei.gui.overlay;

import mezz.jei.api.ingredients.ITypedIngredient;

import java.util.List;

public interface IIngredientGridSource {
	List<ITypedIngredient<?>> getIngredientList(String filterText);

	void addListener(Listener listener);

	interface Listener {
		void onChange();
	}
}
