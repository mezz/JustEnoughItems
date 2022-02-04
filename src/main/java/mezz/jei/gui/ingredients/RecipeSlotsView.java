package mezz.jei.gui.ingredients;

import mezz.jei.api.gui.builder.IRecipeSlotId;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.RecipeIngredientRole;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class RecipeSlotsView implements IRecipeSlotsView {
	private final List<IRecipeSlotView> slots;

	public RecipeSlotsView(List<? extends IRecipeSlotView> slots) {
		this.slots = Collections.unmodifiableList(slots);
	}

	@Override
	public List<IRecipeSlotView> getSlotViews() {
		return this.slots;
	}

	@Override
	public Optional<IRecipeSlotView> findSlotView(IRecipeSlotId recipeSlotId) {
		return this.slots.stream()
			.filter(slot -> recipeSlotId.equals(slot.getSlotId()))
			.findFirst();
	}

	@Override
	public List<IRecipeSlotView> getSlotViews(RecipeIngredientRole role, IIngredientType<?> ingredientType) {
		return this.slots.stream()
			.filter(slotView -> slotView.getRole() == role)
			.filter(slotView -> slotView.getIngredients(ingredientType).findAny().isPresent())
			.toList();
	}
}
