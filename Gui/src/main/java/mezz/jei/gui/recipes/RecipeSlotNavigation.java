package mezz.jei.gui.recipes;

import mezz.jei.api.gui.ingredient.IRecipeSlotView;

final class RecipeSlotNavigation {
	enum Action {
		DISPLAYED_INGREDIENT,
		TAG_RECIPE,
		CANDIDATE_GROUP
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
		boolean hasCandidates = slot.getDisplayedIngredients()
			.limit(2)
			.count() > 1;
		return getAction(wholeSlotTag, tagHasMultipleIngredients, hasCandidates, false);
	}

	static Action getAction(
		boolean wholeSlotTag,
		boolean tagHasMultipleIngredients,
		boolean hasCandidates,
		boolean recipeCyclingPaused
	) {
		if (recipeCyclingPaused) {
			return Action.DISPLAYED_INGREDIENT;
		}
		if (wholeSlotTag && tagHasMultipleIngredients) {
			return Action.TAG_RECIPE;
		}
		if (hasCandidates) {
			return Action.CANDIDATE_GROUP;
		}
		return Action.DISPLAYED_INGREDIENT;
	}
}
