package mezz.jei.library.ingredients;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.core.util.WeakList;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class IngredientBlacklistInternal implements IIngredientManager.IIngredientListener {
	public interface IListener {
		<V> void onIngredientVisibilityChanged(ITypedIngredient<V> ingredient, boolean visible);
	}

	private final Set<String> ingredientBlacklist = new HashSet<>();
	private final WeakList<IListener> listeners = new WeakList<>();

	public <V> void addIngredientToBlacklist(ITypedIngredient<V> typedIngredient, IIngredientHelper<V> ingredientHelper) {
		V ingredient = typedIngredient.getIngredient();
		String uniqueName = ingredientHelper.getUniqueId(ingredient, UidContext.Ingredient);
		if (ingredientBlacklist.add(uniqueName)) {
			notifyListenersOfVisibilityChange(typedIngredient, false);
		}
	}

	public <V> void removeIngredientFromBlacklist(ITypedIngredient<V> typedIngredient, IIngredientHelper<V> ingredientHelper) {
		V ingredient = typedIngredient.getIngredient();
		String uniqueName = ingredientHelper.getUniqueId(ingredient, UidContext.Ingredient);
		if (ingredientBlacklist.remove(uniqueName)) {
			notifyListenersOfVisibilityChange(typedIngredient, true);
		}
	}

	public <V> boolean isIngredientBlacklistedByApi(ITypedIngredient<V> typedIngredient, IIngredientHelper<V> ingredientHelper) {
		V ingredient = typedIngredient.getIngredient();
		String uid = ingredientHelper.getUniqueId(ingredient, UidContext.Ingredient);
		String uidWild = ingredientHelper.getWildcardId(ingredient);

		if (uid.equals(uidWild)) {
			return ingredientBlacklist.contains(uid);
		}
		return ingredientBlacklist.contains(uid) || ingredientBlacklist.contains(uidWild);
	}

	public void registerListener(IListener listener) {
		this.listeners.add(listener);
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
		listeners.forEach(listener -> listener.onIngredientVisibilityChanged(ingredient, visible));
	}
}
