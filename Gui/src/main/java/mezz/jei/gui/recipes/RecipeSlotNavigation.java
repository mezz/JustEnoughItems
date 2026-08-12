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
		if (slot.getTagKey().isPresent()) {
			return Action.TAG_RECIPE;
		}
		boolean hasCandidates = slot.getDisplayedIngredients()
			.limit(2)
			.count() > 1;
		return getAction(false, hasCandidates, false);
	}

	static Action getAction(boolean tag, boolean hasCandidates, boolean recipeCyclingPaused) {
		if (recipeCyclingPaused) {
			return Action.DISPLAYED_INGREDIENT;
		}
		if (tag) {
			return Action.TAG_RECIPE;
		}
		if (hasCandidates) {
			return Action.CANDIDATE_GROUP;
		}
		return Action.DISPLAYED_INGREDIENT;
	}
}
