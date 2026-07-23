package mezz.jei.gui.overlay.history;

import mezz.jei.api.gui.placement.HorizontalAlignment;
import mezz.jei.api.gui.placement.VerticalAlignment;
import mezz.jei.common.config.IIngredientGridConfig;
import mezz.jei.common.config.IngredientGridNavigationMode;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.common.util.NavigationVisibility;
import mezz.jei.gui.overlay.ingredients.IngredientGridButtonNavigationLayout;
import mezz.jei.gui.overlay.ingredients.IngredientGridLayout;
import mezz.jei.gui.overlay.ingredients.IngredientGridWithNavigationLayout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LookupHistoryOverlayLayoutTest {
	private static final int BORDER_MARGIN = 6;
	private static final int INNER_PADDING = 2;
	private static final int BUTTON_SIZE = 20;
	private static final int LOOKUP_HISTORY_BOTTOM_PADDING = BORDER_MARGIN;
	private static final int LOOKUP_HISTORY_PADDING_EXTRA = LOOKUP_HISTORY_BOTTOM_PADDING - INNER_PADDING;
	private static final int LOOKUP_HISTORY_ROWS = 5;

	@ParameterizedTest(name = "alignment={0}")
	@MethodSource("gridAlignments")
	public void visualBottomEdgeDoesNotChangeWhenBackgroundIsEnabled(GridAlignment alignment) {
		// Setup: lookup history has less content height than its reserved area, so vertical alignment is observable.
		TestGridConfig withoutBackgroundConfig = config()
			.drawBackground(false)
			.maxRows(2)
			.horizontalAlignment(alignment.horizontalAlignment())
			.verticalAlignment(alignment.verticalAlignment());
		TestGridConfig withBackgroundConfig = config()
			.drawBackground(true)
			.maxRows(2)
			.horizontalAlignment(alignment.horizontalAlignment())
			.verticalAlignment(alignment.verticalAlignment());

		// Operation: calculate the visual lookup-history areas with and without a drawn background.
		LookupHistoryOverlayLayout withoutBackground = LookupHistoryOverlayLayout.calculate(
			withoutBackgroundConfig,
			lookupHistoryAvailableArea(false)
		);
		LookupHistoryOverlayLayout withBackground = LookupHistoryOverlayLayout.calculate(
			withBackgroundConfig,
			lookupHistoryAvailableArea(true)
		);

		// Assertions: enabling the background adds padding around the panel without moving its visual bottom edge.
		assertEquals(
			bottom(withoutBackground.backgroundArea()),
			bottom(withBackground.backgroundArea())
		);
	}

	@ParameterizedTest(name = "drawBackground={0}, horizontalAlignment={1}")
	@MethodSource("horizontalAlignmentConfigs")
	public void visualAreaFollowsIngredientListHorizontalAlignment(
		boolean drawBackground,
		HorizontalAlignment horizontalAlignment
	) {
		// Setup: lookup history is below the ingredient list and uses the same grid alignment config.
		TestGridConfig gridConfig = config()
			.drawBackground(drawBackground)
			.maxColumns(4)
			.navigationVisibility(NavigationVisibility.DISABLED)
			.horizontalAlignment(horizontalAlignment);

		// Operation: calculate the visual areas for the ingredient list and its lookup history.
		IngredientGridWithNavigationLayout ingredientListLayout = IngredientGridButtonNavigationLayout.calculate(
			gridConfig,
			ingredientListAvailableArea(drawBackground),
			Set.of(),
			null,
			100
		);
		LookupHistoryOverlayLayout lookupHistoryLayout = LookupHistoryOverlayLayout.calculate(
			gridConfig,
			ingredientListLookupHistoryAvailableArea(drawBackground)
		);

		// Assertions: lookup history shares the same aligned horizontal edge as the ingredient list above it.
		assertSharedHorizontalAlignedEdge(
			horizontalAlignment,
			ingredientListLayout.backgroundArea(),
			lookupHistoryLayout.backgroundArea()
		);
	}

	@ParameterizedTest(name = "drawBackground={0}, horizontalAlignment={1}")
	@MethodSource("horizontalAlignmentConfigs")
	public void visualAreaFollowsBookmarkListHorizontalAlignment(
		boolean drawBackground,
		HorizontalAlignment horizontalAlignment
	) {
		// Setup: lookup history is below the bookmark list and uses the same grid alignment config.
		TestGridConfig gridConfig = config()
			.drawBackground(drawBackground)
			.maxColumns(4)
			.navigationVisibility(NavigationVisibility.DISABLED)
			.horizontalAlignment(horizontalAlignment);

		// Operation: calculate the visual areas for the bookmark list and its lookup history.
		IngredientGridWithNavigationLayout bookmarkListLayout = IngredientGridButtonNavigationLayout.calculate(
			gridConfig,
			bookmarkListAvailableArea(drawBackground),
			Set.of(),
			null,
			100
		);
		LookupHistoryOverlayLayout lookupHistoryLayout = LookupHistoryOverlayLayout.calculate(
			gridConfig,
			bookmarkListLookupHistoryAvailableArea(drawBackground)
		);

		// Assertions: lookup history shares the same aligned horizontal edge as the bookmark list above it.
		assertSharedHorizontalAlignedEdge(
			horizontalAlignment,
			bookmarkListLayout.backgroundArea(),
			lookupHistoryLayout.backgroundArea()
		);
	}

	private static Stream<GridAlignment> gridAlignments() {
		return Stream.of(HorizontalAlignment.values())
			.flatMap(horizontalAlignment -> Stream.of(VerticalAlignment.values())
				.map(verticalAlignment -> new GridAlignment(horizontalAlignment, verticalAlignment)));
	}

	private static Stream<Arguments> horizontalAlignmentConfigs() {
		return Stream.of(false, true)
			.flatMap(drawBackground -> Stream.of(HorizontalAlignment.values())
				.map(horizontalAlignment -> Arguments.of(drawBackground, horizontalAlignment)));
	}

	private static ImmutableRect2i ingredientListAvailableArea(boolean lookupHistoryDrawBackground) {
		return overlayContentsAvailableArea(ingredientListDisplayArea(), lookupHistoryDrawBackground);
	}

	private static ImmutableRect2i bookmarkListAvailableArea(boolean lookupHistoryDrawBackground) {
		return overlayContentsAvailableArea(bookmarkListDisplayArea(), lookupHistoryDrawBackground);
	}

	private static ImmutableRect2i overlayContentsAvailableArea(
		ImmutableRect2i displayArea,
		boolean lookupHistoryDrawBackground
	) {
		int lookupHistoryDisplayHeight = LookupHistoryOverlayLayout.getDisplayHeight(
			LOOKUP_HISTORY_ROWS,
			lookupHistoryDrawBackground
		);
		return displayArea
			.cropBottom(BUTTON_SIZE + INNER_PADDING)
			.cropBottom(lookupHistoryDisplayHeight + LOOKUP_HISTORY_PADDING_EXTRA);
	}

	private static ImmutableRect2i lookupHistoryAvailableArea(boolean drawBackground) {
		return lookupHistoryAvailableArea(ingredientListDisplayArea(), drawBackground);
	}

	private static ImmutableRect2i ingredientListLookupHistoryAvailableArea(boolean drawBackground) {
		return lookupHistoryAvailableArea(ingredientListDisplayArea(), drawBackground);
	}

	private static ImmutableRect2i bookmarkListLookupHistoryAvailableArea(boolean drawBackground) {
		return lookupHistoryAvailableArea(bookmarkListDisplayArea(), drawBackground);
	}

	private static ImmutableRect2i lookupHistoryAvailableArea(ImmutableRect2i displayArea, boolean drawBackground) {
		return displayArea
			.insetBy(BORDER_MARGIN)
			.moveUp(BUTTON_SIZE + LOOKUP_HISTORY_BOTTOM_PADDING)
			.keepBottom(LookupHistoryOverlayLayout.getDisplayHeight(LOOKUP_HISTORY_ROWS, drawBackground));
	}

	private static ImmutableRect2i ingredientListDisplayArea() {
		return new ImmutableRect2i(
			150,
			0,
			14 * IngredientGridLayout.INGREDIENT_WIDTH,
			14 * IngredientGridLayout.INGREDIENT_HEIGHT
		);
	}

	private static ImmutableRect2i bookmarkListDisplayArea() {
		return new ImmutableRect2i(
			0,
			0,
			13 * IngredientGridLayout.INGREDIENT_WIDTH,
			14 * IngredientGridLayout.INGREDIENT_HEIGHT
		);
	}

	private static void assertSharedHorizontalAlignedEdge(
		HorizontalAlignment horizontalAlignment,
		ImmutableRect2i ownerListArea,
		ImmutableRect2i lookupHistoryArea
	) {
		switch (horizontalAlignment) {
			case LEFT -> assertEquals(
				ownerListArea.x(),
				lookupHistoryArea.x(),
				"left-aligned lookup history should share the owner list's left edge"
			);
			case CENTER -> assertEquals(
				centerX(ownerListArea),
				centerX(lookupHistoryArea),
				"center-aligned lookup history should share the owner list's center"
			);
			case RIGHT -> assertEquals(
				right(ownerListArea),
				right(lookupHistoryArea),
				"right-aligned lookup history should share the owner list's right edge"
			);
		}
	}

	private static int right(ImmutableRect2i area) {
		return area.x() + area.width();
	}

	private static int centerX(ImmutableRect2i area) {
		return area.x() + area.width() / 2;
	}

	private static int bottom(ImmutableRect2i area) {
		return area.y() + area.height();
	}

	private static TestGridConfig config() {
		return new TestGridConfig();
	}

	private record GridAlignment(HorizontalAlignment horizontalAlignment, VerticalAlignment verticalAlignment) {
		@Override
		public String toString() {
			return horizontalAlignment + " " + verticalAlignment;
		}
	}

	private static class TestGridConfig implements IIngredientGridConfig {
		private int maxColumns = 9;
		private int minColumns = 1;
		private int maxRows = 6;
		private int minRows = 1;
		private boolean drawBackground = true;
		private IngredientGridNavigationMode navigationMode = IngredientGridNavigationMode.PAGED;
		private HorizontalAlignment horizontalAlignment = HorizontalAlignment.LEFT;
		private VerticalAlignment verticalAlignment = VerticalAlignment.TOP;
		private NavigationVisibility navigationVisibility = NavigationVisibility.AUTO_HIDE;

		public TestGridConfig maxColumns(int maxColumns) {
			this.maxColumns = maxColumns;
			return this;
		}

		public TestGridConfig maxRows(int maxRows) {
			this.maxRows = maxRows;
			return this;
		}

		public TestGridConfig drawBackground(boolean drawBackground) {
			this.drawBackground = drawBackground;
			return this;
		}

		public TestGridConfig horizontalAlignment(HorizontalAlignment horizontalAlignment) {
			this.horizontalAlignment = horizontalAlignment;
			return this;
		}

		public TestGridConfig verticalAlignment(VerticalAlignment verticalAlignment) {
			this.verticalAlignment = verticalAlignment;
			return this;
		}

		public TestGridConfig navigationVisibility(NavigationVisibility navigationVisibility) {
			this.navigationVisibility = navigationVisibility;
			return this;
		}

		@Override
		public int getMaxColumns() {
			return maxColumns;
		}

		@Override
		public int getMinColumns() {
			return minColumns;
		}

		@Override
		public int getMaxRows() {
			return maxRows;
		}

		@Override
		public int getMinRows() {
			return minRows;
		}

		@Override
		public boolean drawBackground() {
			return drawBackground;
		}

		@Override
		public IngredientGridNavigationMode getNavigationMode() {
			return navigationMode;
		}

		@Override
		public HorizontalAlignment getHorizontalAlignment() {
			return horizontalAlignment;
		}

		@Override
		public VerticalAlignment getVerticalAlignment() {
			return verticalAlignment;
		}

		@Override
		public NavigationVisibility getNavigationVisibility() {
			return navigationVisibility;
		}
	}
}
