package mezz.jei.config;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.ingredients.IngredientFilter;

public interface IEditModeConfig {
	<V> boolean isIngredientOnConfigBlacklist(ITypedIngredient<V> ingredient, IIngredientHelper<V> ingredientHelper);

	<V> void addIngredientToConfigBlacklist(IngredientFilter ingredientFilter, IIngredientManager ingredientManager, ITypedIngredient<V> ingredient, IngredientBlacklistType blacklistType, IIngredientHelper<V> ingredientHelper);

	<V> void removeIngredientFromConfigBlacklist(IngredientFilter ingredientFilter, IIngredientManager ingredientManager, ITypedIngredient<V> ingredient, IngredientBlacklistType blacklistType, IIngredientHelper<V> ingredientHelper);
}
