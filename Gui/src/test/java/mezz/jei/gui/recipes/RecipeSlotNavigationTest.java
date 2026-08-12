package mezz.jei.gui.recipes;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RecipeSlotNavigationTest {
	@Test
	void tagNavigationTakesPriorityOverCandidateGroupNavigation() {
		RecipeSlotNavigation.Action action = RecipeSlotNavigation.getAction(true, true, false);

		assertEquals(RecipeSlotNavigation.Action.TAG_RECIPE, action);
	}

	@Test
	void candidateGroupOpensWhenTheSlotIsNotATag() {
		RecipeSlotNavigation.Action action = RecipeSlotNavigation.getAction(false, true, false);

		assertEquals(RecipeSlotNavigation.Action.CANDIDATE_GROUP, action);
	}

	@Test
	void pausingUsesTheDisplayedIngredientForTagsAndGroups() {
		assertEquals(
			RecipeSlotNavigation.Action.DISPLAYED_INGREDIENT,
			RecipeSlotNavigation.getAction(true, true, true)
		);
		assertEquals(
			RecipeSlotNavigation.Action.DISPLAYED_INGREDIENT,
			RecipeSlotNavigation.getAction(false, true, true)
		);
	}
}
