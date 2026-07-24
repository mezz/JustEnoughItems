package mezz.jei.library.ingredients;

import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.RecipeIngredientRole;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

public interface IIngredientSupplier {
	@Unmodifiable
	List<ITypedIngredient<?>> getIngredients(RecipeIngredientRole role);
}
