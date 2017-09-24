package mezz.jei.gui.overlay;

import mezz.jei.config.SessionData;
import mezz.jei.gui.ingredients.IIngredientListElement;
import mezz.jei.ingredients.IngredientFilter;
import mezz.jei.render.IngredientListSlot;
import mezz.jei.render.IngredientRenderer;
import mezz.jei.util.MathUtil;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Displays all known recipe ingredients.
 */
public class IngredientGridAll extends IngredientGrid {
	private final IngredientFilter ingredientFilter;

	public IngredientGridAll(IngredientFilter ingredientFilter) {
		this.ingredientFilter = ingredientFilter;
	}

	@Override
	public void updateLayout(Collection<Rectangle> guiExclusionAreas) {
		super.updateLayout(guiExclusionAreas);
		List<IIngredientListElement> ingredientList = ingredientFilter.getIngredientList();
		this.guiIngredientSlots.set(SessionData.getFirstItemIndex(), ingredientList);
	}

	@Override
	public boolean nextPage() {
		final int itemsCount = ingredientFilter.size();
		if (itemsCount > 0) {
			SessionData.setFirstItemIndex(SessionData.getFirstItemIndex() + guiIngredientSlots.size());
			if (SessionData.getFirstItemIndex() >= itemsCount) {
				SessionData.setFirstItemIndex(0);
			}

			return true;
		} else {
			SessionData.setFirstItemIndex(0);
			return false;
		}
	}

	@Override
	public boolean previousPage() {
		final int itemsPerPage = guiIngredientSlots.size();
		if (itemsPerPage == 0) {
			SessionData.setFirstItemIndex(0);
			return false;
		}
		final int itemsCount = ingredientFilter.size();

		int pageNum = SessionData.getFirstItemIndex() / itemsPerPage;
		if (pageNum == 0) {
			pageNum = itemsCount / itemsPerPage;
		} else {
			pageNum--;
		}

		SessionData.setFirstItemIndex(itemsPerPage * pageNum);
		if (SessionData.getFirstItemIndex() > 0 && SessionData.getFirstItemIndex() == itemsCount) {
			pageNum--;
			SessionData.setFirstItemIndex(itemsPerPage * pageNum);
		}
		return true;
	}

	@Override
	public boolean hasNext() {
		// true if there is more than one page because this wraps around
		int itemsPerPage = guiIngredientSlots.size();
		return itemsPerPage > 0 && ingredientFilter.size() > itemsPerPage;
	}

	@Override
	public boolean hasPrevious() {
		// true if there is more than one page because this wraps around
		int itemsPerPage = guiIngredientSlots.size();
		return itemsPerPage > 0 && ingredientFilter.size() > itemsPerPage;
	}

	@Override
	public List<IIngredientListElement> getVisibleElements() {
		List<IIngredientListElement> visibleElements = new ArrayList<>();
		for (IngredientListSlot slot : guiIngredientSlots.getAllGuiIngredientSlots()) {
			IngredientRenderer renderer = slot.getIngredientRenderer();
			if (renderer != null) {
				visibleElements.add(renderer.getElement());
			}
		}
		return visibleElements;
	}

	@Override
	public int getPageCount() {
		final int itemCount = ingredientFilter.size();
		final int stacksPerPage = guiIngredientSlots.size();
		if (stacksPerPage == 0) {
			return 1;
		}
		int pageCount = MathUtil.divideCeil(itemCount, stacksPerPage);
		pageCount = Math.max(1, pageCount);
		return pageCount;
	}

	@Override
	public int getPageNum() {
		final int stacksPerPage = guiIngredientSlots.size();
		if (stacksPerPage == 0) {
			return 1;
		}
		return SessionData.getFirstItemIndex() / stacksPerPage;
	}
}
