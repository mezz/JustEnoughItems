package mezz.jei.library.ingredients;

import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.RecipeIngredientRole;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;

public interface IIngredientSupplier {
	@Unmodifiable
	Collection<ITypedIngredient<?>> getIngredients(RecipeIngredientRole role);
}
