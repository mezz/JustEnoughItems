package mezz.jei.library.gui.recipes;

import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.library.gui.recipes.layout.builder.RecipeSlotIngredients;
import mezz.jei.library.ingredients.IIngredientSupplier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class RecipeLayoutIngredientSupplier implements IIngredientSupplier {
	private final List<RecipeSlotIngredients> slots;

	public RecipeLayoutIngredientSupplier(List<RecipeSlotIngredients> slots) {
		this.slots = slots;
	}

	@Override
	public Stream<? extends IIngredientType<?>> getIngredientTypes(RecipeIngredientRole role) {
		return slots.stream()
			.filter(slot -> slot.role() == role)
			.map(RecipeSlotIngredients::types)
			.flatMap(Collection::stream)
			.distinct();
	}

	@Override
	public <T> Stream<T> getIngredientStream(IIngredientType<T> ingredientType, RecipeIngredientRole role) {
		return slots.stream()
			.filter(slot -> slot.role() == role)
			.flatMap(slot -> slot.ingredients().stream())
			.map(ingredientType::castIngredient)
			.flatMap(Optional::stream);
	}

	@Override
	public Collection<Optional<ITypedIngredient<?>>> getIngredients(RecipeIngredientRole role) {
		List<Optional<ITypedIngredient<?>>> ingredients = new ArrayList<>();

		for (RecipeSlotIngredients slot : slots) {
			if (slot.role() == role) {
				ingredients.addAll(slot.ingredients());
			}
		}

		return ingredients;
	}
}
