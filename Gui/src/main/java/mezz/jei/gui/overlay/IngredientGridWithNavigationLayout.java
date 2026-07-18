package mezz.jei.gui.overlay;

import mezz.jei.common.config.IIngredientGridConfig;
import mezz.jei.common.util.ImmutablePoint2i;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.common.util.MathUtil;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

final class IngredientGridWithNavigationLayout {
	private static final int NAVIGATION_HEIGHT = 20;
	private static final int BORDER_MARGIN = 6;
	private static final int BORDER_PADDING = 5;
	private static final int INNER_PADDING = 2;
	private static final int NAVIGATION_MIN_WIDTH = NAVIGATION_HEIGHT + BORDER_PADDING + INNER_PADDING;

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
		ImmutableRect2i effectiveArea = availableArea;
		ImmutableRect2i availableGridArea = getAvailableGridArea(gridConfig, effectiveArea);
		ImmutableRect2i ingredientGridArea = IngredientGrid.calculateBounds(gridConfig, availableGridArea);
		IngredientGrid.SlotInfo slotInfo = IngredientGrid.calculateSlotInfo(
			ingredientGridArea,
			guiExclusionAreas,
			mouseExclusionPoint
		);

		ImmutableRect2i slotBackgroundArea = calculateSlotBackgroundArea(ingredientGridArea, gridConfig);
		ImmutableRect2i defaultNavigationArea = calculateNavigationArea(slotBackgroundArea, navigationEnabled);
		ImmutableRect2i navigationArea = calculateNavigationAreaAvoidingExclusions(
			defaultNavigationArea, slotBackgroundArea, guiExclusionAreas, gridConfig
		);

		if (navigationEnabled && navigationArea.isEmpty() && !defaultNavigationArea.isEmpty()) {
			int shiftY = calculateNavigationShiftY(effectiveArea, slotBackgroundArea, guiExclusionAreas, gridConfig);
			if (shiftY > effectiveArea.getY()) {
				int effectiveAreaBottom = effectiveArea.getY() + effectiveArea.getHeight();
				shiftY = Math.min(shiftY, effectiveAreaBottom);
				effectiveArea = new ImmutableRect2i(
					effectiveArea.getX(), shiftY,
					effectiveArea.getWidth(), effectiveAreaBottom - shiftY
				);
				availableGridArea = getAvailableGridArea(gridConfig, effectiveArea);
				ingredientGridArea = IngredientGrid.calculateBounds(gridConfig, availableGridArea);
				slotInfo = IngredientGrid.calculateSlotInfo(
					ingredientGridArea, guiExclusionAreas, mouseExclusionPoint
				);
				slotBackgroundArea = calculateSlotBackgroundArea(ingredientGridArea, gridConfig);
				defaultNavigationArea = calculateNavigationArea(slotBackgroundArea, navigationEnabled);
				navigationArea = calculateNavigationAreaAvoidingExclusions(
					defaultNavigationArea, slotBackgroundArea, guiExclusionAreas, gridConfig
				);
			}
		}

		return calculateFromGridArea(
			gridConfig,
			ingredientGridArea,
			slotInfo.available(),
			navigationArea,
			navigationEnabled
		);
	}

	static ImmutableRect2i getAvailableGridArea(
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

	static Layout calculateFromGridArea(
		IIngredientGridConfig gridConfig,
		ImmutableRect2i ingredientGridArea,
		boolean navigationEnabled
	) {
		ImmutableRect2i slotBackgroundArea = calculateSlotBackgroundArea(ingredientGridArea, gridConfig);
		ImmutableRect2i navigationArea = calculateNavigationArea(slotBackgroundArea, navigationEnabled);
		return calculateFromGridArea(
			gridConfig,
			ingredientGridArea,
			IngredientGrid.calculateSlotInfo(ingredientGridArea, Set.of(), null).available(),
			navigationArea,
			navigationEnabled
		);
	}

	private static Layout calculateFromGridArea(
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
		return new Layout(
			ingredientGridArea,
			availableSlotCount,
			slotBackgroundArea,
			navigationArea,
			backgroundArea,
			navigationEnabled
		);
	}

	private static ImmutableRect2i calculateSlotBackgroundArea(ImmutableRect2i ingredientGridArea, IIngredientGridConfig gridConfig) {
		if (ingredientGridArea.isEmpty()) {
			return ImmutableRect2i.EMPTY;
		}
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

	private static int calculateNavigationShiftY(
		ImmutableRect2i availableArea,
		ImmutableRect2i slotBackgroundArea,
		Set<ImmutableRect2i> guiExclusionAreas,
		IIngredientGridConfig gridConfig
	) {
		int padding = gridConfig.drawBackground() ? BORDER_PADDING + INNER_PADDING : 0;
		int stripTop = availableArea.getY() + BORDER_MARGIN;
		int stripHeight = NAVIGATION_HEIGHT + INNER_PADDING + 2 * padding;
		ImmutableRect2i navigationStripArea = calculateNavigationStripArea(
			slotBackgroundArea,
			stripTop,
			stripHeight,
			gridConfig
		);

		int shiftY = availableArea.getY();
		for (ImmutableRect2i exclusion : guiExclusionAreas) {
			if (exclusion.intersects(navigationStripArea)) {
				shiftY = Math.max(shiftY, exclusion.getY() + exclusion.getHeight());
			}
		}
		return shiftY;
	}

	private static ImmutableRect2i calculateNavigationAreaAvoidingExclusions(
		ImmutableRect2i defaultNavigationArea,
		ImmutableRect2i slotBackgroundArea,
		Set<ImmutableRect2i> guiExclusionAreas,
		IIngredientGridConfig gridConfig
	) {
		if (defaultNavigationArea.isEmpty()) {
			return ImmutableRect2i.EMPTY;
		}

		if (guiExclusionAreas.stream().noneMatch(defaultNavigationArea::intersects)) {
			return defaultNavigationArea;
		}

		ImmutableRect2i navigationStripArea = calculateNavigationStripArea(
			slotBackgroundArea,
			defaultNavigationArea.getY(),
			defaultNavigationArea.getHeight(),
			gridConfig
		);
		int stripX = navigationStripArea.getX();
		int stripWidth = navigationStripArea.getWidth();

		List<int[]> excludedRanges = new ArrayList<>();
		for (ImmutableRect2i exclusion : guiExclusionAreas) {
			if (exclusion.intersects(navigationStripArea)) {
				int exclStart = Math.max(exclusion.getX(), stripX);
				int exclEnd = Math.min(exclusion.getX() + exclusion.getWidth(), stripX + stripWidth);
				if (exclStart < exclEnd) {
					excludedRanges.add(new int[]{exclStart, exclEnd});
				}
			}
		}

		if (excludedRanges.isEmpty()) {
			return defaultNavigationArea;
		}

		excludedRanges.sort(Comparator.comparingInt(a -> a[0]));

		List<int[]> gaps = new ArrayList<>();
		int currentX = stripX;
		for (int[] range : excludedRanges) {
			if (range[0] > currentX) {
				gaps.add(new int[]{currentX, range[0]});
			}
			currentX = Math.max(currentX, range[1]);
		}
		if (currentX < stripX + stripWidth) {
			gaps.add(new int[]{currentX, stripX + stripWidth});
		}

		if (gaps.isEmpty()) {
			return ImmutableRect2i.EMPTY;
		}

		int originalX = defaultNavigationArea.getX();
		int originalWidth = defaultNavigationArea.getWidth();

		int bestGapStart = -1;
		int bestGapWidth = 0;
		int bestDistance = Integer.MAX_VALUE;
		for (int[] gap : gaps) {
			int gapStart = gap[0];
			int gapWidth = gap[1] - gap[0];
			if (gapWidth < NAVIGATION_MIN_WIDTH) {
				continue;
			}
			int navWidthInGap = Math.min(originalWidth, gapWidth);
			int navStart = clamp(originalX, gapStart, gap[1] - navWidthInGap);
			int distance = Math.abs(navStart - originalX);
			if (distance < bestDistance) {
				bestGapStart = navStart;
				bestGapWidth = navWidthInGap;
				bestDistance = distance;
			}
		}

		if (bestGapStart < 0) {
			return ImmutableRect2i.EMPTY;
		}

		return new ImmutableRect2i(
			bestGapStart,
			navigationStripArea.getY(),
			bestGapWidth,
			navigationStripArea.getHeight()
		);
	}

	private static int clamp(int value, int min, int max) {
		return Math.max(min, Math.min(value, max));
	}

	private static ImmutableRect2i calculateNavigationStripArea(
		ImmutableRect2i slotBackgroundArea,
		int y,
		int height,
		IIngredientGridConfig gridConfig
	) {
		int x = slotBackgroundArea.getX();
		int right = slotBackgroundArea.getX() + slotBackgroundArea.getWidth();
		if (gridConfig.drawBackground()) {
			x -= BORDER_PADDING;
			right += BORDER_PADDING;
		}
		return new ImmutableRect2i(x, y, right - x, height);
	}

	record Layout(
		ImmutableRect2i ingredientGridArea,
		int availableSlotCount,
		ImmutableRect2i slotBackgroundArea,
		ImmutableRect2i navigationArea,
		ImmutableRect2i backgroundArea,
		boolean navigationEnabled
	) {
		boolean hasRoom() {
			return !ingredientGridArea.isEmpty() &&
				availableSlotCount > 0 &&
				(!navigationEnabled || !navigationArea.isEmpty());
		}
	}
}
