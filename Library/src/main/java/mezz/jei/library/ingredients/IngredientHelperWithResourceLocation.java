package mezz.jei.library.ingredients;

import mezz.jei.api.ingredients.IIngredientHelper;

public interface IngredientHelperWithResourceLocation<V> extends IIngredientHelper<V> {
	@Override
	default String getModId(V ingredient) {
		return getResourceLocation(ingredient).getNamespace();
	}

	@Override
	default String getResourceId(V ingredient) {
		return getResourceLocation(ingredient).getPath();
	}
}
