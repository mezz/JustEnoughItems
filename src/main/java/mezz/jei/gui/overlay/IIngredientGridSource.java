package mezz.jei.gui.overlay;

import java.util.List;

public interface IIngredientGridSource {
	List<?> getIngredientList(String filterText);

	void addListener(Listener listener);

	interface Listener {
		void onChange();
	}
}
