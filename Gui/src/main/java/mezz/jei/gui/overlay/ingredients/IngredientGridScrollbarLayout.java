package mezz.jei.gui.overlay.ingredients;

import mezz.jei.common.config.IIngredientGridConfig;
import mezz.jei.common.util.ImmutablePoint2i;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.common.util.ImmutableSize2i;
import mezz.jei.gui.util.AlignmentUtil;
import org.jspecify.annotations.Nullable;

import java.util.Set;

public final class IngredientGridScrollbarLayout {
	private IngredientGridScrollbarLayout() {
	}

	public static IngredientGridWithNavigationLayout calculate(
		IIngredientGridConfig gridConfig,
		ImmutableRect2i availableArea,
		Set<ImmutableRect2i> guiExclusionAreas,
		@Nullable ImmutablePoint2i mouseExclusionPoint,
		int ingredientCount
	) {
		return switch (gridConfig.getNavigationVisibility()) {
			case ENABLED -> calculateForScrollbar(gridConfig, availableArea, guiExclusionAreas, mouseExclusionPoint, true);
			case DISABLED -> calculateForScrollbar(gridConfig, availableArea, guiExclusionAreas, mouseExclusionPoint, false);
			case AUTO_HIDE -> calculateAutoHideScrollbar(
				gridConfig,
				availableArea,
				guiExclusionAreas,
				mouseExclusionPoint,
				ingredientCount
			);
		};
	}

	private static IngredientGridWithNavigationLayout calculateAutoHideScrollbar(
		IIngredientGridConfig gridConfig,
		ImmutableRect2i availableArea,
		Set<ImmutableRect2i> guiExclusionAreas,
		@Nullable ImmutablePoint2i mouseExclusionPoint,
		int ingredientCount
	) {
		IngredientGridWithNavigationLayout layoutWithoutScrollbar = calculateForScrollbar(
			gridConfig,
			availableArea,
			guiExclusionAreas,
			mouseExclusionPoint,
			false
		);
		int pageCountWithoutScrollbar = IngredientGridPageState.getPageCount(
			ingredientCount,
			layoutWithoutScrollbar.availableSlotCount()
		);
		boolean scrollbarEnabled = layoutWithoutScrollbar.hasRoom() && pageCountWithoutScrollbar > 1;
		if (scrollbarEnabled) {
			return calculateForScrollbar(gridConfig, availableArea, guiExclusionAreas, mouseExclusionPoint, true);
		}
		return layoutWithoutScrollbar;
	}

	private static IngredientGridWithNavigationLayout calculateForScrollbar(
		IIngredientGridConfig gridConfig,
		ImmutableRect2i availableArea,
		Set<ImmutableRect2i> guiExclusionAreas,
		@Nullable ImmutablePoint2i mouseExclusionPoint,
		boolean scrollbarEnabled
	) {
		ImmutableRect2i availableGridArea = IngredientGridWithNavigationLayout.getAvailableGridArea(
			gridConfig,
			availableArea,
			false
		);
		final ImmutableRect2i ingredientGridArea;
		if (scrollbarEnabled) {
			ingredientGridArea = calculateScrollbarGridArea(gridConfig, availableGridArea);
		} else {
			ingredientGridArea = IngredientGridLayout.calculateBounds(gridConfig, availableGridArea);
		}
		int availableSlotCount = IngredientGridLayout.calculateAvailableSlotCount(
			ingredientGridArea,
			guiExclusionAreas,
			mouseExclusionPoint
		);

		ImmutableRect2i slotBackgroundArea = IngredientGridWithNavigationLayout.calculateSlotBackgroundArea(
			ingredientGridArea,
			gridConfig
		);
		return IngredientGridWithNavigationLayout.fromGridArea(
			gridConfig,
			ingredientGridArea,
			availableSlotCount,
			ImmutableRect2i.EMPTY,
			ImmutableRect2i.EMPTY,
			false,
			calculateScrollbarArea(gridConfig, ingredientGridArea, slotBackgroundArea, scrollbarEnabled),
			scrollbarEnabled
		);
	}

	private static ImmutableRect2i calculateScrollbarGridArea(
		IIngredientGridConfig gridConfig,
		ImmutableRect2i availableGridArea
	) {
		if (availableGridArea.isEmpty()) {
			return ImmutableRect2i.EMPTY;
		}

		ImmutableRect2i availableAreaWithoutScrollbar = availableGridArea.cropRight(calculateScrollbarReservedGridWidth(gridConfig));
		ImmutableSize2i ingredientGridSize = IngredientGridLayout.calculateSize(
			gridConfig,
			availableAreaWithoutScrollbar
		);
		if (ingredientGridSize.equals(ImmutableSize2i.EMPTY)) {
			return ImmutableRect2i.EMPTY;
		}

		return AlignmentUtil.align(
			ingredientGridSize,
			availableAreaWithoutScrollbar,
			gridConfig.getHorizontalAlignment(),
			gridConfig.getVerticalAlignment()
		);
	}

	private static int calculateScrollbarExtraWidth(IIngredientGridConfig gridConfig) {
		return calculateScrollbarOffsetFromGrid(gridConfig) + IngredientGridScrollbar.SCROLLBAR_WIDTH;
	}

	private static int calculateScrollbarReservedGridWidth(IIngredientGridConfig gridConfig) {
		int reservedGridWidth = calculateScrollbarExtraWidth(gridConfig);
		if (gridConfig.drawBackground()) {
			return reservedGridWidth - IngredientGridWithNavigationLayout.INNER_PADDING;
		}
		return reservedGridWidth;
	}

	private static int calculateScrollbarOffsetFromGrid(IIngredientGridConfig gridConfig) {
		if (gridConfig.drawBackground()) {
			return 2 * IngredientGridWithNavigationLayout.INNER_PADDING;
		}
		return 0;
	}

	private static ImmutableRect2i calculateScrollbarArea(
		IIngredientGridConfig gridConfig,
		ImmutableRect2i ingredientGridArea,
		ImmutableRect2i slotBackgroundArea,
		boolean scrollbarEnabled
	) {
		if (!scrollbarEnabled || ingredientGridArea.isEmpty()) {
			return ImmutableRect2i.EMPTY;
		}

		return new ImmutableRect2i(
			ingredientGridArea.x() + ingredientGridArea.width() + calculateScrollbarOffsetFromGrid(gridConfig),
			slotBackgroundArea.y(),
			IngredientGridScrollbar.SCROLLBAR_WIDTH,
			slotBackgroundArea.height()
		);
	}
}
