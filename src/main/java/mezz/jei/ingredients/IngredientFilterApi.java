package mezz.jei.ingredients;

import com.google.common.collect.ImmutableList;
import mezz.jei.api.IIngredientFilter;
import mezz.jei.config.IFilterTextSource;
import mezz.jei.util.ErrorUtil;

/** Connects the public filter operations to a search index and its text state. */
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
		if (filterTextSource.setFilterText(filterText)) {
			ingredientFilter.notifyListenersOfChange();
		}
	}

	@Override
	public ImmutableList<Object> getFilteredIngredients() {
		return ingredientFilter.getFilteredIngredients(filterTextSource.getFilterText());
	}
}
