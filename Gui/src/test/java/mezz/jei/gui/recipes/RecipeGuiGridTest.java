package mezz.jei.gui.recipes;

import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.common.util.ImmutableSize2i;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RecipeGuiGridTest {
	private static final ImmutableSize2i RECIPE_SIZE = new ImmutableSize2i(100, 50);
	private static final ImmutableSize2i RECIPE_SIZE_WITH_BUTTONS = new ImmutableSize2i(120, 50);

	@ParameterizedTest
	@CsvSource({"243, 1, 3", "244, 2, 6", "367, 2, 6", "368, 3, 9"})
	public void addsColumnsOnlyWhenRecipesAndButtonsFit(int width, int columns, int recipesPerPage) {
		RecipeGuiGrid grid = RecipeGuiGrid.calculate(new ImmutableSize2i(width, 158), RECIPE_SIZE_WITH_BUTTONS, 10);

		assertEquals(columns, grid.columns());
		assertEquals(recipesPerPage, grid.recipesPerPage());
	}

	@ParameterizedTest
	@ValueSource(ints = {1, 2, 10})
	public void respectsConfiguredColumnLimit(int maxColumns) {
		RecipeGuiGrid grid = RecipeGuiGrid.calculate(new ImmutableSize2i(2000, 158), RECIPE_SIZE_WITH_BUTTONS, maxColumns);

		assertEquals(maxColumns, grid.columns());
		assertEquals(3 * maxColumns, grid.recipesPerPage());
	}

	@Test
	public void oversizedRecipeStillHasOneCell() {
		RecipeGuiGrid grid = RecipeGuiGrid.calculate(new ImmutableSize2i(80, 30), RECIPE_SIZE_WITH_BUTTONS, 2);

		assertEquals(1, grid.recipesPerPage());
		ImmutableRect2i expandedArea = new ImmutableRect2i(20, 30, 120, 30);
		assertEquals(new ImmutableRect2i(20, 30, 120, 50), grid.getRecipeArea(0, expandedArea, RECIPE_SIZE, 120));
	}

	@Test
	public void placesRecipesInReadingOrderAndKeepsSpaceForPartialPages() {
		ImmutableRect2i area = new ImmutableRect2i(20, 30, 244, 158);
		RecipeGuiGrid grid = RecipeGuiGrid.calculate(area.getSize(), RECIPE_SIZE_WITH_BUTTONS, 2);

		assertEquals(new ImmutableRect2i(20, 32, 120, 50), grid.getRecipeArea(0, area, RECIPE_SIZE, 120));
		assertEquals(new ImmutableRect2i(144, 32, 120, 50), grid.getRecipeArea(1, area, RECIPE_SIZE, 120));
		assertEquals(new ImmutableRect2i(20, 84, 120, 50), grid.getRecipeArea(2, area, RECIPE_SIZE, 120));
		assertEquals(new ImmutableRect2i(144, 136, 120, 50), grid.getRecipeArea(5, area, RECIPE_SIZE, 120));
	}

	@ParameterizedTest
	@CsvSource({"120, 20", "140, 30", "200, 70"})
	public void preservesSingleColumnCentering(int width, int expectedX) {
		ImmutableRect2i area = new ImmutableRect2i(20, 30, width, 158);
		RecipeGuiGrid grid = RecipeGuiGrid.calculate(area.getSize(), RECIPE_SIZE_WITH_BUTTONS, 1);

		assertEquals(expectedX, grid.getRecipeArea(0, area, RECIPE_SIZE, 120).x());
	}

	@Test
	public void recipesAndButtonsStayInsideTheAreaWithoutOverlappingDuringResize() {
		for (int width = 120; width <= 1500; width++) {
			ImmutableRect2i area = new ImmutableRect2i(20, 30, width, 158);
			RecipeGuiGrid grid = RecipeGuiGrid.calculate(area.getSize(), RECIPE_SIZE_WITH_BUTTONS, 10);
			for (int i = 0; i < grid.recipesPerPage(); i++) {
				ImmutableRect2i recipeArea = grid.getRecipeArea(i, area, RECIPE_SIZE, 120);
				assertTrue(area.contains(recipeArea.x(), recipeArea.y()));
				assertTrue(area.contains(recipeArea.getX() + recipeArea.width() - 1, recipeArea.getY() + recipeArea.height() - 1));
				if (i > 0) {
					assertFalse(recipeArea.intersects(grid.getRecipeArea(i - 1, area, RECIPE_SIZE, 120)));
				}
				if (i >= grid.columns()) {
					assertFalse(recipeArea.intersects(grid.getRecipeArea(i - grid.columns(), area, RECIPE_SIZE, 120)));
				}
			}
		}
	}
}
