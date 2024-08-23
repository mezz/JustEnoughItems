package mezz.jei.library.ingredients;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.subtypes.UidContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class IngredientSet<V> extends AbstractSet<V> {
	private static final Logger LOGGER = LogManager.getLogger();

	private final IIngredientHelper<V> ingredientHelper;
	private final UidContext context;
	private final Map<Object, V> ingredients;

	public IngredientSet(IIngredientHelper<V> ingredientHelper, UidContext context) {
		this.ingredientHelper = ingredientHelper;
		this.context = context;
		this.ingredients = new LinkedHashMap<>();
	}

	@Nullable
	private Object getUid(V ingredient) {
		try {
			return ingredientHelper.getUid(ingredient, context);
		} catch (RuntimeException e) {
			try {
				String ingredientInfo = ingredientHelper.getErrorInfo(ingredient);
				LOGGER.warn("Found a broken ingredient {}", ingredientInfo, e);
			} catch (RuntimeException e2) {
				LOGGER.warn("Found a broken ingredient.", e2);
			}
			return null;
		}
	}

	@Override
	public boolean add(V v) {
		Object uid = getUid(v);
		return uid != null && ingredients.put(uid, v) == null;
	}

	@Override
	public boolean remove(Object o) {
		//noinspection unchecked
		Object uid = getUid((V) o);
		return uid != null && ingredients.remove(uid) != null;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		if (c instanceof IngredientSet) {
			return super.removeAll(c);
		}
		Objects.requireNonNull(c);
		boolean modified = false;
		for (Object aC : c) {
			modified |= remove(aC);
		}
		return modified;
	}

	@Override
	public boolean contains(Object o) {
		IIngredientType<V> ingredientType = ingredientHelper.getIngredientType();
		Class<? extends V> ingredientClass = ingredientType.getIngredientClass();
		if (!ingredientClass.isInstance(o)) {
			return false;
		}
		V v = ingredientClass.cast(o);
		Object uid = getUid(v);
		return uid != null && ingredients.containsKey(uid);
	}

	@SuppressWarnings("removal")
	@Deprecated(forRemoval = true)
	public Optional<V> getByLegacyUid(String uid) {
		V v = ingredients.get(uid);
		if (v != null) {
			return Optional.of(v);
		}

		for (V ingredient : ingredients.values()) {
			String legacyUid = ingredientHelper.getUniqueId(ingredient, context);
			if (uid.equals(legacyUid)) {
				return Optional.of(ingredient);
			}
		}
		return Optional.empty();
	}

	@Override
	public void clear() {
		ingredients.clear();
	}

	@Override
	public Iterator<V> iterator() {
		return ingredients.values().iterator();
	}

	@Override
	public int size() {
		return ingredients.size();
	}
}
