package mezz.jei.ingredients;

import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.runtime.IIngredientFilter;
import mezz.jei.config.IWorldConfig;
import mezz.jei.util.ErrorUtil;

import java.util.List;

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
	public <T> List<T> getFilteredIngredients(IIngredientType<T> ingredientType) {
		String filterText = worldConfig.getFilterText();
		return ingredientFilter.getFilteredIngredients(filterText, ingredientType);
	}
}
