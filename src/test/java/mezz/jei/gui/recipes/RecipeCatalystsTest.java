package mezz.jei.gui.recipes;

import net.minecraft.client.renderer.Rectangle2d;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RecipeCatalystsTest {
	@Test
	public void zeroCatalystsReturnsEmptyLayout() {
		// Setup: the selected recipe category has no catalysts.
		Rectangle2d recipeArea = recipeArea(100);
		Rectangle2d optionButtonsArea = optionButtonsArea(0);

		// Operation: calculate the catalyst tab layout.
		RecipeCatalysts.Layout layout = RecipeCatalysts.calculateLayout(0, recipeArea, optionButtonsArea);

		// Assertions: no area is reserved for an empty catalyst list.
		assertEquals(0, layout.width());
		assertEquals(0, layout.height());
		assertEquals(0, layout.maxIngredientsPerColumn());
	}

	@Test
	public void singleCatalystUsesOneSlotColumn() {
		// Setup: a normal recipe area has enough room for one catalyst.
		Rectangle2d recipeArea = recipeArea(100);
		Rectangle2d optionButtonsArea = optionButtonsArea(0);

		// Operation: calculate the catalyst tab layout.
		RecipeCatalysts.Layout layout = RecipeCatalysts.calculateLayout(1, recipeArea, optionButtonsArea);

		// Assertions: the tab has one visible slot and overlaps the left side of the recipe area.
		assertEquals(1, layout.maxIngredientsPerColumn());
		assertEquals(recipeArea.getY(), layout.top());
		assertTrue(layout.left() < recipeArea.getX());
		assertPositiveLayout(layout);
	}

	@Test
	public void catalystsFitInOneColumnWhenHeightAllows() {
		// Setup: the recipe area has enough vertical room for every catalyst.
		Rectangle2d recipeArea = recipeArea(220);
		Rectangle2d optionButtonsArea = optionButtonsArea(0);
		RecipeCatalysts.Layout singleColumnLayout = RecipeCatalysts.calculateLayout(1, recipeArea, optionButtonsArea);

		// Operation: calculate a layout for several catalysts.
		RecipeCatalysts.Layout layout = RecipeCatalysts.calculateLayout(10, recipeArea, optionButtonsArea);

		// Assertions: all catalysts fit in one column, so width does not grow.
		assertEquals(10, layout.maxIngredientsPerColumn());
		assertEquals(singleColumnLayout.width(), layout.width());
		assertTrue(layout.height() > singleColumnLayout.height());
	}

	@Test
	public void manyCatalystsUseMultipleColumnsWhenHeightIsLimited() {
		// Setup: the recipe area can only fit five catalysts in the first layout pass.
		Rectangle2d recipeArea = recipeArea(100);
		Rectangle2d optionButtonsArea = optionButtonsArea(0);
		RecipeCatalysts.Layout singleColumnLayout = RecipeCatalysts.calculateLayout(1, recipeArea, optionButtonsArea);

		// Operation: calculate a layout for enough catalysts to require multiple columns.
		RecipeCatalysts.Layout layout = RecipeCatalysts.calculateLayout(11, recipeArea, optionButtonsArea);

		// Assertions: catalysts are spread across columns instead of overflowing one column.
		assertEquals(4, layout.maxIngredientsPerColumn());
		assertTrue(layout.width() > singleColumnLayout.width());
		assertPositiveLayout(layout);
	}

	@Test
	public void shortRecipeAreaUsesAtLeastOneCatalystPerColumn() {
		// Setup: the recipe area is too short to fit the catalyst tab's normal border and one slot.
		Rectangle2d recipeArea = recipeArea(10);
		Rectangle2d optionButtonsArea = optionButtonsArea(0);

		// Operation: calculate a layout for multiple catalysts.
		RecipeCatalysts.Layout layout = RecipeCatalysts.calculateLayout(3, recipeArea, optionButtonsArea);

		// Assertions: layout still reserves one catalyst per column instead of dividing by zero.
		assertEquals(1, layout.maxIngredientsPerColumn());
		assertPositiveLayout(layout);
	}

	@Test
	public void optionButtonsCanUseAllAvailableHeight() {
		// Setup: option buttons leave no remaining vertical room for catalyst slots.
		Rectangle2d recipeArea = recipeArea(80);
		Rectangle2d optionButtonsArea = optionButtonsArea(100);

		// Operation: calculate a layout for multiple catalysts.
		RecipeCatalysts.Layout layout = RecipeCatalysts.calculateLayout(3, recipeArea, optionButtonsArea);

		// Assertions: negative remaining height is handled the same as a very short recipe area.
		assertEquals(1, layout.maxIngredientsPerColumn());
		assertPositiveLayout(layout);
	}

	@Test
	public void largeCatalystCountKeepsPositiveLayout() {
		// Setup: a very large catalyst list is shown in a very short recipe area.
		Rectangle2d recipeArea = recipeArea(10);
		Rectangle2d optionButtonsArea = optionButtonsArea(0);

		// Operation: calculate the catalyst tab layout.
		RecipeCatalysts.Layout layout = RecipeCatalysts.calculateLayout(1_000, recipeArea, optionButtonsArea);

		// Assertions: the layout remains valid and uses one catalyst per column.
		assertEquals(1, layout.maxIngredientsPerColumn());
		assertPositiveLayout(layout);
	}

	private static Rectangle2d recipeArea(int height) {
		return new Rectangle2d(100, 20, 80, height);
	}

	private static Rectangle2d optionButtonsArea(int height) {
		return new Rectangle2d(0, 0, 20, height);
	}

	private static void assertPositiveLayout(RecipeCatalysts.Layout layout) {
		assertTrue(layout.width() > 0, () -> layout + " should have positive width");
		assertTrue(layout.height() > 0, () -> layout + " should have positive height");
	}
}
