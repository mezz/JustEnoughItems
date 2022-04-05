package mezz.jei.common.ingredients;

import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IIngredientVisibility;

public class IngredientVisibilityDummy implements IIngredientVisibility {
	public static final IIngredientVisibility INSTANCE = new IngredientVisibilityDummy();

	private IngredientVisibilityDummy() {}

	@Override
	public <V> boolean isIngredientVisible(ITypedIngredient<V> typedIngredient) {
		return true;
	}

	@Override
	public <V> boolean isIngredientVisible(IIngredientType<V> ingredientType, V ingredient) {
		return true;
	}
}
