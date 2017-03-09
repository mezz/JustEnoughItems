package mezz.jei.gui;

import java.awt.Rectangle;
import java.util.List;

import com.google.common.collect.ImmutableList;
import mezz.jei.ItemFilter;
import mezz.jei.api.ingredients.IIngredientRegistry;
import mezz.jei.config.SessionData;
import mezz.jei.gui.ingredients.GuiIngredientFast;
import mezz.jei.input.IPaged;
import mezz.jei.util.MathUtil;
import net.minecraft.item.ItemStack;

/**
 * Displays all known recipe ingredients.
 */
public class IngredientGridAll extends IngredientGrid implements IPaged {
	private final ItemFilter itemFilter;

	public IngredientGridAll(IIngredientRegistry ingredientRegistry, Rectangle area, List<Rectangle> guiAreas, ItemFilter itemFilter) {
		super(ingredientRegistry, area, guiAreas);
		this.itemFilter = itemFilter;
	}

	public void updateLayout() {
		ImmutableList<Object> ingredientList = itemFilter.getIngredientList();
		guiIngredientList.set(SessionData.getFirstItemIndex(), ingredientList);
	}

	@Override
	public boolean nextPage() {
		final int itemsCount = itemFilter.size();
		if (itemsCount > 0) {
			SessionData.setFirstItemIndex(SessionData.getFirstItemIndex() + guiIngredientList.size());
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
		final int itemsPerPage = guiIngredientList.size();
		if (itemsPerPage == 0) {
			SessionData.setFirstItemIndex(0);
			return false;
		}
		final int itemsCount = itemFilter.size();

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
		int itemsPerPage = guiIngredientList.size();
		return itemsPerPage > 0 && itemFilter.size() > itemsPerPage;
	}

	@Override
	public boolean hasPrevious() {
		// true if there is more than one page because this wraps around
		int itemsPerPage = guiIngredientList.size();
		return itemsPerPage > 0 && itemFilter.size() > itemsPerPage;
	}

	public ImmutableList<ItemStack> getVisibleStacks() {
		ImmutableList.Builder<ItemStack> visibleStacks = ImmutableList.builder();
		for (GuiIngredientFast guiItemStack : guiIngredientList.getAllGuiIngredients()) {
			Object ingredient = guiItemStack.getIngredient();
			if (ingredient instanceof ItemStack) {
				ItemStack itemStack = (ItemStack) ingredient;
				visibleStacks.add(itemStack);
			}
		}
		return visibleStacks.build();
	}

	public int getPageCount() {
		final int itemCount = itemFilter.size();
		final int stacksPerPage = guiIngredientList.size();
		if (stacksPerPage == 0) {
			return 1;
		}
		int pageCount = MathUtil.divideCeil(itemCount, stacksPerPage);
		pageCount = Math.max(1, pageCount);
		return pageCount;
	}

	public int getPageNum() {
		final int stacksPerPage = guiIngredientList.size();
		if (stacksPerPage == 0) {
			return 1;
		}
		return SessionData.getFirstItemIndex() / stacksPerPage;
	}
}
