package mezz.jei.library.ingredients;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IEditModeConfig;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IIngredientVisibility;
import mezz.jei.core.config.IWorldConfig;

public class IngredientVisibility implements IIngredientVisibility {
	private final IngredientBlacklistInternal blacklist;
	private final IWorldConfig worldConfig;
	private final IEditModeConfig editModeConfig;
	private final IIngredientManager ingredientManager;

	public IngredientVisibility(
		IngredientBlacklistInternal blacklist,
		IWorldConfig worldConfig,
		IEditModeConfig editModeConfig,
		IIngredientManager ingredientManager
	) {
		this.blacklist = blacklist;
		this.worldConfig = worldConfig;
		this.editModeConfig = editModeConfig;
		this.ingredientManager = ingredientManager;
	}

	@Override
	public <V> boolean isIngredientVisible(ITypedIngredient<V> typedIngredient) {
		IIngredientType<V> ingredientType = typedIngredient.getType();
		IIngredientHelper<V> ingredientHelper = ingredientManager.getIngredientHelper(ingredientType);
		return isIngredientVisible(typedIngredient, ingredientHelper);
	}

	@Override
	public <V> boolean isIngredientVisible(IIngredientType<V> ingredientType, V ingredient) {
		IIngredientHelper<V> ingredientHelper = ingredientManager.getIngredientHelper(ingredientType);
		return TypedIngredient.createAndFilterInvalid(ingredientManager, ingredientType, ingredient)
			.map(i -> isIngredientVisible(i, ingredientHelper))
			.orElse(false);
	}

	public <V> boolean isIngredientVisible(ITypedIngredient<V> typedIngredient, IIngredientHelper<V> ingredientHelper) {
		if (blacklist.isIngredientBlacklistedByApi(typedIngredient, ingredientHelper)) {
			return false;
		}
		if (!ingredientHelper.isIngredientOnServer(typedIngredient.getIngredient())) {
			return false;
		}
		return worldConfig.isEditModeEnabled() || !editModeConfig.isIngredientHiddenUsingConfigFile(typedIngredient);
	}
}
