package mezz.jei.gui.recipes;

import mezz.jei.common.util.MathUtil;

/** Keeps a recipe anchor across resizes and selects only rows intersecting the viewport. */
public final class RecipeGuiScrollState {
	private static final int PADDING = 4;
	private int columns = 1;
	private int rowHeight = 1;
	private int recipeCount;
	private int visibleHeight;
	private int maxScroll;
	private double scrollY;

	public void update(int columns, int recipeHeight, int recipeCount, int visibleHeight) {
		double anchor = scrollY / rowHeight * this.columns;
		this.columns = columns;
		this.rowHeight = recipeHeight + PADDING;
		this.recipeCount = recipeCount;
		this.visibleHeight = Math.max(0, visibleHeight);
		int contentHeight = MathUtil.divideCeil(recipeCount, columns) * rowHeight + PADDING;
		this.maxScroll = Math.max(0, contentHeight - this.visibleHeight);
		setScrollY(anchor / columns * rowHeight);
	}

	public void scrollToRecipe(int recipeIndex) {
		setScrollY((double) (recipeIndex / columns) * rowHeight);
	}

	public boolean scroll(double pixels) {
		return setScrollY(scrollY + pixels);
	}

	public boolean setScrollOffset(float offset) {
		return setScrollY(offset * maxScroll);
	}

	private boolean setScrollY(double scrollY) {
		double previous = this.scrollY;
		this.scrollY = Math.clamp(scrollY, 0, maxScroll);
		return previous != this.scrollY;
	}

	public float getScrollOffset() {
		if (maxScroll == 0) {
			return 0;
		}
		return (float) (scrollY / maxScroll);
	}

	public int getVisibleHeight() {
		return visibleHeight;
	}

	public int getMaxScroll() {
		return maxScroll;
	}

	public int getRowHeight() {
		return rowHeight;
	}

	public int getFirstRecipeIndex() {
		return Math.min(recipeCount, (int) scrollY / rowHeight * columns);
	}

	public int getEndRecipeIndex() {
		int rows = MathUtil.divideCeil(Math.max(0, (int) scrollY + visibleHeight - PADDING), rowHeight);
		return Math.min(recipeCount, rows * columns);
	}

	public int getRecipeY(int visibleIndex) {
		int row = getFirstRecipeIndex() / columns + visibleIndex / columns;
		return PADDING + row * rowHeight - (int) scrollY;
	}
}
