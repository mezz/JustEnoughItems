package mezz.jei.transfer;

import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.RecipeIngredientRole;

import java.util.List;

public class RecipeSlotsView implements IRecipeSlotsView {
	private final List<IRecipeSlotView> slotViews;

	public RecipeSlotsView(List<IRecipeSlotView> slotViews) {
		this.slotViews = slotViews;
	}

	@Override
	public List<IRecipeSlotView> getSlotViews() {
		return this.slotViews;
	}

	@Override
	public List<IRecipeSlotView> getSlotViews(RecipeIngredientRole role, IIngredientType<?> ingredientType) {
		return this.slotViews.stream()
			.filter(slotView -> slotView.getRole() == role)
			.filter(slotView -> slotView.getAllIngredients(ingredientType).findAny().isPresent())
			.toList();
	}
}
