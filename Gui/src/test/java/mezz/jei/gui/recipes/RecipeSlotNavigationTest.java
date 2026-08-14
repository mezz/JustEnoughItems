package mezz.jei.gui.recipes;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RecipeSlotNavigationTest {
	@Test
	void wholeSlotTagUsesTagRecipeNavigation() {
		RecipeSlotNavigation.Action action = RecipeSlotNavigation.getAction(true, true, false);

		assertEquals(RecipeSlotNavigation.Action.TAG_RECIPE, action);
	}

	@Test
	void singletonTagUsesDisplayedIngredientNavigation() {
		RecipeSlotNavigation.Action action = RecipeSlotNavigation.getAction(true, false, false);

		assertEquals(RecipeSlotNavigation.Action.DISPLAYED_INGREDIENT, action);
	}

	@Test
	void multiIngredientTagUsesTagRecipeNavigation() {
		RecipeSlotNavigation.Action action = RecipeSlotNavigation.getAction(true, true, false);

		assertEquals(RecipeSlotNavigation.Action.TAG_RECIPE, action);
	}

	@Test
	void nonTagGroupUsesDisplayedIngredientNavigation() {
		RecipeSlotNavigation.Action action = RecipeSlotNavigation.getAction(false, false, false);

		assertEquals(RecipeSlotNavigation.Action.DISPLAYED_INGREDIENT, action);
	}

	@Test
	void pausingUsesTheDisplayedIngredientForTags() {
		assertEquals(
			RecipeSlotNavigation.Action.DISPLAYED_INGREDIENT,
			RecipeSlotNavigation.getAction(true, true, true)
		);
	}
}
