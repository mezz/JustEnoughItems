package mezz.jei.gui.overlay.ingredients;

import mezz.jei.common.config.IIngredientGridConfig;
import mezz.jei.common.config.IngredientGridLayoutMode;
import mezz.jei.common.util.ImmutablePoint2i;
import mezz.jei.common.util.ImmutableRect2i;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
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
		return switch (gridConfig.getNavigationVisibility()) {
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
		if (navigationEnabled && gridConfig.getLayoutMode() == IngredientGridLayoutMode.RECTANGULAR) {
			return calculateRectangularLayout(
				gridConfig,
				availableArea,
				guiExclusionAreas,
				mouseExclusionPoint
			);
		}

		ImmutableRect2i effectiveArea = availableArea;
		ImmutableRect2i availableGridArea = IngredientGridWithNavigationLayout.getAvailableGridArea(gridConfig, effectiveArea);
		ImmutableRect2i ingredientGridArea = IngredientGrid.calculateBounds(gridConfig, availableGridArea);
		int availableSlotCount = IngredientGrid.calculateAvailableSlotCount(
			ingredientGridArea,
			guiExclusionAreas,
			mouseExclusionPoint
		);

		ImmutableRect2i slotBackgroundArea = IngredientGridWithNavigationLayout.calculateSlotBackgroundArea(ingredientGridArea, gridConfig);
		ImmutableRect2i defaultNavigationArea = IngredientGridWithNavigationLayout.calculateNavigationArea(slotBackgroundArea, navigationEnabled);
		ImmutableRect2i navigationArea = calculateNavigationAreaAvoidingExclusions(
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
				ingredientGridArea = IngredientGrid.calculateBounds(gridConfig, availableGridArea);
				availableSlotCount = IngredientGrid.calculateAvailableSlotCount(
					ingredientGridArea, guiExclusionAreas, mouseExclusionPoint
				);
				slotBackgroundArea = IngredientGridWithNavigationLayout.calculateSlotBackgroundArea(ingredientGridArea, gridConfig);
				defaultNavigationArea = IngredientGridWithNavigationLayout.calculateNavigationArea(slotBackgroundArea, navigationEnabled);
				navigationArea = calculateNavigationAreaAvoidingExclusions(
					defaultNavigationArea, slotBackgroundArea, guiExclusionAreas, gridConfig
				);
			}
		}

		ImmutableRect2i backgroundNavigationArea = navigationArea.isEmpty() ?
			ImmutableRect2i.EMPTY :
			defaultNavigationArea;
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

	private static IngredientGridWithNavigationLayout calculateRectangularLayout(
		IIngredientGridConfig gridConfig,
		ImmutableRect2i availableArea,
		Set<ImmutableRect2i> guiExclusionAreas,
		@Nullable ImmutablePoint2i mouseExclusionPoint
	) {
		ImmutableRect2i availableGridArea = IngredientGridWithNavigationLayout.getAvailableGridArea(gridConfig, availableArea);
		ImmutableRect2i initialGridArea = IngredientGrid.calculateBounds(gridConfig, availableGridArea);
		IngredientGridWithNavigationLayout initialLayout = createRectangularLayout(
			gridConfig,
			initialGridArea,
			guiExclusionAreas,
			mouseExclusionPoint
		);
		if (initialLayout.hasRoom() || initialGridArea.isEmpty()) {
			return initialLayout;
		}

		int maxColumns = Math.min(
			availableGridArea.width() / IngredientGrid.INGREDIENT_WIDTH,
			gridConfig.getMaxColumns()
		);
		int maxRows = Math.min(
			availableGridArea.height() / IngredientGrid.INGREDIENT_HEIGHT,
			gridConfig.getMaxRows()
		);
		if (maxColumns < gridConfig.getMinColumns() || maxRows < gridConfig.getMinRows()) {
			return initialLayout;
		}

		int gridPadding = 0;
		if (gridConfig.drawBackground()) {
			gridPadding = IngredientGridWithNavigationLayout.INNER_PADDING;
		}
		int navigationToGridOffset = IngredientGridWithNavigationLayout.NAVIGATION_HEIGHT +
			IngredientGridWithNavigationLayout.INNER_PADDING +
			gridPadding;
		int availableGridRight = availableGridArea.x() + availableGridArea.width();
		int availableGridBottom = availableGridArea.y() + availableGridArea.height();

		// A clear navigation strip starts or ends at an exclusion edge, so these positions cover
		// the useful vertical alternatives without scanning every screen pixel.
		Set<Integer> yPositions = new LinkedHashSet<>();
		yPositions.add(initialGridArea.y());
		yPositions.add(availableGridArea.y());
		yPositions.add(availableGridBottom - (maxRows * IngredientGrid.INGREDIENT_HEIGHT));
		for (ImmutableRect2i exclusionArea : guiExclusionAreas) {
			yPositions.add(exclusionArea.y() + exclusionArea.height() + navigationToGridOffset);
			yPositions.add(
				exclusionArea.y() - IngredientGridWithNavigationLayout.NAVIGATION_HEIGHT + navigationToGridOffset
			);
		}

		RectangularLayoutCandidate bestCandidate = null;
		for (int columns = gridConfig.getMinColumns(); columns <= maxColumns; columns++) {
			int gridWidth = columns * IngredientGrid.INGREDIENT_WIDTH;
			// Keep navigation at the grid's full width and place it immediately beside exclusions.
			Set<Integer> xPositions = new LinkedHashSet<>();
			xPositions.add(initialGridArea.x());
			xPositions.add(availableGridArea.x());
			xPositions.add(availableGridRight - gridWidth);
			xPositions.add(
				availableGridArea.x() + gridConfig.getHorizontalAlignment().getXPos(availableGridArea.width(), gridWidth)
			);
			for (ImmutableRect2i exclusionArea : guiExclusionAreas) {
				xPositions.add(exclusionArea.x() + exclusionArea.width() + gridPadding);
				xPositions.add(exclusionArea.x() - gridWidth - gridPadding);
			}

			for (int gridY : yPositions) {
				int availableHeight = availableGridBottom - gridY;
				int rows = Math.min(availableHeight / IngredientGrid.INGREDIENT_HEIGHT, maxRows);
				if (rows < gridConfig.getMinRows()) {
					continue;
				}

				int gridHeight = rows * IngredientGrid.INGREDIENT_HEIGHT;
				for (int gridX : xPositions) {
					ImmutableRect2i ingredientGridArea = new ImmutableRect2i(gridX, gridY, gridWidth, gridHeight);
					if (!contains(availableGridArea, ingredientGridArea)) {
						continue;
					}

					IngredientGridWithNavigationLayout layout = createRectangularLayout(
						gridConfig,
						ingredientGridArea,
						guiExclusionAreas,
						mouseExclusionPoint
					);
					if (!layout.hasRoom()) {
						continue;
					}

					int displacement = Math.abs(ingredientGridArea.x() - initialGridArea.x()) +
						Math.abs(ingredientGridArea.y() - initialGridArea.y());
					RectangularLayoutCandidate candidate = new RectangularLayoutCandidate(layout, displacement);
					if (candidate.isBetterThan(bestCandidate)) {
						bestCandidate = candidate;
					}
				}
			}
		}

		if (bestCandidate == null) {
			return initialLayout;
		}
		return bestCandidate.layout();
	}

	private static IngredientGridWithNavigationLayout createRectangularLayout(
		IIngredientGridConfig gridConfig,
		ImmutableRect2i ingredientGridArea,
		Set<ImmutableRect2i> guiExclusionAreas,
		@Nullable ImmutablePoint2i mouseExclusionPoint
	) {
		int availableSlotCount = IngredientGrid.calculateAvailableSlotCount(
			ingredientGridArea,
			guiExclusionAreas,
			mouseExclusionPoint
		);
		ImmutableRect2i slotBackgroundArea = IngredientGridWithNavigationLayout.calculateSlotBackgroundArea(ingredientGridArea, gridConfig);
		ImmutableRect2i defaultNavigationArea = IngredientGridWithNavigationLayout.calculateNavigationArea(slotBackgroundArea, true);
		boolean navigationBlocked = guiExclusionAreas.stream()
			.anyMatch(defaultNavigationArea::intersects);
		ImmutableRect2i navigationArea = defaultNavigationArea;
		if (navigationBlocked) {
			navigationArea = ImmutableRect2i.EMPTY;
		}
		return IngredientGridWithNavigationLayout.fromGridArea(
			gridConfig,
			ingredientGridArea,
			availableSlotCount,
			navigationArea,
			navigationArea,
			true,
			ImmutableRect2i.EMPTY,
			false
		);
	}

	private static boolean contains(ImmutableRect2i outer, ImmutableRect2i inner) {
		return inner.x() >= outer.x() &&
			inner.y() >= outer.y() &&
			inner.x() + inner.width() <= outer.x() + outer.width() &&
			inner.y() + inner.height() <= outer.y() + outer.height();
	}

	private record RectangularLayoutCandidate(
		IngredientGridWithNavigationLayout layout,
		int displacement
	) {
		public boolean isBetterThan(@Nullable RectangularLayoutCandidate other) {
			if (other == null) {
				return true;
			}

			int slotCountComparison = Integer.compare(layout.availableSlotCount(), other.layout.availableSlotCount());
			if (slotCountComparison != 0) {
				return slotCountComparison > 0;
			}

			int rawSlotCount = getRawSlotCount(layout.ingredientGridArea());
			int otherRawSlotCount = getRawSlotCount(other.layout.ingredientGridArea());
			if (rawSlotCount != otherRawSlotCount) {
				return rawSlotCount > otherRawSlotCount;
			}

			if (displacement != other.displacement) {
				return displacement < other.displacement;
			}

			ImmutableRect2i ingredientGridArea = layout.ingredientGridArea();
			ImmutableRect2i otherIngredientGridArea = other.layout.ingredientGridArea();
			if (ingredientGridArea.width() != otherIngredientGridArea.width()) {
				return ingredientGridArea.width() > otherIngredientGridArea.width();
			}
			if (ingredientGridArea.y() != otherIngredientGridArea.y()) {
				return ingredientGridArea.y() < otherIngredientGridArea.y();
			}
			return ingredientGridArea.x() < otherIngredientGridArea.x();
		}

		private static int getRawSlotCount(ImmutableRect2i ingredientGridArea) {
			int columns = ingredientGridArea.width() / IngredientGrid.INGREDIENT_WIDTH;
			int rows = ingredientGridArea.height() / IngredientGrid.INGREDIENT_HEIGHT;
			return columns * rows;
		}
	}

	private static int calculateNavigationShiftY(
		ImmutableRect2i availableArea,
		ImmutableRect2i slotBackgroundArea,
		Set<ImmutableRect2i> guiExclusionAreas,
		IIngredientGridConfig gridConfig
	) {
		int padding = gridConfig.drawBackground() ?
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
			int navStart = clamp(originalX, gapStart, gapEnd - navWidthInGap);
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

	private static int clamp(int value, int min, int max) {
		return Math.max(min, Math.min(value, max));
	}

	private static ImmutableRect2i calculateNavigationStripArea(
		ImmutableRect2i slotBackgroundArea,
		int y,
		int height,
		IIngredientGridConfig gridConfig
	) {
		int x = slotBackgroundArea.x();
		int right = slotBackgroundArea.x() + slotBackgroundArea.width();
		if (gridConfig.drawBackground()) {
			x -= IngredientGridWithNavigationLayout.BORDER_PADDING;
			right += IngredientGridWithNavigationLayout.BORDER_PADDING;
		}
		return new ImmutableRect2i(x, y, right - x, height);
	}
}
