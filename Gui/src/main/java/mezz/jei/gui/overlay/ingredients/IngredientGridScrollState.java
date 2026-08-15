package mezz.jei.gui.overlay.ingredients;

import mezz.jei.common.gui.GridScrollMath;
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
		this.scrollOffsetY = GridScrollMath.getValidScrollOffsetY(scrollOffsetY, itemCount, columns, visibleRows, visibleIngredientCount);
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
			this.scrollOffsetY = GridScrollMath.getValidScrollOffsetY(this.scrollOffsetY, ingredientList.size(), columns, visibleRows, visibleIngredientCount);
			return;
		}

		float anchorPositionY = getStoredScrollAnchorPositionY(scrollAnchorElement);
		this.scrollOffsetY = GridScrollMath.getScrollOffsetYKeepingAnchorVisible(
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
			IngredientGridPageState.isSameIngredientElement(this.scrollAnchorElement, scrollAnchorElement)) {
			return this.scrollAnchorPositionY;
		}
		return 0;
	}
}
