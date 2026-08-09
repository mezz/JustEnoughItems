package mezz.jei.forge.tests.lib;

import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeIngredientRole;

import java.util.List;
import java.util.Optional;

public record TestRecipeSlotsView(List<IRecipeSlotView> slots) implements IRecipeSlotsView {
	public TestRecipeSlotsView {
		slots = List.copyOf(slots);
	}

	@Override
	public List<IRecipeSlotView> getSlotViews() {
		return slots;
	}

	@Override
	public List<IRecipeSlotView> getSlotViews(RecipeIngredientRole role) {
		return slots.stream()
			.filter(slot -> slot.getRole() == role)
			.toList();
	}

	@Override
	public Optional<IRecipeSlotView> findSlotByName(String slotName) {
		return slots.stream()
			.filter(slot -> slot.getSlotName().filter(slotName::equals).isPresent())
			.findFirst();
	}
}
