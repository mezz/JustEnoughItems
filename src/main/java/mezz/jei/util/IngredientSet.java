package mezz.jei.util;

import java.util.AbstractSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import mezz.jei.api.ingredients.IIngredientHelper;

public class IngredientSet<V> extends AbstractSet<V> {
	private final IIngredientHelper<V> ingredientHelper;
	private final Map<String, V> ingredients;
	
	public IngredientSet(IIngredientHelper<V> ingredientHelper) {
		this.ingredientHelper = ingredientHelper;
		this.ingredients = new HashMap<String, V>();
	}
	
	@Override
	public boolean add(V v) {
		String uid = ingredientHelper.getUniqueId(v);
		return ingredients.put(uid, v) == null;
	}
	
	@Override
	public boolean remove(Object o) {
		//noinspection unchecked
		String uid = ingredientHelper.getUniqueId((V) o);
		return ingredients.remove(uid) != null;
	}
	
	@Override
	public boolean contains(Object o) {
		//noinspection unchecked
		String uid = ingredientHelper.getUniqueId((V) o);
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
