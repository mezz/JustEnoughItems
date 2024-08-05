package mezz.jei.library.gui.recipes.layout.builder;

import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.RecipeIngredientRole;

import java.util.Collection;
import java.util.Optional;

public record RecipeSlotIngredients(
	RecipeIngredientRole role,
	Collection<Optional<ITypedIngredient<?>>> ingredients,
	Collection<IIngredientType<?>> types
) {}
