package mezz.jei.gui.recipes;

import mezz.jei.api.gui.ingredient.IRecipeSlotView;

final class RecipeSlotNavigation {
	enum Action {
		DISPLAYED_INGREDIENT,
		TAG_RECIPE
	}

	private RecipeSlotNavigation() {

	}

	static Action getAction(IRecipeSlotView slot, boolean recipeCyclingPaused) {
		if (recipeCyclingPaused) {
			return Action.DISPLAYED_INGREDIENT;
		}
		boolean wholeSlotTag = slot.getTagKey().isPresent();
		boolean tagHasMultipleIngredients = wholeSlotTag && slot.getAllIngredients()
			.limit(2)
			.count() > 1;
		return getAction(wholeSlotTag, tagHasMultipleIngredients, false);
	}

	static Action getAction(
		boolean wholeSlotTag,
		boolean tagHasMultipleIngredients,
		boolean recipeCyclingPaused
	) {
		if (recipeCyclingPaused) {
			return Action.DISPLAYED_INGREDIENT;
		}
		if (wholeSlotTag && tagHasMultipleIngredients) {
			return Action.TAG_RECIPE;
		}
		return Action.DISPLAYED_INGREDIENT;
	}
}
