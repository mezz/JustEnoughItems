package mezz.jei.gui.overlay.history;

import mezz.jei.common.config.IIngredientGridConfig;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.gui.ingredients.GuiIngredientProperties;
import mezz.jei.gui.overlay.ingredients.IngredientGridLayout;
import mezz.jei.gui.overlay.ingredients.IngredientGridWithNavigationLayout;

public record LookupHistoryOverlayLayout(
	ImmutableRect2i availableGridArea,
	ImmutableRect2i ingredientGridArea,
	ImmutableRect2i slotBackgroundArea,
	ImmutableRect2i backgroundArea
) {
	private static final int INGREDIENT_PADDING = 1;
	public static final int SLOT_HEIGHT = GuiIngredientProperties.getHeight(INGREDIENT_PADDING);
	private static final int BACKGROUND_PADDING = IngredientGridWithNavigationLayout.BORDER_PADDING +
		IngredientGridWithNavigationLayout.INNER_PADDING;

	public static int getDisplayHeight(int maxRows, boolean drawBackground) {
		int height = Math.max(0, maxRows) * SLOT_HEIGHT;
		if (drawBackground) {
			height += 2 * BACKGROUND_PADDING;
		}
		return height;
	}

	public static LookupHistoryOverlayLayout calculate(IIngredientGridConfig historyListConfig, ImmutableRect2i availableArea) {
		ImmutableRect2i availableGridArea = getAvailableGridArea(historyListConfig, availableArea);
		ImmutableRect2i ingredientGridArea = IngredientGridLayout.calculateBounds(historyListConfig, availableGridArea);
		ImmutableRect2i slotBackgroundArea = IngredientGridWithNavigationLayout.calculateSlotBackgroundArea(
			ingredientGridArea,
			historyListConfig
		);
		ImmutableRect2i backgroundArea;
		if (historyListConfig.drawBackground() && !slotBackgroundArea.isEmpty()) {
			backgroundArea = slotBackgroundArea.expandBy(IngredientGridWithNavigationLayout.BORDER_PADDING);
		} else {
			backgroundArea = slotBackgroundArea;
		}

		return new LookupHistoryOverlayLayout(
			availableGridArea,
			ingredientGridArea,
			slotBackgroundArea,
			backgroundArea
		);
	}

	private static ImmutableRect2i getAvailableGridArea(IIngredientGridConfig historyListConfig, ImmutableRect2i availableArea) {
		if (historyListConfig.drawBackground()) {
			return availableArea.insetBy(BACKGROUND_PADDING);
		}
		return availableArea;
	}
}
