package mezz.jei.gui.overlay.history;

import mezz.jei.common.util.ImmutableRect2i;
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

	@Test
	public void availableAreaAlignsToOwnerBackground() {
		ImmutableRect2i ownerBackgroundArea = new ImmutableRect2i(20, 30, 100, 80);
		ImmutableRect2i lookupHistoryArea = new ImmutableRect2i(0, 180, 200, 50);

		ImmutableRect2i result = LookupHistoryOverlayLayout.alignToOwnerBackground(
			lookupHistoryArea,
			ownerBackgroundArea
		);

		assertEquals(ownerBackgroundArea.x() - IngredientGridWithNavigationLayout.BORDER_MARGIN, result.x());
		assertEquals(
			ownerBackgroundArea.width() + (2 * IngredientGridWithNavigationLayout.BORDER_MARGIN),
			result.width()
		);
	}

	@Test
	public void panelContentsUseTheSameGapAsGridAndScrollbar() {
		ImmutableRect2i ownerBackgroundArea = new ImmutableRect2i(10, 20, 100, 80);
		ImmutableRect2i lookupHistoryArea = new ImmutableRect2i(4, 170, 112, 70);
		ImmutableRect2i lookupHistoryBackgroundArea = new ImmutableRect2i(10, 180, 100, 50);

		ImmutableRect2i result = LookupHistoryOverlayLayout.moveNextToOwner(
			lookupHistoryArea,
			lookupHistoryBackgroundArea,
			ownerBackgroundArea
		);

		int offset = lookupHistoryArea.y() - result.y();
		int ownerContentBottom = ownerBackgroundArea.y() + ownerBackgroundArea.height() -
			IngredientGridWithNavigationLayout.BORDER_PADDING;
		int lookupHistoryContentTop = lookupHistoryBackgroundArea.y() - offset +
			IngredientGridWithNavigationLayout.BORDER_PADDING;
		assertEquals(
			IngredientGridWithNavigationLayout.INNER_PADDING,
			lookupHistoryContentTop - ownerContentBottom
		);
		assertEquals(lookupHistoryArea.width(), result.width());
		assertEquals(lookupHistoryArea.height(), result.height());
	}
}
