package mezz.jei.common.gui;

import org.junit.jupiter.api.Test;

import static mezz.jei.common.gui.GridScrollMath.getFirstItemIndexForScrollOffset;
import static mezz.jei.common.gui.GridScrollMath.getFirstRowForScrollOffset;
import static mezz.jei.common.gui.GridScrollMath.getFirstRowForSmoothScrollPixelOffset;
import static mezz.jei.common.gui.GridScrollMath.getHiddenRows;
import static mezz.jei.common.gui.GridScrollMath.getRowPixelOffset;
import static mezz.jei.common.gui.GridScrollMath.getScrollOffsetYKeepingAnchorVisible;
import static mezz.jei.common.gui.GridScrollMath.getSmoothScrollPixelOffset;
import static mezz.jei.common.gui.GridScrollMath.getTotalRows;
import static mezz.jei.common.gui.GridScrollMath.getValidScrollOffsetY;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class GridScrollMathTest {
	@Test
	public void hiddenRowsAreTotalRowsMinusVisibleRows() {
		int itemCount = 100;
		int columns = 9;
		int visibleRows = 6;

		int totalRows = getTotalRows(itemCount, columns);
		int hiddenRows = getHiddenRows(itemCount, columns, visibleRows);

		assertEquals(12, totalRows);
		assertEquals(6, hiddenRows);
	}

	@Test
	public void hiddenRowsAccountForBlockedVisibleSlots() {
		int itemCount = 50;
		int columns = 9;
		int visibleRows = 6;
		int visibleItemCount = 45;

		int hiddenRows = getHiddenRows(itemCount, columns, visibleRows, visibleItemCount);

		assertEquals(1, hiddenRows);
	}

	@Test
	public void validScrollOffsetClampsToBottom() {
		float scrollOffsetY = getValidScrollOffsetY(50, 100, 9, 6);

		assertEquals(1.0f, scrollOffsetY);
	}

	@Test
	public void firstItemIndexUsesRoundedScrollOffset() {
		int itemCount = 100;
		int columns = 9;
		int visibleRows = 6;
		float scrollOffsetY = 2.6f / getHiddenRows(itemCount, columns, visibleRows);

		int firstItemIndex = getFirstItemIndexForScrollOffset(scrollOffsetY, itemCount, columns, visibleRows);

		assertEquals(27, firstItemIndex);
	}

	@Test
	public void firstItemIndexAtBottomAccountsForBlockedVisibleSlots() {
		int firstItemIndex = getFirstItemIndexForScrollOffset(1, 100, 9, 6, 45);

		assertEquals(55, firstItemIndex);
	}

	@Test
	public void smoothScrollOffsetKeepsSubRowPixels() {
		int hiddenRows = 2;
		int rowHeight = 18;
		float scrollOffsetY = 6 / (float) (hiddenRows * rowHeight);

		int scrollPixelOffset = getSmoothScrollPixelOffset(hiddenRows, rowHeight, scrollOffsetY);
		int firstRow = getFirstRowForSmoothScrollPixelOffset(scrollPixelOffset, rowHeight);
		int rowPixelOffset = getRowPixelOffset(scrollPixelOffset, rowHeight);

		assertEquals(0, firstRow);
		assertEquals(6, rowPixelOffset);
	}

	@Test
	public void scrollOffsetKeepingAnchorVisiblePreservesRelativeRowPosition() {
		int itemCount = 1000;
		int columns = 10;
		int visibleRows = 20;
		float scrollOffsetY = getScrollOffsetYKeepingAnchorVisible(
			210,
			itemCount,
			columns,
			visibleRows,
			0.1f,
			false,
			18
		);

		int hiddenRows = getHiddenRows(itemCount, columns, visibleRows);
		int firstRow = getFirstRowForScrollOffset(hiddenRows, scrollOffsetY);

		assertEquals(19, firstRow);
	}

	@Test
	public void smoothScrollOffsetKeepingAnchorVisiblePreservesRelativePixelPosition() {
		int itemCount = 1000;
		int columns = 10;
		int visibleRows = 10;
		int rowHeight = 10;
		float scrollOffsetY = getScrollOffsetYKeepingAnchorVisible(
			210,
			itemCount,
			columns,
			visibleRows,
			0.15f,
			true,
			rowHeight
		);

		int hiddenRows = getHiddenRows(itemCount, columns, visibleRows);
		int scrollPixelOffset = getSmoothScrollPixelOffset(hiddenRows, rowHeight, scrollOffsetY);

		assertEquals(195, scrollPixelOffset);
	}

	@Test
	public void scrollOffsetKeepingAnchorVisibleClampsAtBottom() {
		int itemCount = 1000;
		int columns = 10;
		int visibleRows = 10;
		float scrollOffsetY = getScrollOffsetYKeepingAnchorVisible(
			990,
			itemCount,
			columns,
			visibleRows,
			0.1f,
			false,
			18
		);

		int hiddenRows = getHiddenRows(itemCount, columns, visibleRows);
		int firstRow = getFirstRowForScrollOffset(hiddenRows, scrollOffsetY);

		assertEquals(90, firstRow);
	}

	@Test
	public void firstItemIndexIsZeroWhenGridHasNoColumns() {
		int firstItemIndex = getFirstItemIndexForScrollOffset(2, 100, 0, 6);

		assertEquals(0, firstItemIndex);
	}
}
