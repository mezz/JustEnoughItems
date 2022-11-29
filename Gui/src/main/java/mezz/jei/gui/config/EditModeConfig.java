package mezz.jei.gui.config;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.config.EditModeConfigInternal;
import mezz.jei.common.config.IEditModeConfig;

public class EditModeConfig implements IEditModeConfig {
	private final EditModeConfigInternal internal;
	private final IIngredientManager ingredientManager;

	public EditModeConfig(
		EditModeConfigInternal internal,
		IIngredientManager ingredientManager
	) {
		this.internal = internal;
		this.ingredientManager = ingredientManager;
	}

	@Override
	public <V> boolean isIngredientHiddenUsingConfigFile(ITypedIngredient<V> ingredient) {
		IIngredientType<V> type = ingredient.getType();
		IIngredientHelper<V> ingredientHelper = ingredientManager.getIngredientHelper(type);
		return internal.isIngredientOnConfigBlacklist(ingredient, ingredientHelper);
	}

	@Override
	public <V> void hideIngredientUsingConfigFile(ITypedIngredient<V> ingredient, Mode mode) {
		IIngredientType<V> type = ingredient.getType();
		IIngredientHelper<V> ingredientHelper = ingredientManager.getIngredientHelper(type);
		internal.addIngredientToConfigBlacklist(ingredient, mode, ingredientHelper);
	}

	@Override
	public <V> void showIngredientUsingConfigFile(ITypedIngredient<V> ingredient, Mode mode) {
		IIngredientType<V> type = ingredient.getType();
		IIngredientHelper<V> ingredientHelper = ingredientManager.getIngredientHelper(type);
		internal.removeIngredientFromConfigBlacklist(ingredient, mode, ingredientHelper);
	}
}
