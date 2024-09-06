package mezz.jei.library.gui.recipes;

import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.library.gui.recipes.supplier.builder.IngredientSlotBuilder;
import mezz.jei.library.ingredients.IIngredientSupplier;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class RecipeLayoutIngredientSupplier implements IIngredientSupplier {
	private final Map<RecipeIngredientRole, IngredientSlotBuilder> ingredientSlotBuilders;

	public RecipeLayoutIngredientSupplier(Map<RecipeIngredientRole, IngredientSlotBuilder> ingredientSlotBuilders) {
		this.ingredientSlotBuilders = ingredientSlotBuilders;
	}

	@Override
	public Collection<ITypedIngredient<?>> getIngredients(RecipeIngredientRole role) {
		IngredientSlotBuilder ingredientSlotBuilder = ingredientSlotBuilders.get(role);
		if (ingredientSlotBuilder == null) {
			return List.of();
		}
		return ingredientSlotBuilder.getAllIngredients();
	}
}
