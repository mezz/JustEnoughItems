package mezz.jei.common.ingredients.group;

import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IIngredientManager;

import java.util.function.Predicate;

public record DynamicSelector<T>(
		IIngredientType<T> ingredientType,
		Predicate<T> selector
) implements IIngredientGroupSelector {

	@Override
	public IngredientGroupType getType() {
		return IngredientGroupType.DYNAMIC;
	}

	@Override
	public boolean test(ITypedIngredient<?> ingredient, IIngredientManager ingredientManager) {
		T cast = ingredient.getCastIngredient(ingredientType);
		if (cast == null) {
			return false;
		}
		return selector.test(cast);
	}
}
