package mezz.jei.ingredients;

import java.util.HashMap;
import java.util.Map;

import mezz.jei.api.ingredients.IIngredientHelper;

public class IngredientOrderTracker {
	private final Map<String, Integer> wildcardAddedOrder = new HashMap<>();
	private int addedIndex = 0;

	public <V> int getOrderIndex(V ingredient, IIngredientHelper<V> ingredientHelper) {
		String uid = ingredientHelper.getWildcardId(ingredient);
		if (wildcardAddedOrder.containsKey(uid)) {
			return wildcardAddedOrder.get(uid);
		} else {
			int index = addedIndex;
			wildcardAddedOrder.put(uid, index);
			addedIndex++;
			return index;
		}
	}
}
