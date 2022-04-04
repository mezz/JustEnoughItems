package mezz.jei.common.ingredients;

import org.jetbrains.annotations.Nullable;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.subtypes.UidContext;

public class IngredientSet<V> extends AbstractSet<V> {
	public static <V> IngredientSet<V> create(IIngredientHelper<V> ingredientHelper, UidContext context) {
		final Function<V, String> uidGenerator = v -> ingredientHelper.getUniqueId(v, context);
		return new IngredientSet<>(uidGenerator);
	}

	private final Function<V, String> uidGenerator;
	private final Map<String, V> ingredients;

	private IngredientSet(Function<V, String> uidGenerator) {
		this.uidGenerator = uidGenerator;
		this.ingredients = new LinkedHashMap<>();
	}

	@Override
	public boolean add(V v) {
		String uid = uidGenerator.apply(v);
		return ingredients.put(uid, v) == null;
	}

	@Override
	public boolean remove(Object o) {
		//noinspection unchecked
		String uid = uidGenerator.apply((V) o);
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
		String uid = uidGenerator.apply((V) o);
		return ingredients.containsKey(uid);
	}

	@Nullable
	public V getByUid(String uid) {
		return ingredients.get(uid);
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
