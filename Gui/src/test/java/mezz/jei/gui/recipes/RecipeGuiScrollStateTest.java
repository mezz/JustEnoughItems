package mezz.jei.gui.recipes;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RecipeGuiScrollStateTest {
	@Test
	public void requestsExactlyTheIntersectingRowsIncludingPartialRows() {
		RecipeGuiScrollState state = new RecipeGuiScrollState();
		state.update(2, 50, 17, 158);
		for (int offset = 0; offset <= state.getMaxScroll(); offset++) {
			if (offset > 0) {
				state.scroll(1);
			}
			for (int index = 0; index < 17; index++) {
				int y = 4 + (index / 2) * 54 - offset;
				boolean intersects = y < 158 && y + 50 > 0;
				boolean requested = index >= state.getFirstRecipeIndex() && index < state.getEndRecipeIndex();
				assertEquals(intersects, requested, "Recipe " + index + " at scroll " + offset);
				if (requested) {
					assertEquals(y, state.getRecipeY(index - state.getFirstRecipeIndex()));
				}
			}
		}
	}

	@Test
	public void jumpingToTheBottomOfALargeCategoryOnlyRequestsAViewport() {
		RecipeGuiScrollState state = new RecipeGuiScrollState();
		state.update(2, 50, 10_001, 158);
		state.setScrollOffset(1);

		assertEquals(10_001, state.getEndRecipeIndex());
		assertTrue(state.getEndRecipeIndex() - state.getFirstRecipeIndex() <= 8);
		assertFalse(state.scroll(100));
		assertEquals(1, state.getScrollOffset());
		state.setScrollOffset(-1);
		assertEquals(0, state.getFirstRecipeIndex());
		assertFalse(state.scroll(-100));
	}

	@Test
	public void resizeKeepsTheAnchorAndClampsWhenEverythingFits() {
		RecipeGuiScrollState state = new RecipeGuiScrollState();
		state.update(1, 50, 100, 158);
		state.scrollToRecipe(20);
		state.update(2, 50, 100, 158);
		assertEquals(20, state.getFirstRecipeIndex());
		state.update(1, 50, 100, 158);
		assertEquals(20, state.getFirstRecipeIndex());

		state.update(2, 50, 3, 158);
		assertEquals(0, state.getFirstRecipeIndex());
		assertEquals(3, state.getEndRecipeIndex());
		assertEquals(0, state.getMaxScroll());
	}

	@Test
	public void oversizedRecipesCanBeScrolledToTheirBottom() {
		RecipeGuiScrollState state = new RecipeGuiScrollState();
		state.update(1, 500, 1, 158);
		state.setScrollOffset(1);
		assertEquals(0, state.getFirstRecipeIndex());
		assertEquals(1, state.getEndRecipeIndex());
		assertEquals(154, state.getRecipeY(0) + 500);
	}

	@Test
	public void emptyCategoryHasNoVisibleRecipesOrScrollRange() {
		RecipeGuiScrollState state = new RecipeGuiScrollState();
		state.update(2, 50, 0, 158);
		assertEquals(0, state.getFirstRecipeIndex());
		assertEquals(0, state.getEndRecipeIndex());
		assertFalse(state.setScrollOffset(1));
	}
}
