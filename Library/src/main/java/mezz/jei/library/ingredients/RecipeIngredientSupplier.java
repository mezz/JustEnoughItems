package mezz.jei.library.ingredients;

import mezz.jei.api.ingredients.IIngredientSupplier;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.RecipeIngredientRole;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Ingredients collected from a recipe layout, including the slot display data needed when recipes are indexed.
 */
public class RecipeIngredientSupplier implements IIngredientSupplier {
	private final Map<RecipeIngredientRole, List<SlotIngredient<?>>> ingredientsByRole;

	public RecipeIngredientSupplier(Map<RecipeIngredientRole, List<SlotIngredient<?>>> ingredientsByRole) {
		EnumMap<RecipeIngredientRole, List<SlotIngredient<?>>> copiedIngredients = new EnumMap<>(RecipeIngredientRole.class);
		ingredientsByRole.forEach((role, ingredients) -> copiedIngredients.put(role, List.copyOf(ingredients)));
		this.ingredientsByRole = Collections.unmodifiableMap(copiedIngredients);
	}

	@Override
	public List<ITypedIngredient<?>> getIngredients(RecipeIngredientRole role) {
		return getSlotIngredients(role).stream()
			.<ITypedIngredient<?>>map(SlotIngredient::typedIngredient)
			.toList();
	}

	public List<SlotIngredient<?>> getSlotIngredients(RecipeIngredientRole role) {
		return ingredientsByRole.getOrDefault(role, List.of());
	}
}
