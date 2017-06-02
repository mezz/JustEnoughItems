package mezz.jei.gui.overlay;

import java.util.List;

import com.google.common.collect.ImmutableList;
import mezz.jei.api.ingredients.IIngredientRegistry;
import mezz.jei.config.SessionData;
import mezz.jei.gui.ingredients.IIngredientListElement;
import mezz.jei.ingredients.IngredientFilter;
import mezz.jei.render.GuiIngredientFast;
import mezz.jei.util.MathUtil;
import net.minecraft.item.ItemStack;

/**
 * Displays all known recipe ingredients.
 */
public class IngredientGridAll extends IngredientGrid {
	private final IngredientFilter ingredientFilter;

	public IngredientGridAll(IIngredientRegistry ingredientRegistry, IngredientFilter ingredientFilter) {
		super(ingredientRegistry);
		this.ingredientFilter = ingredientFilter;
	}

	@Override
	public void updateLayout() {
		super.updateLayout();
		List<IIngredientListElement> ingredientList = ingredientFilter.getIngredientList();
		this.guiIngredientList.set(SessionData.getFirstItemIndex(), ingredientList);
	}

	@Override
	public boolean nextPage() {
		final int itemsCount = ingredientFilter.size();
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
		int itemsPerPage = guiIngredientList.size();
		return itemsPerPage > 0 && ingredientFilter.size() > itemsPerPage;
	}

	@Override
	public boolean hasPrevious() {
		// true if there is more than one page because this wraps around
		int itemsPerPage = guiIngredientList.size();
		return itemsPerPage > 0 && ingredientFilter.size() > itemsPerPage;
	}

	@Override
	public ImmutableList<ItemStack> getVisibleStacks() {
		ImmutableList.Builder<ItemStack> visibleStacks = ImmutableList.builder();
		for (GuiIngredientFast guiItemStack : guiIngredientList.getAllGuiIngredients()) {
			IIngredientListElement element = guiItemStack.getElement();
			if (element != null) {
				Object ingredient = element.getIngredient();
				if (ingredient instanceof ItemStack) {
					ItemStack itemStack = (ItemStack) ingredient;
					visibleStacks.add(itemStack);
				}
			}
		}
		return visibleStacks.build();
	}

	@Override
	public int getPageCount() {
		final int itemCount = ingredientFilter.size();
		final int stacksPerPage = guiIngredientList.size();
		if (stacksPerPage == 0) {
			return 1;
		}
		int pageCount = MathUtil.divideCeil(itemCount, stacksPerPage);
		pageCount = Math.max(1, pageCount);
		return pageCount;
	}

	@Override
	public int getPageNum() {
		final int stacksPerPage = guiIngredientList.size();
		if (stacksPerPage == 0) {
			return 1;
		}
		return SessionData.getFirstItemIndex() / stacksPerPage;
	}
}
