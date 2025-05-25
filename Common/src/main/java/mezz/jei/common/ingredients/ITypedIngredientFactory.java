package mezz.jei.common.ingredients;

import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;

import java.util.Optional;

@FunctionalInterface
public interface ITypedIngredientFactory {
	<T> Optional<ITypedIngredient<T>> createTypedIngredient(IIngredientType<T> ingredientType, T ingredient, boolean normalize);
}
