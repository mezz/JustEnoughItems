package mezz.jei.library.ingredients;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IEditModeConfig;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IIngredientVisibility;
import mezz.jei.core.config.IWorldConfig;
import mezz.jei.core.util.WeakList;
import mezz.jei.library.config.EditModeConfig;

public class IngredientVisibility implements IIngredientVisibility {
	private final IngredientBlacklistInternal blacklist;
	private final IWorldConfig worldConfig;
	private final IEditModeConfig editModeConfig;
	private final IIngredientManager ingredientManager;
	private final WeakList<IListener> listeners = new WeakList<>();

	public IngredientVisibility(
		IngredientBlacklistInternal blacklist,
		IWorldConfig worldConfig,
		EditModeConfig editModeConfig,
		IIngredientManager ingredientManager
	) {
		this.blacklist = blacklist;
		this.worldConfig = worldConfig;
		this.editModeConfig = editModeConfig;
		this.ingredientManager = ingredientManager;

		blacklist.registerListener(this::notifyListenersOfVisibilityChange);
		editModeConfig.registerListener(this::notifyListenersOfVisibilityChange);
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

	@Override
	public void registerListener(IListener listener) {
		this.listeners.add(listener);
	}

	private <T> void notifyListenersOfVisibilityChange(ITypedIngredient<T> ingredient, boolean visible) {
		listeners.forEach(listener -> listener.onIngredientVisibilityChanged(ingredient, visible));
	}
}
