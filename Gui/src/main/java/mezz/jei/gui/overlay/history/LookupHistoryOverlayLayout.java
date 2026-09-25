package mezz.jei.gui.overlay.history;

import mezz.jei.gui.overlay.ingredients.IngredientGridLayout;
import mezz.jei.gui.overlay.ingredients.IngredientGridWithNavigationLayout;

public final class LookupHistoryOverlayLayout {
	private LookupHistoryOverlayLayout() {
	}

	public static int getDisplayHeight(int maxRows, boolean drawBackground, boolean usesScrollbar) {
		int height = Math.max(0, maxRows) * IngredientGridLayout.INGREDIENT_HEIGHT;
		height += 2 * IngredientGridWithNavigationLayout.BORDER_MARGIN;
		if (drawBackground) {
			height += 2 * (IngredientGridWithNavigationLayout.BORDER_PADDING +
				IngredientGridWithNavigationLayout.INNER_PADDING);
		}
		if (!usesScrollbar) {
			height += IngredientGridWithNavigationLayout.NAVIGATION_HEIGHT +
				IngredientGridWithNavigationLayout.INNER_PADDING;
		}
		return height;
	}
}
