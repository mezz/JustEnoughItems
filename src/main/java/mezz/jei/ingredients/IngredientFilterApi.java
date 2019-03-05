package mezz.jei.ingredients;

import com.google.common.collect.ImmutableList;
import mezz.jei.api.runtime.IIngredientFilter;
import mezz.jei.config.IWorldConfig;
import mezz.jei.util.ErrorUtil;

public class IngredientFilterApi implements IIngredientFilter {
	private final IngredientFilter ingredientFilter;
	private final IWorldConfig worldConfig;

	public IngredientFilterApi(IngredientFilter ingredientFilter, IWorldConfig worldConfig) {
		this.ingredientFilter = ingredientFilter;
		this.worldConfig = worldConfig;
	}

	@Override
	public String getFilterText() {
		return worldConfig.getFilterText();
	}

	@Override
	public void setFilterText(String filterText) {
		ErrorUtil.checkNotNull(filterText, "filterText");
		if (worldConfig.setFilterText(filterText)) {
			ingredientFilter.notifyListenersOfChange();
		}
	}

	@Override
	public ImmutableList<Object> getFilteredIngredients() {
		String filterText = worldConfig.getFilterText();
		return ingredientFilter.getFilteredIngredients(filterText);
	}
}
