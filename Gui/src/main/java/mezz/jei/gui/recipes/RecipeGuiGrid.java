package mezz.jei.gui.recipes;

import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.common.util.ImmutableSize2i;

public record RecipeGuiGrid(int rows, int columns) {
	private static final int MIN_RECIPE_PADDING = 4;

	public static RecipeGuiGrid calculate(ImmutableSize2i availableSize, ImmutableSize2i recipeSizeWithButtons, int maxColumns) {
		int rows = Math.max(1, (availableSize.height() + MIN_RECIPE_PADDING) / (recipeSizeWithButtons.height() + MIN_RECIPE_PADDING));
		int columns = Math.clamp((availableSize.width() + MIN_RECIPE_PADDING) / (recipeSizeWithButtons.width() + MIN_RECIPE_PADDING), 1, maxColumns);
		return new RecipeGuiGrid(rows, columns);
	}

	public int recipesPerPage() {
		return rows * columns;
	}

	public ImmutableRect2i getRecipeArea(int index, ImmutableRect2i availableArea, ImmutableSize2i recipeSize, int recipeWidthWithButtons) {
		int columnWidth = (availableArea.width() - (columns - 1) * MIN_RECIPE_PADDING) / columns;
		int column = index % columns;
		int row = index / columns;
		int columnX = availableArea.x() + column * (columnWidth + MIN_RECIPE_PADDING);
		int buttonSpace = recipeWidthWithButtons - recipeSize.width();
		int recipeX;
		if (columnWidth > recipeSize.width() + 2 * buttonSpace) {
			// Center the recipe itself when its buttons fit off to the side.
			recipeX = columnX + (columnWidth - recipeSize.width()) / 2;
		} else {
			recipeX = columnX + (columnWidth - recipeWidthWithButtons) / 2;
		}

		int availableHeight = Math.max(availableArea.height(), recipeSize.height());
		int recipeSpacing = (availableHeight - rows * recipeSize.height()) / (rows + 1);
		int recipeY = availableArea.y() + recipeSpacing + row * (recipeSize.height() + recipeSpacing);
		return new ImmutableRect2i(recipeX, recipeY, recipeWidthWithButtons, recipeSize.height());
	}
}
