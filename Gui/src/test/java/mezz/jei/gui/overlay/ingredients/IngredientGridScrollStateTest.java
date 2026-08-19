package mezz.jei.gui.overlay.ingredients;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.gui.overlay.elements.IElement;
import mezz.jei.gui.overlay.elements.IngredientElement;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class IngredientGridScrollStateTest {
	private static final IIngredientType<Integer> INTEGER_TYPE = () -> Integer.class;

	@Test
	public void missingAnchorPreservesScrollOffsetWhenLayoutStillScrolls() {
		// Setup: the overlay was closed after the user scrolled down, so there is no visible element to use
		// as an anchor when the overlay opens again.
		int itemCount = 100;
		int columns = 10;
		int visibleRows = 5;
		int visibleIngredientCount = 50;
		IngredientGridScrollState scrollState = new IngredientGridScrollState();
		scrollState.updateForScrollOffset(0.5f, itemCount, columns, visibleRows, visibleIngredientCount);

		// Operation: update the layout without an anchor while the grid is still scrollable.
		scrollState.updateKeepingScrollAnchorVisible(
			null,
			createElements(itemCount),
			columns,
			visibleRows,
			visibleIngredientCount,
			false,
			IngredientGridLayout.INGREDIENT_HEIGHT
		);

		// Assertions: the reopen/layout pass keeps the current scroll position instead of resetting to top.
		assertEquals(0.5f, scrollState.getScrollOffsetY());
	}

	private static List<IElement<?>> createElements(int itemCount) {
		List<IElement<?>> elements = new ArrayList<>();
		for (int i = 0; i < itemCount; i++) {
			elements.add(new IngredientElement<>(new TestTypedIngredient(i)));
		}
		return List.copyOf(elements);
	}

	private record TestTypedIngredient(Integer ingredient) implements ITypedIngredient<Integer> {
		@Override
		public ITypedIngredient<Integer> normalize(IIngredientHelper<Integer> ingredientHelper) {
			return this;
		}

		@Override
		public IIngredientType<Integer> getType() {
			return INTEGER_TYPE;
		}

		@Override
		public Integer getIngredient() {
			return ingredient;
		}
	}
}
