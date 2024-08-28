package mezz.jei.library.ingredients;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.runtime.IIngredientManager;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class IngredientBlacklistInternal implements IIngredientManager.IIngredientListener {
	private final Set<String> uidBlacklist = new HashSet<>();
	private WeakReference<IngredientVisibility> ingredientVisibilityRef = new WeakReference<>(null);

	public <V> void addIngredientToBlacklist(ITypedIngredient<V> typedIngredient, IIngredientHelper<V> ingredientHelper) {
		V ingredient = typedIngredient.getIngredient();
		String uniqueName = ingredientHelper.getUniqueId(ingredient, UidContext.Ingredient);
		if (uidBlacklist.add(uniqueName)) {
			notifyListenersOfVisibilityChange(typedIngredient, false);
		}
	}

	public <V> void removeIngredientFromBlacklist(ITypedIngredient<V> typedIngredient, IIngredientHelper<V> ingredientHelper) {
		V ingredient = typedIngredient.getIngredient();
		String uniqueName = ingredientHelper.getUniqueId(ingredient, UidContext.Ingredient);
		if (uidBlacklist.remove(uniqueName)) {
			notifyListenersOfVisibilityChange(typedIngredient, true);
		}
	}

	public <V> boolean isIngredientBlacklistedByApi(ITypedIngredient<V> typedIngredient, IIngredientHelper<V> ingredientHelper) {
		V ingredient = typedIngredient.getIngredient();
		String uid = ingredientHelper.getUniqueId(ingredient, UidContext.Ingredient);
		String uidWild = ingredientHelper.getWildcardId(ingredient);

		if (uid.equals(uidWild)) {
			return uidBlacklist.contains(uid);
		}
		return uidBlacklist.contains(uid) || uidBlacklist.contains(uidWild);
	}

	public void registerListener(IngredientVisibility ingredientVisibility) {
		this.ingredientVisibilityRef = new WeakReference<>(ingredientVisibility);
	}

	@Override
	public <V> void onIngredientsAdded(IIngredientHelper<V> ingredientHelper, Collection<ITypedIngredient<V>> ingredients) {
		for (ITypedIngredient<V> ingredient : ingredients) {
			removeIngredientFromBlacklist(ingredient, ingredientHelper);
		}
	}

	@Override
	public <V> void onIngredientsRemoved(IIngredientHelper<V> ingredientHelper, Collection<ITypedIngredient<V>> ingredients) {
		for (ITypedIngredient<V> ingredient : ingredients) {
			addIngredientToBlacklist(ingredient, ingredientHelper);
		}
	}

	private <T> void notifyListenersOfVisibilityChange(ITypedIngredient<T> ingredient, boolean visible) {
		IngredientVisibility ingredientVisibility = ingredientVisibilityRef.get();
		if (ingredientVisibility != null) {
			ingredientVisibility.notifyListeners(ingredient, visible);
		}
	}
}
