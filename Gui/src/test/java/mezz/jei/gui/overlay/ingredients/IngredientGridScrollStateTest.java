package mezz.jei.gui.overlay.ingredients;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.gui.overlay.elements.IElement;
import mezz.jei.gui.overlay.elements.IngredientElement;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static mezz.jei.gui.overlay.ingredients.IngredientGridScrollState.getFirstItemIndexForScrollOffset;
import static mezz.jei.gui.overlay.ingredients.IngredientGridScrollState.getFirstRowForSmoothScrollPixelOffset;
import static mezz.jei.gui.overlay.ingredients.IngredientGridScrollState.getRowPixelOffset;
import static mezz.jei.gui.overlay.ingredients.IngredientGridScrollState.getHiddenRows;
import static mezz.jei.gui.overlay.ingredients.IngredientGridScrollState.getSmoothScrollPixelOffset;
import static mezz.jei.gui.overlay.ingredients.IngredientGridScrollState.getScrollOffsetYKeepingAnchorVisible;
import static mezz.jei.gui.overlay.ingredients.IngredientGridScrollState.getTotalRows;
import static mezz.jei.gui.overlay.ingredients.IngredientGridScrollState.getValidScrollOffsetY;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class IngredientGridScrollStateTest {
	private static final IIngredientType<Integer> INTEGER_TYPE = () -> Integer.class;

	@Test
	public void hiddenRowsAreTotalRowsMinusVisibleRows() {
		// Setup: 100 ingredients in a nine-column grid take 12 rows, and six rows are visible.
		int itemCount = 100;
		int columns = 9;
		int visibleRows = 6;

		// Operation: calculate total and hidden rows.
		int totalRows = getTotalRows(itemCount, columns);
		int hiddenRows = getHiddenRows(itemCount, columns, visibleRows);

		// Assertions: the scroll range is measured in rows.
		assertEquals(12, totalRows);
		assertEquals(6, hiddenRows);
	}

	@Test
	public void hiddenRowsAccountForBlockedVisibleSlots() {
		// Setup: 50 ingredients fit in six nine-column rows, but exclusions leave only 45 usable visible slots.
		int itemCount = 50;
		int columns = 9;
		int visibleRows = 6;
		int visibleIngredientCount = 45;

		// Operation: calculate the hidden rows.
		int hiddenRows = getHiddenRows(itemCount, columns, visibleRows, visibleIngredientCount);

		// Assertions: reduced visible capacity adds a scroll range even though raw rows fit.
		assertEquals(1, hiddenRows);
	}

	@Test
	public void validScrollOffsetClampsToBottom() {
		// Setup: a scroll request goes past the bottom of the list.
		int itemCount = 100;
		int columns = 9;
		int visibleRows = 6;

		// Operation: clamp the requested offset to the valid scroll range.
		float scrollOffsetY = getValidScrollOffsetY(50, itemCount, columns, visibleRows);

		// Assertions: the scroll offset cannot move past the bottom.
		assertEquals(1.0f, scrollOffsetY);
	}

	@Test
	public void firstItemIndexUsesRoundedScrollOffset() {
		// Setup: row-stepped scrolling has accumulated more than half a row.
		int itemCount = 100;
		int columns = 9;
		int visibleRows = 6;
		float scrollOffsetY = 2.6f / getHiddenRows(itemCount, columns, visibleRows);

		// Operation: calculate the rendered first item from the fractional scroll offset.
		int firstItemIndex = getFirstItemIndexForScrollOffset(scrollOffsetY, itemCount, columns, visibleRows);

		// Assertions: row-stepped scrolling renders the closest full row.
		assertEquals(27, firstItemIndex);
	}

	@Test
	public void firstItemIndexAtBottomAccountsForBlockedVisibleSlots() {
		// Setup: exclusions leave fewer usable slots than a full six-row grid.
		int itemCount = 100;
		int columns = 9;
		int visibleRows = 6;
		int visibleIngredientCount = 45;

		// Operation: calculate the first item index at the bottom of the scroll range.
		int firstItemIndex = getFirstItemIndexForScrollOffset(1, itemCount, columns, visibleRows, visibleIngredientCount);

		// Assertions: the bottom position starts late enough for the last item to be visible.
		assertEquals(55, firstItemIndex);
	}

	@Test
	public void smoothScrollOffsetKeepsSubRowPixels() {
		// Setup: smooth scroll has moved six pixels into a row.
		int hiddenRows = 2;
		int rowHeight = IngredientGridLayout.INGREDIENT_HEIGHT;
		float scrollOffsetY = 6 / (float) (hiddenRows * rowHeight);

		// Operation: calculate the rendered row and pixel offset.
		int scrollPixelOffset = getSmoothScrollPixelOffset(hiddenRows, rowHeight, scrollOffsetY);
		int firstRow = getFirstRowForSmoothScrollPixelOffset(scrollPixelOffset, rowHeight);
		int rowPixelOffset = getRowPixelOffset(scrollPixelOffset, rowHeight);

		// Assertions: the first row remains the same, and the pixel offset carries the smooth movement.
		assertEquals(0, firstRow);
		assertEquals(6, rowPixelOffset);
	}

	@Test
	public void scrollOffsetKeepingAnchorVisiblePreservesRelativeRowPosition() {
		// Setup: the anchor was one row down in a ten-row viewport.
		int itemCount = 1000;
		int columns = 10;
		int visibleRows = 20;
		int anchorIndex = 210;
		float anchorPositionY = 0.1f;

		// Operation: recalculate the scroll offset for a twenty-row viewport.
		float scrollOffsetY = getScrollOffsetYKeepingAnchorVisible(
			anchorIndex,
			itemCount,
			columns,
			visibleRows,
			anchorPositionY,
			false,
			IngredientGridLayout.INGREDIENT_HEIGHT
		);
		int hiddenRows = getHiddenRows(itemCount, columns, visibleRows);
		int firstRow = IngredientGridScrollState.getFirstRowForScrollOffset(hiddenRows, scrollOffsetY);

		// Assertions: the anchor row moves to the matching relative position in the new viewport.
		assertEquals(19, firstRow);
	}

	@Test
	public void smoothScrollOffsetKeepingAnchorVisiblePreservesRelativePixelPosition() {
		// Setup: the anchor was 15% down the viewport.
		int itemCount = 1000;
		int columns = 10;
		int visibleRows = 10;
		int rowHeight = 10;
		int anchorIndex = 210;
		float anchorPositionY = 0.15f;

		// Operation: recalculate the smooth scroll offset.
		float scrollOffsetY = getScrollOffsetYKeepingAnchorVisible(
			anchorIndex,
			itemCount,
			columns,
			visibleRows,
			anchorPositionY,
			true,
			rowHeight
		);
		int hiddenRows = getHiddenRows(itemCount, columns, visibleRows);
		int scrollPixelOffset = getSmoothScrollPixelOffset(hiddenRows, rowHeight, scrollOffsetY);

		// Assertions: the anchor's rendered top remains at the same relative pixel position.
		assertEquals(195, scrollPixelOffset);
	}

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

	@Test
	public void scrollOffsetKeepingAnchorVisibleClampsAtBottom() {
		// Setup: the anchor is in the last row, where its requested relative position cannot be preserved.
		int itemCount = 1000;
		int columns = 10;
		int visibleRows = 10;
		int anchorIndex = 990;
		float anchorPositionY = 0.1f;

		// Operation: recalculate the scroll offset.
		float scrollOffsetY = getScrollOffsetYKeepingAnchorVisible(
			anchorIndex,
			itemCount,
			columns,
			visibleRows,
			anchorPositionY,
			false,
			IngredientGridLayout.INGREDIENT_HEIGHT
		);
		int hiddenRows = getHiddenRows(itemCount, columns, visibleRows);
		int firstRow = IngredientGridScrollState.getFirstRowForScrollOffset(hiddenRows, scrollOffsetY);

		// Assertions: the scroll clamps to the bottom while keeping the anchor visible.
		assertEquals(90, firstRow);
	}

	@Test
	public void firstItemIndexIsZeroWhenGridHasNoColumns() {
		// Setup: the overlay has no usable grid columns.
		int itemCount = 100;
		int noColumns = 0;
		int visibleRows = 6;

		// Operation: calculate the first item.
		int firstItemIndex = getFirstItemIndexForScrollOffset(2, itemCount, noColumns, visibleRows);

		// Assertions: there is no meaningful scroll offset without columns.
		assertEquals(0, firstItemIndex);
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
