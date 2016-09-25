package mezz.jei.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mezz.jei.IngredientRegistry;
import mezz.jei.Internal;
import mezz.jei.api.ingredients.IIngredientHelper;

public class UniqueIngredientListBuilder<T> {
	private final IIngredientHelper<T> ingredientHelper;
	private final List<T> ingredients = new ArrayList<T>();
	private final Set<String> ingredientUids = new HashSet<String>();

	public UniqueIngredientListBuilder(Class<T> ingredientClass) {
		IngredientRegistry ingredientRegistry = Internal.getIngredientRegistry();
		ingredientHelper = ingredientRegistry.getIngredientHelper(ingredientClass);
	}

	public void add(T ingredient) {
		String uid = ingredientHelper.getUniqueId(ingredient);
		if (!ingredientUids.contains(uid)) {
			ingredientUids.add(uid);
			ingredients.add(ingredient);
		}
	}

	public List<T> build() {
		return ingredients;
	}
}
