package mezz.jei.gui.overlay;

import mezz.jei.common.config.IIngredientGridConfig;
import mezz.jei.common.util.ImmutablePoint2i;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.common.util.MathUtil;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

final class IngredientGridWithNavigationLayout {
	private static final int NAVIGATION_HEIGHT = 20;
	private static final int BORDER_MARGIN = 6;
	private static final int BORDER_PADDING = 5;
	private static final int INNER_PADDING = 2;
	private static final int TAB_OVERLAP = 3;
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
		ImmutableRect2i ingredientGridArea = IngredientGridLayout.calculateBounds(gridConfig, availableGridArea);
		IngredientGridLayout.SlotInfo slotInfo = IngredientGridLayout.calculateSlotInfo(
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
			int shiftY = calculateNavigationShiftY(effectiveArea, guiExclusionAreas, gridConfig);
			if (shiftY > effectiveArea.y()) {
				int effectiveAreaBottom = effectiveArea.y() + effectiveArea.height();
				shiftY = Math.min(shiftY, effectiveAreaBottom);
				effectiveArea = new ImmutableRect2i(
					effectiveArea.x(), shiftY,
					effectiveArea.width(), effectiveAreaBottom - shiftY
				);
				availableGridArea = getAvailableGridArea(gridConfig, effectiveArea);
				ingredientGridArea = IngredientGridLayout.calculateBounds(gridConfig, availableGridArea);
				slotInfo = IngredientGridLayout.calculateSlotInfo(
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
			navigationArea
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

		ImmutableRect2i estimatedGridArea = IngredientGridLayout.calculateBounds(gridConfig, availableGridArea);
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
			IngredientGridLayout.calculateSlotInfo(ingredientGridArea, Set.of(), null).available(),
			navigationArea
		);
	}

	private static Layout calculateFromGridArea(
		IIngredientGridConfig gridConfig,
		ImmutableRect2i ingredientGridArea,
		int availableSlotCount,
		ImmutableRect2i navigationArea
	) {
		ImmutableRect2i slotBackgroundArea = calculateSlotBackgroundArea(ingredientGridArea, gridConfig);
		boolean navigationAligned = isNavigationAligned(slotBackgroundArea, navigationArea);

		ImmutableRect2i backgroundArea;
		ImmutableRect2i navigationBackgroundArea;
		if (navigationAligned || navigationArea.isEmpty()) {
			backgroundArea = MathUtil.union(slotBackgroundArea, navigationArea);
			if (gridConfig.drawBackground() && !backgroundArea.isEmpty()) {
				backgroundArea = backgroundArea.expandBy(BORDER_PADDING);
			}
			navigationBackgroundArea = ImmutableRect2i.EMPTY;
		} else {
			backgroundArea = slotBackgroundArea;
			if (gridConfig.drawBackground()) {
				backgroundArea = backgroundArea.expandBy(BORDER_PADDING);
				navigationBackgroundArea = calculateNavigationBackgroundArea(
					navigationArea, slotBackgroundArea
				);
			} else {
				navigationBackgroundArea = ImmutableRect2i.EMPTY;
			}
		}
		return new Layout(
			ingredientGridArea,
			availableSlotCount,
			slotBackgroundArea,
			navigationArea,
			backgroundArea,
			navigationBackgroundArea
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

	private static ImmutableRect2i calculateNavigationBackgroundArea(
		ImmutableRect2i navigationArea,
		ImmutableRect2i slotBackgroundArea
	) {
		int gridBgX = slotBackgroundArea.x() - BORDER_PADDING;
		int gridBgRight = slotBackgroundArea.x() + slotBackgroundArea.width() + BORDER_PADDING;
		int gridBgTop = slotBackgroundArea.y() - BORDER_PADDING;

		int navBgX = navigationArea.x() - BORDER_PADDING;
		int navBgRight = navigationArea.x() + navigationArea.width() + BORDER_PADDING;
		int navBgY = navigationArea.y() - BORDER_PADDING;
		// Overlap the grid background's top border by TAB_OVERLAP pixels.
		int navBgBottom = gridBgTop + TAB_OVERLAP;

		// Clamp to the grid background's horizontal bounds.
		navBgX = Math.max(navBgX, gridBgX);
		navBgRight = Math.min(navBgRight, gridBgRight);

		if (navBgRight <= navBgX || navBgBottom <= navBgY) {
			return ImmutableRect2i.EMPTY;
		}

		return new ImmutableRect2i(navBgX, navBgY, navBgRight - navBgX, navBgBottom - navBgY);
	}

	private static boolean isNavigationAligned(ImmutableRect2i slotBackgroundArea, ImmutableRect2i navigationArea) {
		if (navigationArea.isEmpty()) {
			return true;
		}
		return navigationArea.x() == slotBackgroundArea.x() &&
			navigationArea.width() == slotBackgroundArea.width() &&
			navigationArea.y() + navigationArea.height() <= slotBackgroundArea.y();
	}

	private static int calculateNavigationShiftY(
		ImmutableRect2i availableArea,
		Set<ImmutableRect2i> guiExclusionAreas,
		IIngredientGridConfig gridConfig
	) {
		int padding = gridConfig.drawBackground() ? BORDER_PADDING + INNER_PADDING : 0;
		int stripTop = availableArea.y() + BORDER_MARGIN;
		int stripBottom = stripTop + NAVIGATION_HEIGHT + INNER_PADDING + 2 * padding;

		int shiftY = availableArea.y();
		for (ImmutableRect2i exclusion : guiExclusionAreas) {
			if (exclusion.getY() < stripBottom && exclusion.getY() + exclusion.getHeight() > stripTop) {
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

		int padding = gridConfig.drawBackground() ? BORDER_PADDING + INNER_PADDING : 0;
		ImmutableRect2i paddedNavigationArea = padding > 0 ? defaultNavigationArea.expandBy(padding) : defaultNavigationArea;

		if (guiExclusionAreas.stream().noneMatch(paddedNavigationArea::intersects)) {
			return defaultNavigationArea;
		}

		int stripX = slotBackgroundArea.x();
		int stripRight = slotBackgroundArea.x() + slotBackgroundArea.width();
		if (gridConfig.drawBackground()) {
			stripX -= BORDER_PADDING;
			stripRight += BORDER_PADDING;
		}
		int stripWidth = stripRight - stripX;
		int stripY = defaultNavigationArea.y();
		int stripHeight = defaultNavigationArea.height();

		List<int[]> excludedRanges = new ArrayList<>();
		for (ImmutableRect2i exclusion : guiExclusionAreas) {
			if (exclusion.getY() < stripY + stripHeight + padding &&
				exclusion.getY() + exclusion.getHeight() > stripY - padding) {
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

		int originalX = defaultNavigationArea.x();
		int originalWidth = defaultNavigationArea.width();

		int bestGapStart = -1;
		int bestGapWidth = 0;
		int bestDistance = Integer.MAX_VALUE;
		for (int[] gap : gaps) {
			int gapStart = gap[0];
			int gapWidth = gap[1] - gap[0];
			int maxNavWidthInGap = gapWidth - 2 * padding;
			if (maxNavWidthInGap < NAVIGATION_MIN_WIDTH) {
				continue;
			}
			int navWidthInGap = Math.min(originalWidth, maxNavWidthInGap);
			int navStart = Math.clamp(originalX, gapStart + padding, gap[1] - padding - navWidthInGap);
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

		return new ImmutableRect2i(bestGapStart, stripY, bestGapWidth, stripHeight);
	}

	record Layout(
		ImmutableRect2i ingredientGridArea,
		int availableSlotCount,
		ImmutableRect2i slotBackgroundArea,
		ImmutableRect2i navigationArea,
		ImmutableRect2i backgroundArea,
		ImmutableRect2i navigationBackgroundArea
	) {
		boolean hasRoom() {
			return !ingredientGridArea.isEmpty();
		}
	}
}
