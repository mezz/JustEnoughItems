package mezz.jei.gui.overlay.ingredients;

import mezz.jei.common.config.IIngredientGridConfig;
import mezz.jei.common.gui.elements.Scrollbar;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.common.util.ImmutableSize2i;
import mezz.jei.gui.util.AlignmentUtil;

import java.util.Set;

public final class IngredientGridScrollbarLayout {
	private IngredientGridScrollbarLayout() {
	}

	public static IngredientGridWithNavigationLayout calculate(
		IIngredientGridConfig gridConfig,
		ImmutableRect2i availableArea,
		Set<ImmutableRect2i> guiExclusionAreas,
		int ingredientCount,
		boolean smoothScrolling
	) {
		return switch (gridConfig.navigationVisibility().get()) {
			case ENABLED -> calculateForScrollbar(gridConfig, availableArea, guiExclusionAreas, true, smoothScrolling);
			case DISABLED -> calculateForScrollbar(gridConfig, availableArea, guiExclusionAreas, false, smoothScrolling);
			case AUTO_HIDE -> calculateAutoHideScrollbar(
				gridConfig,
				availableArea,
				guiExclusionAreas,
				ingredientCount,
				smoothScrolling
			);
		};
	}

	private static IngredientGridWithNavigationLayout calculateAutoHideScrollbar(
		IIngredientGridConfig gridConfig,
		ImmutableRect2i availableArea,
		Set<ImmutableRect2i> guiExclusionAreas,
		int ingredientCount,
		boolean smoothScrolling
	) {
		IngredientGridWithNavigationLayout layoutWithoutScrollbar = calculateForScrollbar(
			gridConfig,
			availableArea,
			guiExclusionAreas,
			false,
			smoothScrolling
		);
		int fullyVisibleSlotCount = getFullyVisibleSlotCount(
			layoutWithoutScrollbar,
			guiExclusionAreas,
			smoothScrolling
		);
		int pageCountWithoutScrollbar = IngredientGridPageState.getPageCount(
			ingredientCount,
			fullyVisibleSlotCount
		);
		boolean scrollbarEnabled = layoutWithoutScrollbar.hasRoom() && pageCountWithoutScrollbar > 1;
		if (scrollbarEnabled) {
			return calculateForScrollbar(gridConfig, availableArea, guiExclusionAreas, true, smoothScrolling);
		}
		return layoutWithoutScrollbar;
	}

	private static int getFullyVisibleSlotCount(
		IngredientGridWithNavigationLayout layout,
		Set<ImmutableRect2i> guiExclusionAreas,
		boolean smoothScrolling
	) {
		ImmutableRect2i ingredientGridArea = layout.ingredientGridArea();
		int partialRowHeight = ingredientGridArea.height() % IngredientGridLayout.INGREDIENT_HEIGHT;
		if (!smoothScrolling || partialRowHeight == 0) {
			return layout.availableSlotCount();
		}
		ImmutableRect2i fullyVisibleGridArea = ingredientGridArea.cropBottom(partialRowHeight);
		return IngredientGridLayout.calculateAvailableSlotCount(fullyVisibleGridArea, guiExclusionAreas);
	}

	private static IngredientGridWithNavigationLayout calculateForScrollbar(
		IIngredientGridConfig gridConfig,
		ImmutableRect2i availableArea,
		Set<ImmutableRect2i> guiExclusionAreas,
		boolean scrollbarEnabled,
		boolean smoothScrolling
	) {
		ImmutableRect2i availableGridArea = IngredientGridWithNavigationLayout.getAvailableGridArea(
			gridConfig,
			availableArea,
			false
		);
		final ImmutableRect2i ingredientGridArea;
		if (scrollbarEnabled) {
			ingredientGridArea = calculateScrollbarGridArea(gridConfig, availableGridArea, smoothScrolling);
		} else {
			ingredientGridArea = IngredientGridLayout.calculateBounds(gridConfig, availableGridArea, smoothScrolling);
		}
		int availableSlotCount = IngredientGridLayout.calculateAvailableSlotCount(
			ingredientGridArea,
			guiExclusionAreas
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
		ImmutableRect2i availableGridArea,
		boolean smoothScrolling
	) {
		if (availableGridArea.isEmpty()) {
			return ImmutableRect2i.EMPTY;
		}

		ImmutableRect2i availableAreaWithoutScrollbar = availableGridArea.cropRight(calculateScrollbarReservedGridWidth(gridConfig));
		ImmutableSize2i ingredientGridSize = IngredientGridLayout.calculateSize(
			gridConfig,
			availableAreaWithoutScrollbar,
			smoothScrolling
		);
		if (ingredientGridSize.equals(ImmutableSize2i.EMPTY)) {
			return ImmutableRect2i.EMPTY;
		}

		return AlignmentUtil.align(
			ingredientGridSize,
			availableAreaWithoutScrollbar,
			gridConfig.horizontalAlignment().get(),
			gridConfig.verticalAlignment().get()
		);
	}

	private static int calculateScrollbarExtraWidth(IIngredientGridConfig gridConfig) {
		return calculateScrollbarOffsetFromGrid(gridConfig) + Scrollbar.WIDTH;
	}

	private static int calculateScrollbarReservedGridWidth(IIngredientGridConfig gridConfig) {
		int reservedGridWidth = calculateScrollbarExtraWidth(gridConfig);
		if (gridConfig.backgroundStyle().get().isEnabled()) {
			return reservedGridWidth - IngredientGridWithNavigationLayout.INNER_PADDING;
		}
		return reservedGridWidth;
	}

	private static int calculateScrollbarOffsetFromGrid(IIngredientGridConfig gridConfig) {
		if (gridConfig.backgroundStyle().get().isEnabled()) {
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
			Scrollbar.WIDTH,
			slotBackgroundArea.height()
		);
	}
}
