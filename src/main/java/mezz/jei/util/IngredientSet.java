package mezz.jei.util;

import mezz.jei.Internal;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.startup.StackHelper;
import net.minecraft.item.ItemStack;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class IngredientSet<V> extends AbstractSet<V> {
	public static <V> IngredientSet<V> create(Class<V> ingredientClass, IIngredientHelper<V> ingredientHelper) {
		final UidGenerator<V> uidGenerator;
		if (ingredientClass == ItemStack.class) {
			StackHelper stackHelper = Internal.getStackHelper();
			uidGenerator = stack -> stackHelper.getUniqueIdentifierForStack((ItemStack) stack, StackHelper.UidMode.FULL);
		} else {
			uidGenerator = ingredientHelper::getUniqueId;
		}
		return new IngredientSet<>(uidGenerator);
	}

	private interface UidGenerator<V> {
		String getUniqueId(V ingredient);
	}

	private final UidGenerator<V> uidGenerator;
	private final Map<String, V> ingredients;

	private IngredientSet(UidGenerator<V> uidGenerator) {
		this.uidGenerator = uidGenerator;
		this.ingredients = new LinkedHashMap<>();
	}

	@Override
	public boolean add(V v) {
		String uid = uidGenerator.getUniqueId(v);
		return ingredients.put(uid, v) == null;
	}

	@Override
	public boolean remove(Object o) {
		//noinspection unchecked
		String uid = uidGenerator.getUniqueId((V) o);
		return ingredients.remove(uid) != null;
	}

	@Override
	public boolean contains(Object o) {
		//noinspection unchecked
		String uid = uidGenerator.getUniqueId((V) o);
		return ingredients.containsKey(uid);
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
