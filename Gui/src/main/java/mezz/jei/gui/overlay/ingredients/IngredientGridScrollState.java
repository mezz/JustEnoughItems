package mezz.jei.gui.overlay.ingredients;

import mezz.jei.common.util.MathUtil;
import mezz.jei.gui.overlay.elements.IElement;
import org.jspecify.annotations.Nullable;

import java.util.List;

public final class IngredientGridScrollState {
	/**
	 * Amount scrolled in percent, where 0 is the top and 1 is the bottom.
	 */
	private float scrollOffsetY = 0;
	@Nullable
	private IElement<?> scrollAnchorElement;
	private float scrollAnchorPositionY = 0;

	public float getScrollOffsetY() {
		return scrollOffsetY;
	}

	public void updateForScrollOffset(float scrollOffsetY, int itemCount, int columns, int visibleRows, int visibleIngredientCount) {
		this.scrollAnchorElement = null;
		this.scrollOffsetY = getValidScrollOffsetY(scrollOffsetY, itemCount, columns, visibleRows, visibleIngredientCount);
	}

	public void updateKeepingScrollAnchorVisible(
		@Nullable IElement<?> scrollAnchorElement,
		List<IElement<?>> ingredientList,
		int columns,
		int visibleRows,
		int visibleIngredientCount,
		boolean smoothScrolling,
		int rowHeight
	) {
		int anchorIndex = IngredientGridPageState.findIndexOfIngredientElement(scrollAnchorElement, ingredientList);
		if (anchorIndex < 0) {
			this.scrollOffsetY = getValidScrollOffsetY(this.scrollOffsetY, ingredientList.size(), columns, visibleRows, visibleIngredientCount);
			return;
		}

		float anchorPositionY = getStoredScrollAnchorPositionY(scrollAnchorElement);
		this.scrollOffsetY = getScrollOffsetYKeepingAnchorVisible(
			anchorIndex,
			ingredientList.size(),
			columns,
			visibleRows,
			visibleIngredientCount,
			anchorPositionY,
			smoothScrolling,
			rowHeight
		);
	}

	@Nullable
	public IElement<?> getScrollAnchorElement(List<IElement<?>> ingredientList) {
		if (this.scrollAnchorElement != null) {
			if (IngredientGridPageState.findIndexOfIngredientElement(this.scrollAnchorElement, ingredientList) >= 0) {
				return this.scrollAnchorElement;
			}
			this.scrollAnchorElement = null;
		}
		return null;
	}

	public void setScrollAnchorElement(IElement<?> scrollAnchorElement, float scrollAnchorPositionY) {
		this.scrollAnchorElement = scrollAnchorElement;
		this.scrollAnchorPositionY = Math.clamp(scrollAnchorPositionY, 0, 1);
	}

	private float getStoredScrollAnchorPositionY(@Nullable IElement<?> scrollAnchorElement) {
		if (scrollAnchorElement != null &&
			this.scrollAnchorElement != null &&
			IngredientGridPageState.isSameIngredientElement(this.scrollAnchorElement, scrollAnchorElement)
		) {
			return this.scrollAnchorPositionY;
		}
		return 0;
	}

	public static float getValidScrollOffsetY(float scrollOffsetY, int itemCount, int columns, int visibleRows) {
		int visibleIngredientCount = columns * visibleRows;
		return getValidScrollOffsetY(scrollOffsetY, itemCount, columns, visibleRows, visibleIngredientCount);
	}

	public static float getValidScrollOffsetY(float scrollOffsetY, int itemCount, int columns, int visibleRows, int visibleIngredientCount) {
		if (getHiddenRows(itemCount, columns, visibleRows, visibleIngredientCount) == 0) {
			return 0;
		}
		return Math.clamp(scrollOffsetY, 0, 1);
	}

	public static float getScrollOffsetYForFirstRow(int firstRow, int itemCount, int columns, int visibleRows, int visibleIngredientCount) {
		int hiddenRows = getHiddenRows(itemCount, columns, visibleRows, visibleIngredientCount);
		if (hiddenRows == 0) {
			return 0;
		}
		return Math.clamp(firstRow / (float) hiddenRows, 0, 1);
	}

	public static float getScrollOffsetYKeepingAnchorVisible(
		int anchorIndex,
		int itemCount,
		int columns,
		int visibleRows,
		float anchorPositionY,
		boolean smoothScrolling,
		int rowHeight
	) {
		int visibleIngredientCount = columns * visibleRows;
		return getScrollOffsetYKeepingAnchorVisible(
			anchorIndex,
			itemCount,
			columns,
			visibleRows,
			visibleIngredientCount,
			anchorPositionY,
			smoothScrolling,
			rowHeight
		);
	}

	public static float getScrollOffsetYKeepingAnchorVisible(
		int anchorIndex,
		int itemCount,
		int columns,
		int visibleRows,
		int visibleIngredientCount,
		float anchorPositionY,
		boolean smoothScrolling,
		int rowHeight
	) {
		int hiddenRows = getHiddenRows(itemCount, columns, visibleRows, visibleIngredientCount);
		if (anchorIndex < 0 || hiddenRows == 0 || columns == 0 || visibleRows == 0) {
			return 0;
		}

		int anchorRow = anchorIndex / columns;
		float validAnchorPositionY = Math.clamp(anchorPositionY, 0, 1);
		if (smoothScrolling) {
			return getSmoothScrollOffsetYKeepingAnchorVisible(
				anchorRow,
				hiddenRows,
				visibleRows,
				validAnchorPositionY,
				rowHeight
			);
		}

		int targetVisibleRow = Math.round(validAnchorPositionY * visibleRows);
		targetVisibleRow = Math.clamp(targetVisibleRow, 0, visibleRows - 1);
		int desiredFirstRow = anchorRow - targetVisibleRow;
		int minFirstRow = Math.max(0, anchorRow - visibleRows + 1);
		int maxFirstRow = Math.min(hiddenRows, anchorRow);
		int validFirstRow = Math.clamp(desiredFirstRow, minFirstRow, maxFirstRow);
		return validFirstRow / (float) hiddenRows;
	}

	private static float getSmoothScrollOffsetYKeepingAnchorVisible(
		int anchorRow,
		int hiddenRows,
		int visibleRows,
		float anchorPositionY,
		int rowHeight
	) {
		if (rowHeight == 0) {
			return 0;
		}

		int hiddenPixels = hiddenRows * rowHeight;
		int visiblePixels = visibleRows * rowHeight;
		int anchorTopPixel = anchorRow * rowHeight;
		int targetAnchorTopPixel = Math.round(anchorPositionY * visiblePixels);
		int desiredScrollPixelOffset = anchorTopPixel - targetAnchorTopPixel;
		int minScrollPixelOffset = Math.max(0, anchorTopPixel - ((visibleRows - 1) * rowHeight));
		int maxScrollPixelOffset = Math.min(hiddenPixels, anchorTopPixel);
		int validScrollPixelOffset = Math.clamp(desiredScrollPixelOffset, minScrollPixelOffset, maxScrollPixelOffset);
		return validScrollPixelOffset / (float) hiddenPixels;
	}

	public static int getFirstItemIndexForScrollOffset(float scrollOffsetY, int itemCount, int columns, int visibleRows) {
		int visibleIngredientCount = columns * visibleRows;
		return getFirstItemIndexForScrollOffset(scrollOffsetY, itemCount, columns, visibleRows, visibleIngredientCount);
	}

	public static int getFirstItemIndexForScrollOffset(float scrollOffsetY, int itemCount, int columns, int visibleRows, int visibleIngredientCount) {
		int hiddenRows = getHiddenRows(itemCount, columns, visibleRows, visibleIngredientCount);
		int firstRow = getFirstRowForScrollOffset(hiddenRows, scrollOffsetY);
		int firstItemIndex = getFirstItemIndexForRow(firstRow, itemCount, columns);
		int maxFirstItemIndex = getMaxFirstItemIndex(itemCount, columns, visibleRows, visibleIngredientCount);
		if (Float.compare(scrollOffsetY, 1.0f) >= 0) {
			return maxFirstItemIndex;
		}
		return Math.min(firstItemIndex, maxFirstItemIndex);
	}

	public static int getFirstRowForScrollOffset(int hiddenRows, float scrollOffsetY) {
		int rowIndex = (int) ((double) (scrollOffsetY * (float) hiddenRows) + 0.5D);
		return Math.max(rowIndex, 0);
	}

	public static int getFirstItemIndexForRow(int firstRow, int itemCount, int columns) {
		if (itemCount == 0 || columns == 0) {
			return 0;
		}
		return Math.max(0, firstRow) * columns;
	}

	public static int getSmoothScrollPixelOffset(int hiddenRows, int rowHeight, float scrollOffsetY) {
		int hiddenPixels = hiddenRows * rowHeight;
		return Math.clamp(Math.round(hiddenPixels * scrollOffsetY), 0, hiddenPixels);
	}

	public static int getFirstRowForSmoothScrollPixelOffset(int scrollPixelOffset, int rowHeight) {
		if (rowHeight == 0) {
			return 0;
		}
		return scrollPixelOffset / rowHeight;
	}

	public static int getRowPixelOffset(int scrollPixelOffset, int rowHeight) {
		if (rowHeight == 0) {
			return 0;
		}
		return scrollPixelOffset % rowHeight;
	}

	public static int getHiddenRows(int itemCount, int columns, int visibleRows) {
		int visibleIngredientCount = columns * visibleRows;
		return getHiddenRows(itemCount, columns, visibleRows, visibleIngredientCount);
	}

	public static int getHiddenRows(int itemCount, int columns, int visibleRows, int visibleIngredientCount) {
		int maxFirstItemIndex = getMaxFirstItemIndex(itemCount, columns, visibleRows, visibleIngredientCount);
		return getTotalRows(maxFirstItemIndex, columns);
	}

	public static int getMaxFirstItemIndex(int itemCount, int columns, int visibleRows, int visibleIngredientCount) {
		if (itemCount == 0 || columns == 0 || visibleRows == 0 || visibleIngredientCount == 0) {
			return 0;
		}
		int totalRows = getTotalRows(itemCount, columns);
		int rowBasedFirstItemIndex = Math.max(0, totalRows - visibleRows) * columns;
		int slotBasedFirstItemIndex = Math.max(0, itemCount - visibleIngredientCount);
		int maxFirstItemIndex = Math.max(rowBasedFirstItemIndex, slotBasedFirstItemIndex);
		int lastItemIndex = itemCount - 1;
		return Math.min(maxFirstItemIndex, lastItemIndex);
	}

	public static int getTotalRows(int itemCount, int columns) {
		if (itemCount == 0 || columns == 0) {
			return 0;
		}
		return MathUtil.divideCeil(itemCount, columns);
	}
}
