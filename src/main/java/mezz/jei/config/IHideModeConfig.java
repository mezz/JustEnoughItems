package mezz.jei.config;

import mezz.jei.api.ingredients.IIngredientHelper;

public interface IHideModeConfig {
	boolean isHideModeEnabled();

	<V> boolean isIngredientOnConfigBlacklist(V ingredient, IIngredientHelper<V> ingredientHelper);
}
