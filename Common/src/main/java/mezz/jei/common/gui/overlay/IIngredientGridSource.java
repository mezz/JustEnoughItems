package mezz.jei.common.gui.overlay;

import mezz.jei.api.ingredients.ITypedIngredient;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

public interface IIngredientGridSource {
	@Unmodifiable
	List<ITypedIngredient<?>> getIngredientList();

	void addSourceListChangedListener(SourceListChangedListener listener);

	interface SourceListChangedListener {
		void onSourceListChanged();
	}
}
