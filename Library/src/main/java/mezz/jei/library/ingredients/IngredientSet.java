package mezz.jei.library.ingredients;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.IIngredientTypeWithSubtypes;
import mezz.jei.api.ingredients.subtypes.UidContext;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class IngredientSet<V> extends AbstractSet<V> {
	private final IIngredientHelper<V> ingredientHelper;
	private final UidContext context;
	private final Map<Object, V> ingredients;

	public IngredientSet(IIngredientHelper<V> ingredientHelper, UidContext context) {
		this.ingredientHelper = ingredientHelper;
		this.context = context;
		this.ingredients = new LinkedHashMap<>();
	}

	private Object getUid(V ingredient) {
		IIngredientType<V> ingredientType = ingredientHelper.getIngredientType();
		if (ingredientType instanceof IIngredientTypeWithSubtypes<?,V> ingredientTypeWithSubtypes) {
			if (!ingredientHelper.hasSubtypes(ingredient)) {
				return ingredientTypeWithSubtypes.getBase(ingredient);
			}
		}
		return ingredientHelper.getUniqueId(ingredient, context);
	}

	@Override
	public boolean add(V v) {
		Object uid = getUid(v);
		return ingredients.put(uid, v) == null;
	}

	@Override
	public boolean remove(Object o) {
		//noinspection unchecked
		Object uid = getUid((V) o);
		return ingredients.remove(uid) != null;
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
		//noinspection unchecked
		Object uid = getUid((V) o);
		return ingredients.containsKey(uid);
	}

	public Optional<V> getByUid(String uid) {
		V v = ingredients.get(uid);
		return Optional.ofNullable(v);
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
