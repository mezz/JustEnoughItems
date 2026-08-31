package mezz.jei.gui.overlay.ingredients;

import mezz.jei.gui.overlay.elements.IElement;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

public interface IIngredientGridSource {
	@Unmodifiable
	List<IElement<?>> getElements();

	default boolean containsElement(IElement<?> element) {
		return getElements().stream()
			.anyMatch(candidate -> candidate == element);
	}

	void addSourceListChangedListener(SourceListChangedListener listener);

	interface SourceListChangedListener {
		void onSourceListChanged();
	}
}
