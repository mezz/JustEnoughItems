package mezz.jei.library.ingredients;

import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.RecipeIngredientRole;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

public interface IIngredientSupplier {
	Stream<? extends IIngredientType<?>> getIngredientTypes(RecipeIngredientRole role);

	<T> Stream<T> getIngredientStream(IIngredientType<T> ingredientType, RecipeIngredientRole role);

	Collection<Optional<ITypedIngredient<?>>> getIngredients(RecipeIngredientRole role);
}
