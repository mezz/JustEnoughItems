package mezz.jei.library.gui.ingredients;

import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeIngredientRole;

import java.util.ArrayList;
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
	public List<IRecipeSlotView> getSlotViews(RecipeIngredientRole role) {
		List<IRecipeSlotView> list = new ArrayList<>();
		for (IRecipeSlotView slotView : this.slots) {
			if (slotView.getRole() == role) {
				list.add(slotView);
			}
		}
		return list;
	}

	@Override
	public Optional<IRecipeSlotView> findSlotByName(String slotName) {
		return this.slots.stream()
			.filter(slot ->
				slot.getSlotName()
					.map(slotName::equals)
					.orElse(false)
			)
			.findFirst();
	}
}
