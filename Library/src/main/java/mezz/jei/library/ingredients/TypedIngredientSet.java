package mezz.jei.library.ingredients;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.ingredients.subtypes.UidContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.Nullable;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class TypedIngredientSet<T> extends AbstractSet<ITypedIngredient<T>> {
	private static final Logger LOGGER = LogManager.getLogger();

	private final IIngredientHelper<T> ingredientHelper;
	private final UidContext context;
	private final Map<Object, ITypedIngredient<T>> ingredients;

	public TypedIngredientSet(IIngredientHelper<T> ingredientHelper, UidContext context) {
		this.ingredientHelper = ingredientHelper;
		this.context = context;
		this.ingredients = new LinkedHashMap<>();
	}

	@Nullable
	private Object getUid(ITypedIngredient<T> typedIngredient) {
		try {
			return ingredientHelper.getUid(typedIngredient, context);
		} catch (RuntimeException e) {
			try {
				String ingredientInfo = ingredientHelper.getErrorInfo(typedIngredient.getIngredient());
				LOGGER.warn("Found a broken ingredient {}", ingredientInfo, e);
			} catch (RuntimeException e2) {
				LOGGER.warn("Found a broken ingredient.", e2);
			}
			return null;
		}
	}

	@Override
	public boolean add(ITypedIngredient<T> value) {
		Object uid = getUid(value);
		return uid != null && ingredients.put(uid, value) == null;
	}

	@Override
	public boolean remove(Object value) {
		if (value instanceof ITypedIngredient<?> typedIngredient) {
			IIngredientType<?> type = typedIngredient.getType();
			if (this.ingredientHelper.getIngredientType().equals(type)) {
				@SuppressWarnings("unchecked")
				ITypedIngredient<T> cast = (ITypedIngredient<T>) typedIngredient;
				Object uid = getUid(cast);
				return uid != null && ingredients.remove(uid) != null;
			}
		}
		return false;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		Objects.requireNonNull(c);
		boolean modified = false;
		for (Object value : c) {
			modified |= remove(value);
		}
		return modified;
	}

	@Override
	public boolean addAll(Collection<? extends ITypedIngredient<T>> c) {
		Objects.requireNonNull(c);
		boolean modified = false;
		for (ITypedIngredient<T> value : c) {
			modified |= add(value);
		}
		return modified;
	}

	@Override
	public boolean contains(Object value) {
		if (value instanceof ITypedIngredient<?> typedIngredient) {
			IIngredientType<?> type = typedIngredient.getType();
			if (this.ingredientHelper.getIngredientType().equals(type)) {
				@SuppressWarnings("unchecked")
				ITypedIngredient<T> cast = (ITypedIngredient<T>) typedIngredient;
				Object uid = getUid(cast);
				return uid != null && ingredients.containsKey(uid);
			}
		}
		return false;
	}

	@Override
	public void clear() {
		ingredients.clear();
	}

	@Override
	public Iterator<ITypedIngredient<T>> iterator() {
		return ingredients.values().iterator();
	}

	@Override
	public int size() {
		return ingredients.size();
	}
}
