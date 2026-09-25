package mezz.jei.gui.overlay.history;

import mezz.jei.gui.overlay.ingredients.IngredientGridLayout;
import mezz.jei.gui.overlay.ingredients.IngredientGridWithNavigationLayout;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LookupHistoryOverlayLayoutTest {
	@Test
	public void pagedHistoryReservesNavigationHeight() {
		int maxRows = 3;
		int scrollingHeight = LookupHistoryOverlayLayout.getDisplayHeight(maxRows, true, true);
		int pagedHeight = LookupHistoryOverlayLayout.getDisplayHeight(maxRows, true, false);

		assertEquals(
			IngredientGridWithNavigationLayout.NAVIGATION_HEIGHT + IngredientGridWithNavigationLayout.INNER_PADDING,
			pagedHeight - scrollingHeight
		);
	}

	@Test
	public void displayHeightIncludesRowsAndPanelPadding() {
		int maxRows = 3;
		int expectedHeight = (maxRows * IngredientGridLayout.INGREDIENT_HEIGHT) +
			(2 * IngredientGridWithNavigationLayout.BORDER_MARGIN) +
			(2 * (IngredientGridWithNavigationLayout.BORDER_PADDING +
				IngredientGridWithNavigationLayout.INNER_PADDING));

		int displayHeight = LookupHistoryOverlayLayout.getDisplayHeight(maxRows, true, true);

		assertEquals(expectedHeight, displayHeight);
	}
}
