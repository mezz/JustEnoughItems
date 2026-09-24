package mezz.jei.library.ingredients;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.ingredients.subtypes.UidContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Indexes registered ingredients by their exact and grouping UIDs.
 */
final class RegisteredIngredientIndex<T> {
	private static final Logger LOGGER = LogManager.getLogger();

	private final IIngredientHelper<T> ingredientHelper;
	private final Map<Object, ITypedIngredient<T>> ingredientsByUid = new LinkedHashMap<>();
	private final Map<Object, List<ITypedIngredient<T>>> ingredientsByGroupingUid = new HashMap<>();

	RegisteredIngredientIndex(IIngredientHelper<T> ingredientHelper) {
		this.ingredientHelper = ingredientHelper;
	}

	public void addAll(Collection<ITypedIngredient<T>> ingredients) {
		ingredients.forEach(this::add);
	}

	public void add(ITypedIngredient<T> ingredient) {
		@Nullable
		Object uid = getUid(ingredient);
		if (uid == null) {
			return;
		}
		@Nullable
		Object groupingUid = getGroupingUid(ingredient);

		ITypedIngredient<T> previous = ingredientsByUid.put(uid, ingredient);
		if (previous == null) {
			addToGroup(groupingUid, ingredient);
			return;
		}

		@Nullable
		Object previousGroupingUid = getGroupingUid(previous);
		if (Objects.equals(previousGroupingUid, groupingUid)) {
			replaceInGroup(groupingUid, previous, ingredient);
		} else {
			removeFromGroup(previousGroupingUid, previous);
			addToGroup(groupingUid, ingredient);
		}
	}

	public void removeAll(Collection<ITypedIngredient<T>> ingredients) {
		ingredients.forEach(this::remove);
	}

	public void remove(ITypedIngredient<T> ingredient) {
		@Nullable
		Object uid = getUid(ingredient);
		if (uid == null) {
			return;
		}
		ITypedIngredient<T> removed = getIngredientByUid(uid);
		if (removed != null) {
			ingredientsByUid.remove(uid);
			@Nullable
			Object groupingUid = getGroupingUid(removed);
			removeFromGroup(groupingUid, removed);
		}
	}

	@Nullable
	public ITypedIngredient<T> getIngredientByUid(Object uid) {
		return ingredientsByUid.get(uid);
	}

	@Unmodifiable
	public List<ITypedIngredient<T>> getIngredientsByGroupingUid(Object groupingUid) {
		List<ITypedIngredient<T>> ingredients = ingredientsByGroupingUid.get(groupingUid);
		if (ingredients == null) {
			return List.of();
		}
		return Collections.unmodifiableList(ingredients);
	}

	@Unmodifiable
	public Collection<ITypedIngredient<T>> getAllIngredients() {
		return Collections.unmodifiableCollection(ingredientsByUid.values());
	}

	private void addToGroup(@Nullable Object groupingUid, ITypedIngredient<T> ingredient) {
		if (groupingUid != null) {
			ingredientsByGroupingUid.computeIfAbsent(groupingUid, key -> new ArrayList<>())
				.add(ingredient);
		}
	}

	private void replaceInGroup(
		@Nullable Object groupingUid,
		ITypedIngredient<T> previous,
		ITypedIngredient<T> replacement
	) {
		if (groupingUid == null) {
			return;
		}
		List<ITypedIngredient<T>> group = ingredientsByGroupingUid.get(groupingUid);
		if (group == null) {
			addToGroup(groupingUid, replacement);
			return;
		}
		for (int i = 0; i < group.size(); i++) {
			if (group.get(i) == previous) {
				group.set(i, replacement);
				return;
			}
		}
		group.add(replacement);
	}

	private void removeFromGroup(@Nullable Object groupingUid, ITypedIngredient<T> ingredient) {
		if (groupingUid == null) {
			return;
		}
		List<ITypedIngredient<T>> group = ingredientsByGroupingUid.get(groupingUid);
		if (group == null) {
			return;
		}
		for (int i = 0; i < group.size(); i++) {
			if (group.get(i) == ingredient) {
				group.remove(i);
				if (group.isEmpty()) {
					ingredientsByGroupingUid.remove(groupingUid);
				}
				return;
			}
		}
	}

	@Nullable
	private Object getUid(ITypedIngredient<T> ingredient) {
		try {
			return ingredientHelper.getUid(ingredient, UidContext.Ingredient);
		} catch (RuntimeException e) {
			logBrokenIngredient(ingredient, e);
			return null;
		}
	}

	@Nullable
	private Object getGroupingUid(ITypedIngredient<T> ingredient) {
		try {
			return ingredientHelper.getGroupingUid(ingredient);
		} catch (RuntimeException e) {
			logBrokenIngredient(ingredient, e);
			return null;
		}
	}

	private void logBrokenIngredient(ITypedIngredient<T> ingredient, RuntimeException e) {
		try {
			String ingredientInfo = ingredientHelper.getErrorInfo(ingredient.getIngredient());
			LOGGER.warn("Found a broken ingredient {}", ingredientInfo, e);
		} catch (RuntimeException e2) {
			LOGGER.warn("Found a broken ingredient.", e2);
		}
	}
}
