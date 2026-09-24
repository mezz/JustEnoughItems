package mezz.jei.gui.overlay.history;

import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.gui.overlay.ingredients.IngredientGridLayout;
import mezz.jei.gui.overlay.ingredients.IngredientGridWithNavigationLayout;

public final class LookupHistoryOverlayLayout {
	private LookupHistoryOverlayLayout() {
	}

	public static ImmutableRect2i alignToOwnerBackground(
		ImmutableRect2i lookupHistoryArea,
		ImmutableRect2i ownerBackgroundArea
	) {
		if (ownerBackgroundArea.isEmpty()) {
			return lookupHistoryArea;
		}
		ImmutableRect2i ownerAvailableArea = ownerBackgroundArea.expandBy(
			IngredientGridWithNavigationLayout.BORDER_MARGIN
		);
		int left = Math.max(lookupHistoryArea.x(), ownerAvailableArea.x());
		int right = Math.min(
			lookupHistoryArea.x() + lookupHistoryArea.width(),
			ownerAvailableArea.x() + ownerAvailableArea.width()
		);
		if (left >= right) {
			return lookupHistoryArea;
		}
		return new ImmutableRect2i(left, lookupHistoryArea.y(), right - left, lookupHistoryArea.height());
	}

	public static ImmutableRect2i moveNextToOwner(
		ImmutableRect2i lookupHistoryArea,
		ImmutableRect2i lookupHistoryBackgroundArea,
		ImmutableRect2i ownerBackgroundArea
	) {
		if (lookupHistoryBackgroundArea.isEmpty() || ownerBackgroundArea.isEmpty()) {
			return lookupHistoryArea;
		}
		int ownerBottom = ownerBackgroundArea.y() + ownerBackgroundArea.height();
		int backgroundOverlap = (2 * IngredientGridWithNavigationLayout.BORDER_PADDING) -
			IngredientGridWithNavigationLayout.INNER_PADDING;
		int targetY = ownerBottom - backgroundOverlap;
		int gap = lookupHistoryBackgroundArea.y() - targetY;
		if (gap <= 0) {
			return lookupHistoryArea;
		}
		return lookupHistoryArea.moveUp(gap);
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
