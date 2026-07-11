package mezz.jei.gui.overlay;

import mezz.jei.gui.input.IRecipeFocusSource;
import mezz.jei.gui.overlay.elements.IElement;

import java.util.List;
import java.util.stream.Stream;

public interface IIngredientGrid extends IRecipeFocusSource {
	boolean isMouseOver(double mouseX, double mouseY);

	int size();

	void set(int firstItemIndex, List<IElement<?>> ingredientList);

	Stream<IElement<?>> getVisibleElements();
}
