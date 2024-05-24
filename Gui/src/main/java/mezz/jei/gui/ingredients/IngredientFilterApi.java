package mezz.jei.gui.ingredients;

import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.runtime.IIngredientFilter;
import mezz.jei.gui.filter.IFilterTextSource;
import net.minecraft.world.item.ItemStack;
import mezz.jei.common.util.ErrorUtil;

import java.util.Comparator;
import java.util.List;

public class IngredientFilterApi implements IIngredientFilter {
	private final IngredientFilter ingredientFilter;
	private final IFilterTextSource filterTextSource;

	public IngredientFilterApi(IngredientFilter ingredientFilter, IFilterTextSource filterTextSource) {
		this.ingredientFilter = ingredientFilter;
		this.filterTextSource = filterTextSource;
	}

	@Override
	public String getFilterText() {
		return filterTextSource.getFilterText();
	}

	@Override
	public void setFilterText(String filterText) {
		ErrorUtil.checkNotNull(filterText, "filterText");
		filterTextSource.setFilterText(filterText);
	}

	@Override
	public <T> List<T> getFilteredIngredients(IIngredientType<T> ingredientType) {
		return ingredientFilter.getFilteredIngredients(ingredientType);
	}

	@Override
	public void addIngredientListItemStackSorter(String name, Comparator<ItemStack> comparator) {
		ingredientFilter.addIngredientListItemStackSorter(name, comparator);
	}

}
