package mezz.jei.gui.overlay.ingredients;

import mezz.jei.gui.input.IRecipeFocusSource;
import mezz.jei.gui.overlay.elements.IElement;

import java.util.List;
import java.util.stream.Stream;

public interface IIngredientGrid extends IRecipeFocusSource {
	boolean isMouseOver(double mouseX, double mouseY);

	int size();

	int getColumnCount();

	int getRowCount();

	int getVisibleHeight();

	void set(int firstItemIndex, List<IElement<?>> ingredientList);

	void set(int firstItemIndex, int smoothScrollRowPixelOffset, List<IElement<?>> ingredientList);

	Stream<IElement<?>> getVisibleElements();

	void tick();
}
