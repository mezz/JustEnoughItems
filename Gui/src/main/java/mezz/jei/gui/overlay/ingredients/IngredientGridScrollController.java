package mezz.jei.gui.overlay.ingredients;

import mezz.jei.common.config.IClientConfig;
import mezz.jei.common.config.IIngredientGridConfig;
import mezz.jei.common.gui.GridScrollMath;
import mezz.jei.gui.overlay.elements.IElement;
import org.jspecify.annotations.Nullable;

import java.util.List;

public final class IngredientGridScrollController {
	private static final ScrollResult NOT_CONSUMED = new ScrollResult(false, false);

	private final IngredientGridScrollState scrollState = new IngredientGridScrollState();
	private final IIngredientGridSource ingredientSource;
	private final IIngredientGrid ingredientGrid;
	private final IIngredientGridConfig gridConfig;
	private final IClientConfig clientConfig;

	public IngredientGridScrollController(
		IIngredientGridSource ingredientSource,
		IIngredientGrid ingredientGrid,
		IIngredientGridConfig gridConfig,
		IClientConfig clientConfig
	) {
		this.ingredientSource = ingredientSource;
		this.ingredientGrid = ingredientGrid;
		this.gridConfig = gridConfig;
		this.clientConfig = clientConfig;
	}

	public void updateLayoutStartingAt(int firstItemIndex) {
		List<IElement<?>> ingredientList = ingredientSource.getElements();
		int columnCount = ingredientGrid.getColumnCount();
		int rowCount = ingredientGrid.getRowCount();
		int visibleIngredientCount = ingredientGrid.size();
		int firstRow = 0;
		if (columnCount > 0) {
			firstRow = firstItemIndex / columnCount;
		}
		float scrollOffsetY;
		if (isSmoothScrolling()) {
			scrollOffsetY = getScrollOffsetYForPixelOffset(firstRow * IngredientGridLayout.INGREDIENT_HEIGHT);
		} else {
			scrollOffsetY = GridScrollMath.getScrollOffsetYForFirstRow(
				firstRow,
				ingredientList.size(),
				columnCount,
				rowCount,
				visibleIngredientCount
			);
		}
		this.scrollState.updateForScrollOffset(
			scrollOffsetY,
			ingredientList.size(),
			columnCount,
			rowCount,
			visibleIngredientCount
		);
		updateGridFromScrollState(ingredientList);
		rememberFirstVisibleElementAsScrollAnchor();
	}

	public void updateLayoutKeepingScrollAnchorVisible(@Nullable IElement<?> scrollAnchorElement) {
		List<IElement<?>> ingredientList = ingredientSource.getElements();
		this.scrollState.updateKeepingScrollAnchorVisible(
			scrollAnchorElement,
			ingredientList,
			ingredientGrid.getColumnCount(),
			ingredientGrid.getRowCount(),
			ingredientGrid.size(),
			isSmoothScrolling(),
			IngredientGridLayout.INGREDIENT_HEIGHT,
			ingredientGrid.getVisibleHeight()
		);
		updateGridFromScrollState(ingredientList);
	}

	public @Nullable IElement<?> getScrollAnchorElement() {
		return this.scrollState.getScrollAnchorElement(ingredientSource.getElements());
	}

	public void setScrollAnchorElement(IElement<?> scrollAnchorElement) {
		this.scrollState.setScrollAnchorElement(scrollAnchorElement, getScrollAnchorPositionY(scrollAnchorElement));
	}

	public boolean canScroll() {
		if (isSmoothScrolling()) {
			return getHiddenScrollPixels() > 0;
		}
		return getHiddenScrollRows() > 0;
	}

	public int getVisibleScrollRows() {
		return ingredientGrid.getRowCount();
	}

	public int getHiddenScrollRows() {
		return GridScrollMath.getHiddenRows(
			ingredientSource.getElements().size(),
			ingredientGrid.getColumnCount(),
			ingredientGrid.getRowCount(),
			ingredientGrid.size()
		);
	}

	public int getVisibleScrollAmount() {
		int visibleRows = getVisibleScrollRows();
		if (isSmoothScrolling()) {
			return ingredientGrid.getVisibleHeight();
		}
		return visibleRows;
	}

	public int getHiddenScrollAmount() {
		if (isSmoothScrolling()) {
			return getHiddenScrollPixels();
		}
		return getHiddenScrollRows();
	}

	public float getScrollOffsetY() {
		if (!canScroll()) {
			return 0;
		}
		return this.scrollState.getScrollOffsetY();
	}

	public boolean setScrollOffsetY(float scrollOffsetY) {
		if (!canScroll()) {
			return false;
		}
		return updateScrollOffset(scrollOffsetY);
	}

	public ScrollResult scrollByMouse(double scrollDeltaY) {
		if (!canScroll()) {
			return NOT_CONSUMED;
		}

		float scrollAmount = getMouseWheelScrollAmount(scrollDeltaY);
		if (scrollAmount == 0) {
			return NOT_CONSUMED;
		}

		boolean changed = updateScrollOffset(this.scrollState.getScrollOffsetY() - scrollAmount);
		return new ScrollResult(true, changed);
	}

	public boolean scrollByRows(int rows) {
		if (!canScroll() || rows == 0) {
			return false;
		}
		int firstRow = getFirstVisibleScrollRow() + rows;
		float scrollOffsetY;
		if (isSmoothScrolling()) {
			scrollOffsetY = getScrollOffsetYForPixelOffset(firstRow * IngredientGridLayout.INGREDIENT_HEIGHT);
		} else {
			List<IElement<?>> ingredientList = ingredientSource.getElements();
			scrollOffsetY = GridScrollMath.getScrollOffsetYForFirstRow(
				firstRow,
				ingredientList.size(),
				ingredientGrid.getColumnCount(),
				ingredientGrid.getRowCount(),
				ingredientGrid.size()
			);
		}
		return updateScrollOffset(scrollOffsetY);
	}

	public boolean scrollByPixels(double pixels) {
		int hiddenPixels = getHiddenScrollPixels();
		if (hiddenPixels == 0 || pixels == 0) {
			return false;
		}
		return updateScrollOffset(this.scrollState.getScrollOffsetY() + (float) (pixels / hiddenPixels));
	}

	public int getFirstVisibleScrollRow() {
		if (isSmoothScrolling()) {
			int scrollPixelOffset = getSmoothScrollPixelOffset();
			return GridScrollMath.getFirstRowForSmoothScrollPixelOffset(
				scrollPixelOffset,
				IngredientGridLayout.INGREDIENT_HEIGHT
			);
		}
		return GridScrollMath.getFirstRowForScrollOffset(getHiddenScrollRows(), this.scrollState.getScrollOffsetY());
	}

	private boolean isSmoothScrolling() {
		return this.clientConfig.smoothScrollingEnabled().get();
	}

	private float getMouseWheelScrollAmount(double scrollDeltaY) {
		if (isSmoothScrolling()) {
			int hiddenHeight = getHiddenScrollPixels();
			if (hiddenHeight == 0) {
				return 0;
			}
			return (float) (scrollDeltaY * this.clientConfig.smoothScrollRate().get() / (double) hiddenHeight);
		}
		int hiddenRows = getHiddenScrollRows();
		if (hiddenRows == 0) {
			return 0;
		}
		return (float) (scrollDeltaY / (double) hiddenRows);
	}

	private boolean updateScrollOffset(float scrollOffsetY) {
		float oldScrollOffsetY = this.scrollState.getScrollOffsetY();
		List<IElement<?>> ingredientList = ingredientSource.getElements();
		int columnCount = ingredientGrid.getColumnCount();
		int rowCount = ingredientGrid.getRowCount();
		float validScrollOffsetY = GridScrollMath.getValidScrollOffsetY(
			scrollOffsetY,
			ingredientList.size(),
			columnCount,
			rowCount,
			ingredientGrid.size()
		);
		if (Float.compare(oldScrollOffsetY, validScrollOffsetY) == 0) {
			return false;
		}

		this.scrollState.updateForScrollOffset(
			validScrollOffsetY,
			ingredientList.size(),
			columnCount,
			rowCount,
			ingredientGrid.size()
		);
		updateGridFromScrollState(ingredientList);
		rememberFirstVisibleElementAsScrollAnchor();
		return true;
	}

	private void rememberFirstVisibleElementAsScrollAnchor() {
		if (!isSmoothScrolling()) {
			this.ingredientGrid.getVisibleElements()
				.findFirst()
				.ifPresent(this::setScrollAnchorElement);
		}
	}

	private void updateGridFromScrollState(List<IElement<?>> ingredientList) {
		ScrollRenderPosition scrollRenderPosition = getScrollRenderPosition(ingredientList);
		this.ingredientGrid.set(scrollRenderPosition.firstItemIndex(), scrollRenderPosition.rowPixelOffset(), ingredientList);
	}

	private ScrollRenderPosition getScrollRenderPosition(List<IElement<?>> ingredientList) {
		int columnCount = ingredientGrid.getColumnCount();
		int rowCount = ingredientGrid.getRowCount();
		int visibleIngredientCount = ingredientGrid.size();
		float scrollOffsetY = this.scrollState.getScrollOffsetY();
		if (isSmoothScrolling()) {
			int scrollPixelOffset = getSmoothScrollPixelOffset();
			int firstRow = GridScrollMath.getFirstRowForSmoothScrollPixelOffset(
				scrollPixelOffset,
				IngredientGridLayout.INGREDIENT_HEIGHT
			);
			int firstItemIndex = GridScrollMath.getFirstItemIndexForRow(firstRow, ingredientList.size(), columnCount);
			int maxFirstItemIndex = GridScrollMath.getMaxFirstItemIndex(
				ingredientList.size(),
				columnCount,
				rowCount,
				visibleIngredientCount
			);
			firstItemIndex = Math.min(firstItemIndex, maxFirstItemIndex);
			int rowPixelOffset = GridScrollMath.getRowPixelOffset(
				scrollPixelOffset,
				IngredientGridLayout.INGREDIENT_HEIGHT
			);
			return new ScrollRenderPosition(firstItemIndex, rowPixelOffset);
		}

		int firstItemIndex = GridScrollMath.getFirstItemIndexForScrollOffset(
			scrollOffsetY,
			ingredientList.size(),
			columnCount,
			rowCount,
			visibleIngredientCount
		);
		return new ScrollRenderPosition(firstItemIndex, 0);
	}

	private float getScrollAnchorPositionY(IElement<?> element) {
		List<IElement<?>> ingredientList = ingredientSource.getElements();
		int anchorIndex = IngredientGridPageState.findIndexOfIngredientElement(element, ingredientList);
		int columnCount = ingredientGrid.getColumnCount();
		int rowCount = ingredientGrid.getRowCount();
		if (anchorIndex < 0 || columnCount == 0 || rowCount == 0) {
			return 0;
		}

		int visibleHeight = ingredientGrid.getVisibleHeight();
		if (visibleHeight == 0) {
			return 0;
		}
		int anchorRow = anchorIndex / columnCount;
		int scrollPixelOffset = getCurrentScrollPixelOffset(ingredientList);
		int anchorTopY = (anchorRow * IngredientGridLayout.INGREDIENT_HEIGHT) - scrollPixelOffset;
		return Math.clamp(anchorTopY / (float) visibleHeight, 0, 1);
	}

	private int getCurrentScrollPixelOffset(List<IElement<?>> ingredientList) {
		int columnCount = ingredientGrid.getColumnCount();
		int rowCount = ingredientGrid.getRowCount();
		int hiddenRows = GridScrollMath.getHiddenRows(ingredientList.size(), columnCount, rowCount, ingredientGrid.size());
		float scrollOffsetY = this.scrollState.getScrollOffsetY();
		if (isSmoothScrolling()) {
			return getSmoothScrollPixelOffset();
		}
		int firstRow = GridScrollMath.getFirstRowForScrollOffset(hiddenRows, scrollOffsetY);
		return firstRow * IngredientGridLayout.INGREDIENT_HEIGHT;
	}

	private int getHiddenScrollPixels() {
		return GridScrollMath.getHiddenScrollPixels(
			getHiddenScrollRows(),
			IngredientGridLayout.INGREDIENT_HEIGHT,
			ingredientGrid.getVisibleHeight()
		);
	}

	private int getSmoothScrollPixelOffset() {
		int hiddenPixels = getHiddenScrollPixels();
		return Math.clamp(Math.round(hiddenPixels * this.scrollState.getScrollOffsetY()), 0, hiddenPixels);
	}

	private float getScrollOffsetYForPixelOffset(int scrollPixelOffset) {
		int hiddenPixels = getHiddenScrollPixels();
		if (hiddenPixels == 0) {
			return 0;
		}
		return Math.clamp(scrollPixelOffset / (float) hiddenPixels, 0, 1);
	}

	private record ScrollRenderPosition(int firstItemIndex, int rowPixelOffset) {

	}

	public record ScrollResult(boolean consumed, boolean changed) {

	}
}
