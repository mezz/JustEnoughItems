package mezz.jei.gui.overlay;

import mezz.jei.api.ingredients.ITypedIngredient;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

public interface IIngredientGridSource {
	@Unmodifiable
	List<ITypedIngredient<?>> getIngredientList(String filterText);

	void addListener(Listener listener);

	interface Listener {
		void onChange();
	}
}
