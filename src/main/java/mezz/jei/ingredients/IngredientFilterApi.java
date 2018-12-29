package mezz.jei.ingredients;

import com.google.common.collect.ImmutableList;
import mezz.jei.api.IIngredientFilter;
import mezz.jei.config.ClientConfig;
import mezz.jei.util.ErrorUtil;

public class IngredientFilterApi implements IIngredientFilter {
	private final IngredientFilter ingredientFilter;

	public IngredientFilterApi(IngredientFilter ingredientFilter) {
		this.ingredientFilter = ingredientFilter;
	}

	@Override
	public String getFilterText() {
		return ClientConfig.getInstance().getFilterText();
	}

	@Override
	public void setFilterText(String filterText) {
		ErrorUtil.checkNotNull(filterText, "filterText");
		if (ClientConfig.getInstance().setFilterText(filterText)) {
			ingredientFilter.notifyListenersOfChange();
		}
	}

	@Override
	public ImmutableList<Object> getFilteredIngredients() {
		String filterText = ClientConfig.getInstance().getFilterText();
		return ingredientFilter.getFilteredIngredients(filterText);
	}
}
