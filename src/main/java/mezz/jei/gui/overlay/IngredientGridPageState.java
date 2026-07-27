package mezz.jei.gui.overlay;

import javax.annotation.Nullable;
import java.util.List;

import mezz.jei.gui.ingredients.IIngredientListElement;
import mezz.jei.util.MathUtil;

final class IngredientGridPageState {
	/**
	 * The normalized first item index for the page currently being rendered.
	 * Requested item indexes and anchor indexes are rounded down to their containing page before being stored here.
	 */
	private int firstItemIndex = 0;
	/**
	 * An explicit element to keep visible when the ingredient list or grid bounds change.
	 */
	@Nullable
	private IIngredientListElement pageAnchorElement;

	public int getFirstItemIndex() {
		return firstItemIndex;
	}

	public int updateForPageNavigation(int firstItemIndex, int itemCount, int itemsPerPage) {
		this.pageAnchorElement = null;
		this.firstItemIndex = getFirstItemIndexForValidPage(firstItemIndex, itemCount, itemsPerPage);
		return this.firstItemIndex;
	}

	public int updateKeepingPageAnchorVisible(@Nullable IIngredientListElement pageAnchorElement, List<IIngredientListElement> ingredientList, int itemsPerPage) {
		int anchorIndex = findIndexOfIngredientElement(pageAnchorElement, ingredientList);
		this.firstItemIndex = getFirstItemIndexForValidPage(anchorIndex, ingredientList.size(), itemsPerPage);
		return this.firstItemIndex;
	}

	@Nullable
	public IIngredientListElement getPageAnchorElement(List<IIngredientListElement> ingredientList) {
		if (this.pageAnchorElement != null) {
			if (findIndexOfIngredientElement(this.pageAnchorElement, ingredientList) >= 0) {
				return this.pageAnchorElement;
			}
			this.pageAnchorElement = null;
		}
		return null;
	}

	public void setPageAnchorElement(IIngredientListElement pageAnchorElement) {
		this.pageAnchorElement = pageAnchorElement;
	}

	static int findIndexOfIngredientElement(@Nullable IIngredientListElement element, List<IIngredientListElement> ingredientList) {
		if (element == null) {
			return -1;
		}
		for (int i = 0; i < ingredientList.size(); i++) {
			if (isSameIngredientElement(ingredientList.get(i), element)) {
				return i;
			}
		}
		return -1;
	}

	static int getFirstItemIndexForValidPage(int firstItemIndex, int itemCount, int itemsPerPage) {
		if (itemCount == 0 || itemsPerPage == 0) {
			return 0;
		}
		int requestedPageStart = (Math.max(0, firstItemIndex) / itemsPerPage) * itemsPerPage;
		int lastPageIndex = ((itemCount - 1) / itemsPerPage) * itemsPerPage;
		return Math.min(requestedPageStart, lastPageIndex);
	}

	/**
	 * Page anchors only need to find the same in-memory element after filtering or relayout.
	 * A full UID comparison would call ingredient helpers and subtype interpreters for every candidate here,
	 * which is slower and unnecessary for keeping the user's visible page stable.
	 */
	static boolean isSameIngredientElement(IIngredientListElement first, IIngredientListElement second) {
		if (first == second) {
			return true;
		}
		return first.getIngredient() == second.getIngredient();
	}

	static int getPageCount(int itemCount, int itemsPerPage) {
		if (itemsPerPage == 0) {
			return 1;
		}
		int pageCount = MathUtil.divideCeil(itemCount, itemsPerPage);
		pageCount = Math.max(1, pageCount);
		return pageCount;
	}

	static int getPageNumberForFirstItemIndex(int firstItemIndex, int itemsPerPage, int itemCount) {
		int firstIndex = getFirstItemIndexForValidPage(firstItemIndex, itemCount, itemsPerPage);
		if (itemsPerPage == 0) {
			return 0;
		}
		return firstIndex / itemsPerPage;
	}
}
