package mezz.jei.library.ingredients;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IIngredientVisibility;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class IngredientBlacklistInternal implements IIngredientManager.IIngredientListener, IIngredientVisibility.IListener {
	private final IIngredientManager ingredientManager;
	private final Set<Object> removedIngredientUids = new HashSet<>();
	private final Map<UidContext, Set<Object>> hiddenIngredientUids = new EnumMap<>(UidContext.class);
	private WeakReference<IngredientVisibility> ingredientVisibilityRef = new WeakReference<>(null);

	public IngredientBlacklistInternal(IIngredientManager ingredientManager) {
		this.ingredientManager = ingredientManager;
	}

	public <V> boolean isIngredientBlacklistedByApi(
		ITypedIngredient<V> typedIngredient,
		IIngredientHelper<V> ingredientHelper,
		UidContext context
	) {
		if (isIngredientRemoved(typedIngredient, ingredientHelper)) {
			return true;
		}
		return isIngredientHidden(typedIngredient, ingredientHelper, context);
	}

	private <V> boolean isIngredientRemoved(
		ITypedIngredient<V> typedIngredient,
		IIngredientHelper<V> ingredientHelper
	) {
		Object uid = ingredientHelper.getUid(typedIngredient, UidContext.Ingredient);
		Object groupingUid = ingredientHelper.getGroupingUid(typedIngredient);
		return containsUid(removedIngredientUids, uid, groupingUid);
	}

	private <V> boolean isIngredientHidden(
		ITypedIngredient<V> typedIngredient,
		IIngredientHelper<V> ingredientHelper,
		UidContext context
	) {
		Set<Object> hiddenUids = hiddenIngredientUids.get(context);
		if (hiddenUids == null) {
			return false;
		}
		Object uid = ingredientHelper.getUid(typedIngredient, context);
		Object groupingUid = ingredientHelper.getGroupingUid(typedIngredient);
		return containsUid(hiddenUids, uid, groupingUid);
	}

	private static boolean containsUid(Set<Object> uids, Object uid, Object groupingUid) {
		if (uid.equals(groupingUid)) {
			return uids.contains(uid);
		}
		return uids.contains(uid) || uids.contains(groupingUid);
	}

	public void registerListener(IngredientVisibility ingredientVisibility) {
		this.ingredientVisibilityRef = new WeakReference<>(ingredientVisibility);
	}

	@Override
	public <V> void onIngredientsAdded(IIngredientHelper<V> ingredientHelper, Collection<ITypedIngredient<V>> ingredients) {
		for (ITypedIngredient<V> ingredient : ingredients) {
			Object uid = ingredientHelper.getUid(ingredient, UidContext.Ingredient);
			if (removedIngredientUids.remove(uid)) {
				Set<UidContext> visibleContexts = EnumSet.allOf(UidContext.class);
				visibleContexts.removeIf(context -> isIngredientHidden(ingredient, ingredientHelper, context));
				if (!visibleContexts.isEmpty()) {
					notifyListenersOfVisibilityChange(ingredient, visibleContexts, true);
				}
			}
		}
	}

	@Override
	public <V> void onIngredientsRemoved(IIngredientHelper<V> ingredientHelper, Collection<ITypedIngredient<V>> ingredients) {
		for (ITypedIngredient<V> ingredient : ingredients) {
			Object uid = ingredientHelper.getUid(ingredient, UidContext.Ingredient);
			if (removedIngredientUids.add(uid)) {
				Set<UidContext> changedContexts = EnumSet.allOf(UidContext.class);
				changedContexts.removeIf(context -> isIngredientHidden(ingredient, ingredientHelper, context));
				if (!changedContexts.isEmpty()) {
					notifyListenersOfVisibilityChange(ingredient, changedContexts, false);
				}
			}
		}
	}

	@Override
	public <V> void onIngredientVisibilityChanged(ITypedIngredient<V> ingredient, boolean visible) {
		onIngredientsVisibilityChanged(
			Set.of(ingredient),
			Set.of(UidContext.values()),
			visible
		);
	}

	@Override
	public <V> void onIngredientsVisibilityChanged(
		Collection<ITypedIngredient<V>> ingredients,
		Collection<UidContext> contexts,
		boolean visible
	) {
		for (ITypedIngredient<V> ingredient : ingredients) {
			IIngredientHelper<V> ingredientHelper = ingredientManager.getIngredientHelper(ingredient.getType());
			Set<UidContext> changedContexts = updateHiddenContexts(ingredient, ingredientHelper, contexts, visible);
			if (!isIngredientRemoved(ingredient, ingredientHelper) && !changedContexts.isEmpty()) {
				notifyListenersOfVisibilityChange(ingredient, changedContexts, visible);
			}
		}
	}

	private <V> Set<UidContext> updateHiddenContexts(
		ITypedIngredient<V> ingredient,
		IIngredientHelper<V> ingredientHelper,
		Collection<UidContext> contexts,
		boolean visible
	) {
		Set<UidContext> changedContexts = EnumSet.noneOf(UidContext.class);
		for (UidContext context : contexts) {
			Object uid = ingredientHelper.getUid(ingredient, context);
			Set<Object> hiddenUids = hiddenIngredientUids.computeIfAbsent(context, key -> new HashSet<>());
			boolean changed;
			if (visible) {
				changed = hiddenUids.remove(uid);
				if (hiddenUids.isEmpty()) {
					hiddenIngredientUids.remove(context);
				}
			} else {
				changed = hiddenUids.add(uid);
			}
			if (changed) {
				changedContexts.add(context);
			}
		}
		return changedContexts;
	}

	private <T> void notifyListenersOfVisibilityChange(
		ITypedIngredient<T> ingredient,
		Collection<UidContext> contexts,
		boolean visible
	) {
		IngredientVisibility ingredientVisibility = ingredientVisibilityRef.get();
		if (ingredientVisibility != null) {
			ingredientVisibility.notifyListeners(Set.of(ingredient), contexts, visible);
		}
	}
}
