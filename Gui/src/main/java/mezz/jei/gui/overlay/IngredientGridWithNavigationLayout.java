package mezz.jei.gui.overlay;

import mezz.jei.common.config.IIngredientGridConfig;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.common.util.MathUtil;

import java.util.Set;

public record IngredientGridWithNavigationLayout(
	ImmutableRect2i ingredientGridArea,
	int availableSlotCount,
	ImmutableRect2i slotBackgroundArea,
	ImmutableRect2i navigationArea,
	ImmutableRect2i backgroundArea,
	boolean navigationEnabled
) {
	public static final int NAVIGATION_HEIGHT = 20;
	public static final int BORDER_MARGIN = 6;
	public static final int BORDER_PADDING = 5;
	public static final int INNER_PADDING = 2;

	public static ImmutableRect2i getAvailableGridArea(
		IIngredientGridConfig gridConfig,
		ImmutableRect2i availableArea
	) {
		ImmutableRect2i availableGridArea = availableArea
			.insetBy(BORDER_MARGIN)
			.cropTop(NAVIGATION_HEIGHT + INNER_PADDING);

		if (gridConfig.drawBackground()) {
			availableGridArea = availableGridArea.insetBy(BORDER_PADDING + INNER_PADDING);
		}

		ImmutableRect2i estimatedGridArea = IngredientGrid.calculateBounds(gridConfig, availableGridArea);
		if (estimatedGridArea.isEmpty()) {
			return ImmutableRect2i.EMPTY;
		}

		return availableGridArea;
	}

	public static IngredientGridWithNavigationLayout fromGridArea(
		IIngredientGridConfig gridConfig,
		ImmutableRect2i ingredientGridArea,
		boolean navigationEnabled
	) {
		ImmutableRect2i slotBackgroundArea = calculateSlotBackgroundArea(ingredientGridArea, gridConfig);
		ImmutableRect2i navigationArea = calculateNavigationArea(slotBackgroundArea, navigationEnabled);
		return fromGridArea(
			gridConfig,
			ingredientGridArea,
			IngredientGrid.calculateAvailableSlotCount(ingredientGridArea, Set.of(), null),
			navigationArea,
			navigationEnabled
		);
	}

	public static IngredientGridWithNavigationLayout fromGridArea(
		IIngredientGridConfig gridConfig,
		ImmutableRect2i ingredientGridArea,
		int availableSlotCount,
		ImmutableRect2i navigationArea,
		boolean navigationEnabled
	) {
		ImmutableRect2i slotBackgroundArea = calculateSlotBackgroundArea(ingredientGridArea, gridConfig);
		ImmutableRect2i backgroundArea = MathUtil.union(slotBackgroundArea, navigationArea);
		if (gridConfig.drawBackground() && !backgroundArea.isEmpty()) {
			backgroundArea = backgroundArea.expandBy(BORDER_PADDING);
		}
		return new IngredientGridWithNavigationLayout(
			ingredientGridArea,
			availableSlotCount,
			slotBackgroundArea,
			navigationArea,
			backgroundArea,
			navigationEnabled
		);
	}

	public static ImmutableRect2i calculateSlotBackgroundArea(ImmutableRect2i ingredientGridArea, IIngredientGridConfig gridConfig) {
		if (ingredientGridArea.isEmpty()) {
			return ImmutableRect2i.EMPTY;
		}
		if (gridConfig.drawBackground()) {
			return ingredientGridArea.expandBy(INNER_PADDING);
		} else {
			return ingredientGridArea;
		}
	}

	public static ImmutableRect2i calculateNavigationArea(ImmutableRect2i slotBackgroundArea, boolean navigationEnabled) {
		if (!navigationEnabled) {
			return ImmutableRect2i.EMPTY;
		}

		return slotBackgroundArea
			.keepTop(NAVIGATION_HEIGHT)
			.moveUp(NAVIGATION_HEIGHT + INNER_PADDING);
	}

	public boolean hasRoom() {
		return !ingredientGridArea.isEmpty() &&
			availableSlotCount > 0 &&
			(!navigationEnabled || !navigationArea.isEmpty());
	}
}
