package mezz.jei.gui.overlay;

import mezz.jei.common.config.IIngredientGridConfig;
import mezz.jei.common.util.ImmutablePoint2i;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.common.util.MathUtil;
import mezz.jei.gui.util.MaximalRectangle;
import org.jspecify.annotations.Nullable;

import java.util.Comparator;
import java.util.Set;

final class IngredientGridWithNavigationLayout {
	private static final int NAVIGATION_HEIGHT = 20;
	private static final int BORDER_MARGIN = 6;
	private static final int BORDER_PADDING = 5;
	private static final int INNER_PADDING = 2;

	private IngredientGridWithNavigationLayout() {

	}

	static Layout calculate(
		IIngredientGridConfig gridConfig,
		ImmutableRect2i availableArea,
		Set<ImmutableRect2i> guiExclusionAreas,
		@Nullable ImmutablePoint2i mouseExclusionPoint,
		int ingredientCount
	) {
		return switch (gridConfig.getButtonNavigationVisibility()) {
			case ENABLED -> calculateForNavigation(gridConfig, availableArea, guiExclusionAreas, mouseExclusionPoint, true);
			case DISABLED -> calculateForNavigation(gridConfig, availableArea, guiExclusionAreas, mouseExclusionPoint, false);
			case AUTO_HIDE -> calculateAutoHide(
				gridConfig,
				availableArea,
				guiExclusionAreas,
				mouseExclusionPoint,
				ingredientCount
			);
		};
	}

	private static Layout calculateAutoHide(
		IIngredientGridConfig gridConfig,
		ImmutableRect2i availableArea,
		Set<ImmutableRect2i> guiExclusionAreas,
		@Nullable ImmutablePoint2i mouseExclusionPoint,
		int ingredientCount
	) {
		Layout layoutWithoutNavigation = calculateForNavigation(
			gridConfig,
			availableArea,
			guiExclusionAreas,
			mouseExclusionPoint,
			false
		);
		int pageCountWithoutNavigation = IngredientGridPageState.getPageCount(
			ingredientCount,
			layoutWithoutNavigation.availableSlotCount()
		);
		boolean navigationEnabled = layoutWithoutNavigation.hasRoom() && pageCountWithoutNavigation > 1;
		if (navigationEnabled) {
			return calculateForNavigation(gridConfig, availableArea, guiExclusionAreas, mouseExclusionPoint, true);
		}
		return layoutWithoutNavigation;
	}

	private static Layout calculateForNavigation(
		IIngredientGridConfig gridConfig,
		ImmutableRect2i availableArea,
		Set<ImmutableRect2i> guiExclusionAreas,
		@Nullable ImmutablePoint2i mouseExclusionPoint,
		boolean navigationEnabled
	) {
		ImmutableRect2i availableGridArea = getAvailableGridArea(
			gridConfig,
			availableArea,
			guiExclusionAreas,
			navigationEnabled
		);
		ImmutableRect2i ingredientGridArea = IngredientGridLayout.calculateBounds(gridConfig, availableGridArea);
		IngredientGridLayout.SlotInfo slotInfo = IngredientGridLayout.calculateSlotInfo(
			ingredientGridArea,
			guiExclusionAreas,
			mouseExclusionPoint
		);
		return calculateFromGridArea(gridConfig, availableGridArea, ingredientGridArea, navigationEnabled, slotInfo.available());
	}

	static ImmutableRect2i getAvailableGridArea(
		IIngredientGridConfig gridConfig,
		ImmutableRect2i availableArea,
		Set<ImmutableRect2i> guiExclusionAreas,
		boolean navigationEnabled
	) {
		ImmutableRect2i availableGridArea = availableArea.insetBy(BORDER_MARGIN);
		if (gridConfig.drawBackground()) {
			availableGridArea = availableGridArea
				.insetBy(BORDER_PADDING + INNER_PADDING);
		}

		ImmutableRect2i estimatedGridArea = IngredientGridLayout.calculateBounds(gridConfig, availableGridArea);

		if (!estimatedGridArea.isEmpty()) {
			ImmutableRect2i slotBackgroundArea = calculateSlotBackgroundArea(estimatedGridArea, gridConfig);
			ImmutableRect2i estimatedNavigationArea = calculateNavigationArea(slotBackgroundArea, navigationEnabled);
			if (gridConfig.drawBackground() && !estimatedNavigationArea.isEmpty()) {
				estimatedNavigationArea = estimatedNavigationArea.expandBy(BORDER_PADDING + INNER_PADDING);
			}

			availableGridArea = avoidExclusionAreas(
				availableArea,
				estimatedNavigationArea,
				guiExclusionAreas,
				gridConfig
			)
				.insetBy(BORDER_MARGIN)
				.cropTop(NAVIGATION_HEIGHT + INNER_PADDING);

			if (gridConfig.drawBackground()) {
				availableGridArea = availableGridArea.insetBy(BORDER_PADDING + INNER_PADDING);
			}
		}

		return availableGridArea;
	}

	static Layout calculateFromGridArea(
		IIngredientGridConfig gridConfig,
		ImmutableRect2i availableGridArea,
		ImmutableRect2i ingredientGridArea,
		boolean navigationEnabled
	) {
		return calculateFromGridArea(
			gridConfig,
			availableGridArea,
			ingredientGridArea,
			navigationEnabled,
			IngredientGridLayout.calculateSlotInfo(ingredientGridArea, Set.of(), null).available()
		);
	}

	private static Layout calculateFromGridArea(
		IIngredientGridConfig gridConfig,
		ImmutableRect2i availableGridArea,
		ImmutableRect2i ingredientGridArea,
		boolean navigationEnabled,
		int availableSlotCount
	) {
		ImmutableRect2i slotBackgroundArea = calculateSlotBackgroundArea(ingredientGridArea, gridConfig);
		ImmutableRect2i navigationArea = calculateNavigationArea(slotBackgroundArea, navigationEnabled);
		ImmutableRect2i backgroundArea = MathUtil.union(slotBackgroundArea, navigationArea);
		if (gridConfig.drawBackground()) {
			backgroundArea = backgroundArea.expandBy(BORDER_PADDING);
		}
		return new Layout(
			availableGridArea,
			ingredientGridArea,
			availableSlotCount,
			slotBackgroundArea,
			navigationArea,
			backgroundArea
		);
	}

	private static ImmutableRect2i avoidExclusionAreas(
		ImmutableRect2i availableArea,
		ImmutableRect2i estimatedNavigationArea,
		Set<ImmutableRect2i> guiExclusionAreas,
		IIngredientGridConfig gridConfig
	) {
		final int maxDimension = Math.max(availableArea.getWidth(), availableArea.getHeight());
		final int samplingScale = Math.max(IngredientGridLayout.INGREDIENT_HEIGHT / 2, maxDimension / 25);

		ImmutableRect2i largestSafeArea = MaximalRectangle.getLargestRectangles(
			availableArea,
			guiExclusionAreas,
			samplingScale
		)
			.max(Comparator.comparingInt((ImmutableRect2i rect) -> IngredientGridLayout.calculateSize(gridConfig, rect).getArea())
				.thenComparing(r -> r.getWidth() * r.getHeight()))
			.orElse(ImmutableRect2i.EMPTY);

		final boolean intersectsNavigationArea = guiExclusionAreas.stream()
			.anyMatch(estimatedNavigationArea::intersects);
		if (intersectsNavigationArea) {
			return largestSafeArea;
		}

		IngredientGridLayout.SlotInfo slotInfo = IngredientGridLayout.calculateSlotInfo(gridConfig, availableArea, guiExclusionAreas);
		IngredientGridLayout.SlotInfo safeSlotInfo = IngredientGridLayout.calculateSlotInfo(gridConfig, largestSafeArea, guiExclusionAreas);
		if (slotInfo.percentBlocked() > 0.25 || safeSlotInfo.total() > slotInfo.total()) {
			return largestSafeArea;
		} else {
			return availableArea;
		}
	}

	private static ImmutableRect2i calculateSlotBackgroundArea(ImmutableRect2i ingredientGridArea, IIngredientGridConfig gridConfig) {
		if (gridConfig.drawBackground()) {
			return ingredientGridArea.expandBy(INNER_PADDING);
		} else {
			return ingredientGridArea;
		}
	}

	private static ImmutableRect2i calculateNavigationArea(ImmutableRect2i slotBackgroundArea, boolean navigationEnabled) {
		if (!navigationEnabled) {
			return ImmutableRect2i.EMPTY;
		}

		return slotBackgroundArea
			.keepTop(NAVIGATION_HEIGHT)
			.moveUp(NAVIGATION_HEIGHT + INNER_PADDING);
	}

	record Layout(
		ImmutableRect2i availableGridArea,
		ImmutableRect2i ingredientGridArea,
		int availableSlotCount,
		ImmutableRect2i slotBackgroundArea,
		ImmutableRect2i navigationArea,
		ImmutableRect2i backgroundArea
	) {
		boolean hasRoom() {
			return !ingredientGridArea.isEmpty();
		}
	}
}
