package mezz.jei.gui.overlay.ingredients;

import mezz.jei.common.config.IIngredientGridConfig;
import mezz.jei.common.config.IngredientGridLayoutMode;
import mezz.jei.common.util.ImmutablePoint2i;
import mezz.jei.common.util.ImmutableRect2i;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public final class IngredientGridButtonNavigationLayout {
	private static final int NAVIGATION_MIN_WIDTH = IngredientGridWithNavigationLayout.NAVIGATION_HEIGHT +
		IngredientGridWithNavigationLayout.BORDER_PADDING +
		IngredientGridWithNavigationLayout.INNER_PADDING;

	private IngredientGridButtonNavigationLayout() {
	}

	public static IngredientGridWithNavigationLayout calculate(
		IIngredientGridConfig gridConfig,
		ImmutableRect2i availableArea,
		Set<ImmutableRect2i> guiExclusionAreas,
		@Nullable ImmutablePoint2i mouseExclusionPoint,
		int ingredientCount
	) {
		return switch (gridConfig.navigationVisibility().getValue()) {
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

	private static IngredientGridWithNavigationLayout calculateAutoHide(
		IIngredientGridConfig gridConfig,
		ImmutableRect2i availableArea,
		Set<ImmutableRect2i> guiExclusionAreas,
		@Nullable ImmutablePoint2i mouseExclusionPoint,
		int ingredientCount
	) {
		IngredientGridWithNavigationLayout layoutWithoutNavigation = calculateForNavigation(
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

	private static IngredientGridWithNavigationLayout calculateForNavigation(
		IIngredientGridConfig gridConfig,
		ImmutableRect2i availableArea,
		Set<ImmutableRect2i> guiExclusionAreas,
		@Nullable ImmutablePoint2i mouseExclusionPoint,
		boolean navigationEnabled
	) {
		ImmutableRect2i effectiveArea = availableArea;
		ImmutableRect2i availableGridArea = IngredientGridWithNavigationLayout.getAvailableGridArea(gridConfig, effectiveArea);
		ImmutableRect2i ingredientGridArea = IngredientGridLayout.calculateBounds(gridConfig, availableGridArea);
		int availableSlotCount = IngredientGridLayout.calculateAvailableSlotCount(
			ingredientGridArea,
			guiExclusionAreas,
			mouseExclusionPoint
		);

		ImmutableRect2i slotBackgroundArea = IngredientGridWithNavigationLayout.calculateSlotBackgroundArea(ingredientGridArea, gridConfig);
		ImmutableRect2i defaultNavigationArea = IngredientGridWithNavigationLayout.calculateNavigationArea(slotBackgroundArea, navigationEnabled);
		ImmutableRect2i navigationArea = calculateNavigationArea(
			defaultNavigationArea, slotBackgroundArea, guiExclusionAreas, gridConfig
		);

		if (navigationEnabled && navigationArea.isEmpty() && !defaultNavigationArea.isEmpty()) {
			int shiftY = calculateNavigationShiftY(effectiveArea, slotBackgroundArea, guiExclusionAreas, gridConfig);
			if (shiftY > effectiveArea.y()) {
				int effectiveAreaBottom = effectiveArea.y() + effectiveArea.height();
				shiftY = Math.min(shiftY, effectiveAreaBottom);
				effectiveArea = new ImmutableRect2i(
					effectiveArea.x(), shiftY,
					effectiveArea.width(), effectiveAreaBottom - shiftY
				);
				availableGridArea = IngredientGridWithNavigationLayout.getAvailableGridArea(gridConfig, effectiveArea);
				ingredientGridArea = IngredientGridLayout.calculateBounds(gridConfig, availableGridArea);
				availableSlotCount = IngredientGridLayout.calculateAvailableSlotCount(
					ingredientGridArea, guiExclusionAreas, mouseExclusionPoint
				);
				slotBackgroundArea = IngredientGridWithNavigationLayout.calculateSlotBackgroundArea(ingredientGridArea, gridConfig);
				defaultNavigationArea = IngredientGridWithNavigationLayout.calculateNavigationArea(slotBackgroundArea, navigationEnabled);
				navigationArea = calculateNavigationArea(
					defaultNavigationArea, slotBackgroundArea, guiExclusionAreas, gridConfig
				);
			}
		}

		ImmutableRect2i backgroundNavigationArea;
		if (navigationArea.isEmpty()) {
			backgroundNavigationArea = ImmutableRect2i.EMPTY;
		} else {
			backgroundNavigationArea = defaultNavigationArea;
		}
		return IngredientGridWithNavigationLayout.fromGridArea(
			gridConfig,
			ingredientGridArea,
			availableSlotCount,
			navigationArea,
			backgroundNavigationArea,
			navigationEnabled,
			ImmutableRect2i.EMPTY,
			false
		);
	}

	private static ImmutableRect2i calculateNavigationArea(
		ImmutableRect2i defaultNavigationArea,
		ImmutableRect2i slotBackgroundArea,
		Set<ImmutableRect2i> guiExclusionAreas,
		IIngredientGridConfig gridConfig
	) {
		if (gridConfig.layoutMode().getValue() == IngredientGridLayoutMode.RECTANGULAR) {
			boolean blocked = guiExclusionAreas.stream()
				.anyMatch(defaultNavigationArea::intersects);
			if (blocked) {
				return ImmutableRect2i.EMPTY;
			}
			return defaultNavigationArea;
		}

		return calculateNavigationAreaAvoidingExclusions(
			defaultNavigationArea,
			slotBackgroundArea,
			guiExclusionAreas,
			gridConfig
		);
	}

	private static int calculateNavigationShiftY(
		ImmutableRect2i availableArea,
		ImmutableRect2i slotBackgroundArea,
		Set<ImmutableRect2i> guiExclusionAreas,
		IIngredientGridConfig gridConfig
	) {
		int padding = gridConfig.drawBackground().getValue() ?
			IngredientGridWithNavigationLayout.BORDER_PADDING + IngredientGridWithNavigationLayout.INNER_PADDING :
			0;
		int stripTop = availableArea.y() + IngredientGridWithNavigationLayout.BORDER_MARGIN;
		int stripHeight = IngredientGridWithNavigationLayout.NAVIGATION_HEIGHT +
			IngredientGridWithNavigationLayout.INNER_PADDING +
			2 * padding;
		ImmutableRect2i navigationStripArea = calculateNavigationStripArea(
			slotBackgroundArea,
			stripTop,
			stripHeight,
			gridConfig
		);

		int shiftY = availableArea.y();
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
			defaultNavigationArea.y(),
			defaultNavigationArea.height(),
			gridConfig
		);
		int stripX = navigationStripArea.x();
		int stripWidth = navigationStripArea.width();

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

		int originalX = defaultNavigationArea.x();
		int originalWidth = defaultNavigationArea.width();
		int originalRight = originalX + originalWidth;

		ImmutableRect2i navigationArea = calculateNavigationAreaInGaps(
			gaps,
			originalX,
			originalRight,
			originalX,
			originalWidth,
			navigationStripArea.y(),
			navigationStripArea.height()
		);
		if (!navigationArea.isEmpty()) {
			return navigationArea;
		}

		return ImmutableRect2i.EMPTY;
	}

	private static ImmutableRect2i calculateNavigationAreaInGaps(
		List<int[]> gaps,
		int minX,
		int maxX,
		int originalX,
		int originalWidth,
		int y,
		int height
	) {
		int bestGapStart = -1;
		int bestGapWidth = 0;
		int bestDistance = Integer.MAX_VALUE;
		for (int[] gap : gaps) {
			int gapStart = Math.max(gap[0], minX);
			int gapEnd = Math.min(gap[1], maxX);
			int gapWidth = gapEnd - gapStart;
			if (gapWidth < NAVIGATION_MIN_WIDTH) {
				continue;
			}
			int navWidthInGap = Math.min(originalWidth, gapWidth);
			int navStart = Math.clamp(originalX, gapStart, gapEnd - navWidthInGap);
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
			y,
			bestGapWidth,
			height
		);
	}

	private static ImmutableRect2i calculateNavigationStripArea(
		ImmutableRect2i slotBackgroundArea,
		int y,
		int height,
		IIngredientGridConfig gridConfig
	) {
		int x = slotBackgroundArea.x();
		int right = slotBackgroundArea.x() + slotBackgroundArea.width();
		if (gridConfig.drawBackground().getValue()) {
			x -= IngredientGridWithNavigationLayout.BORDER_PADDING;
			right += IngredientGridWithNavigationLayout.BORDER_PADDING;
		}
		return new ImmutableRect2i(x, y, right - x, height);
	}
}
