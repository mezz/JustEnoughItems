package mezz.jei.gui.overlay;

import mezz.jei.api.gui.placement.HorizontalAlignment;
import mezz.jei.api.gui.placement.VerticalAlignment;
import mezz.jei.common.config.IIngredientGridConfig;
import mezz.jei.common.util.ImmutablePoint2i;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.common.util.ImmutableSize2i;
import mezz.jei.common.util.NavigationVisibility;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class IngredientGridConfigTest {
	@Test
	public void maxColumnsAndRowsLimitGridSize() {
		// Setup: the available area could fit many more slots than the configured maximum.
		TestGridConfig gridConfig = config()
			.maxColumns(3)
			.maxRows(2)
			.minColumns(1)
			.minRows(1);
		ImmutableRect2i expectedGridArea = new ImmutableRect2i(
			0,
			0,
			3 * IngredientGridLayout.INGREDIENT_WIDTH,
			2 * IngredientGridLayout.INGREDIENT_HEIGHT
		);
		ImmutableRect2i availableArea = new ImmutableRect2i(
			0,
			0,
			10 * IngredientGridLayout.INGREDIENT_WIDTH,
			10 * IngredientGridLayout.INGREDIENT_HEIGHT
		);

		// Operation: calculate the grid size from the configured limits.
		ImmutableSize2i size = IngredientGridLayout.calculateSize(gridConfig, availableArea);

		// Assertions: the grid is capped by max columns and rows.
		assertEquals(expectedGridArea.getSize(), size);
	}

	@Test
	public void minColumnsAndRowsRequireEnoughAvailableArea() {
		// Setup: the grid requires at least three columns and two rows.
		TestGridConfig gridConfig = config()
			.maxColumns(9)
			.maxRows(6)
			.minColumns(3)
			.minRows(2);
		ImmutableRect2i tooNarrowArea = new ImmutableRect2i(
			0,
			0,
			(3 * IngredientGridLayout.INGREDIENT_WIDTH) - 1,
			2 * IngredientGridLayout.INGREDIENT_HEIGHT
		);
		ImmutableRect2i tooShortArea = new ImmutableRect2i(
			0,
			0,
			3 * IngredientGridLayout.INGREDIENT_WIDTH,
			(2 * IngredientGridLayout.INGREDIENT_HEIGHT) - 1
		);
		ImmutableRect2i exactMinimumArea = new ImmutableRect2i(
			0,
			0,
			3 * IngredientGridLayout.INGREDIENT_WIDTH,
			2 * IngredientGridLayout.INGREDIENT_HEIGHT
		);

		// Operation: calculate sizes around the minimum configured dimensions.
		ImmutableSize2i tooNarrowSize = IngredientGridLayout.calculateSize(gridConfig, tooNarrowArea);
		ImmutableSize2i tooShortSize = IngredientGridLayout.calculateSize(gridConfig, tooShortArea);
		ImmutableSize2i exactMinimumSize = IngredientGridLayout.calculateSize(gridConfig, exactMinimumArea);

		// Assertions: the grid has no room below the minimum, and exactly enough room at the minimum.
		assertEquals(ImmutableSize2i.EMPTY, tooNarrowSize);
		assertEquals(ImmutableSize2i.EMPTY, tooShortSize);
		assertEquals(exactMinimumArea.getSize(), exactMinimumSize);
	}

	@Test
	public void horizontalAndVerticalAlignmentPositionGridInAvailableArea() {
		// Setup: a bounded grid leaves extra room on both axes, so alignment can move it inside the available area.
		ImmutableRect2i availableArea = new ImmutableRect2i(
			50,
			40,
			7 * IngredientGridLayout.INGREDIENT_WIDTH,
			6 * IngredientGridLayout.INGREDIENT_HEIGHT
		);
		TestGridConfig gridConfig = config()
			.maxColumns(3)
			.maxRows(2);
		ImmutableSize2i expectedGridSize = IngredientGridLayout.calculateSize(gridConfig, availableArea);

		// Operation: calculate bounds for every configured alignment value.
		ImmutableRect2i topLeft = IngredientGridLayout.calculateBounds(
			gridConfig
				.horizontalAlignment(HorizontalAlignment.LEFT)
				.verticalAlignment(VerticalAlignment.TOP),
			availableArea
		);
		ImmutableRect2i centered = IngredientGridLayout.calculateBounds(
			gridConfig
				.horizontalAlignment(HorizontalAlignment.CENTER)
				.verticalAlignment(VerticalAlignment.CENTER),
			availableArea
		);
		ImmutableRect2i bottomRight = IngredientGridLayout.calculateBounds(
			gridConfig
				.horizontalAlignment(HorizontalAlignment.RIGHT)
				.verticalAlignment(VerticalAlignment.BOTTOM),
			availableArea
		);

		// Assertions: each configured alignment controls the grid's position without changing its slot size.
		assertSize(topLeft, expectedGridSize);
		assertAlignedToLeft(topLeft, availableArea);
		assertAlignedToTop(topLeft, availableArea);

		assertSize(centered, expectedGridSize);
		assertCenteredHorizontally(centered, availableArea);
		assertCenteredVertically(centered, availableArea);

		assertSize(bottomRight, expectedGridSize);
		assertAlignedToRight(bottomRight, availableArea);
		assertAlignedToBottom(bottomRight, availableArea);
	}

	@Test
	public void enabledNavigationVisibilityShowsNavigationForSinglePage() {
		// Setup: navigation is configured to always show.
		ImmutableRect2i availableArea = largeAvailableArea();
		TestGridConfig gridConfig = config()
			.drawBackground(false)
			.buttonNavigationVisibility(NavigationVisibility.ENABLED);

		// Operation: calculate layout for a single-page ingredient list.
		IngredientGridWithNavigationLayout.Layout layout = IngredientGridWithNavigationLayout.calculate(
			gridConfig,
			availableArea,
			Set.of(),
			null,
			1
		);

		// Assertions: the enabled config reserves navigation even when there is only one page.
		assertPositiveArea(layout.navigationArea());
	}

	@Test
	public void disabledNavigationVisibilityHidesNavigationForMultiplePages() {
		// Setup: navigation is configured to never show.
		ImmutableRect2i availableArea = largeAvailableArea();
		TestGridConfig gridConfig = config()
			.drawBackground(false)
			.buttonNavigationVisibility(NavigationVisibility.DISABLED);

		// Operation: calculate layout for a multi-page ingredient list.
		IngredientGridWithNavigationLayout.Layout layout = IngredientGridWithNavigationLayout.calculate(
			gridConfig,
			availableArea,
			Set.of(),
			null,
			100
		);

		// Assertions: the disabled config suppresses navigation even when there are multiple pages.
		assertEquals(ImmutableRect2i.EMPTY, layout.navigationArea());
		assertPositiveArea(layout.ingredientGridArea());
	}

	@Test
	public void autoHideNavigationVisibilityShowsNavigationOnlyForMultiplePagesWithRoom() {
		// Setup: navigation is configured to auto-hide.
		ImmutableRect2i availableArea = largeAvailableArea();
		TestGridConfig gridConfig = config()
			.maxColumns(4)
			.maxRows(3)
			.drawBackground(false)
			.buttonNavigationVisibility(NavigationVisibility.AUTO_HIDE);
		IngredientGridWithNavigationLayout.Layout referenceLayout = IngredientGridWithNavigationLayout.calculate(
			config()
				.maxColumns(4)
				.maxRows(3)
				.drawBackground(false)
				.buttonNavigationVisibility(NavigationVisibility.DISABLED),
			availableArea,
			Set.of(),
			null,
			0
		);
		int onePageIngredientCount = referenceLayout.availableSlotCount();

		// Operation: calculate layout for no grid room, one-page, and multi-page states.
		IngredientGridWithNavigationLayout.Layout hiddenWithoutRoom = IngredientGridWithNavigationLayout.calculate(
			gridConfig,
			ImmutableRect2i.EMPTY,
			Set.of(),
			null,
			onePageIngredientCount + 1
		);
		IngredientGridWithNavigationLayout.Layout hiddenWithOnePage = IngredientGridWithNavigationLayout.calculate(
			gridConfig,
			availableArea,
			Set.of(),
			null,
			onePageIngredientCount
		);
		IngredientGridWithNavigationLayout.Layout shownWithMultiplePages = IngredientGridWithNavigationLayout.calculate(
			gridConfig,
			availableArea,
			Set.of(),
			null,
			onePageIngredientCount + 1
		);

		// Assertions: auto-hide only shows navigation when the grid has room and more than one page.
		assertEquals(ImmutableRect2i.EMPTY, hiddenWithoutRoom.navigationArea());
		assertEquals(ImmutableRect2i.EMPTY, hiddenWithOnePage.navigationArea());
		assertPositiveArea(shownWithMultiplePages.navigationArea());
	}

	@Test
	public void autoHideNavigationUsesMouseBlockedSlotCapacity() {
		// Setup: a mouse exclusion blocks one otherwise available slot in an auto-hide grid.
		ImmutableRect2i availableArea = largeAvailableArea();
		TestGridConfig disabledGridConfig = config()
			.maxColumns(4)
			.maxRows(3)
			.drawBackground(false)
			.buttonNavigationVisibility(NavigationVisibility.DISABLED);
		TestGridConfig autoHideGridConfig = config()
			.maxColumns(4)
			.maxRows(3)
			.drawBackground(false)
			.buttonNavigationVisibility(NavigationVisibility.AUTO_HIDE);
		IngredientGridWithNavigationLayout.Layout unblockedLayout = IngredientGridWithNavigationLayout.calculate(
			disabledGridConfig,
			availableArea,
			Set.of(),
			null,
			0
		);
		ImmutablePoint2i mouseExclusionPoint = new ImmutablePoint2i(
			unblockedLayout.ingredientGridArea().x(),
			unblockedLayout.ingredientGridArea().y()
		);

		// Operation: calculate layout with one slot blocked and enough ingredients to need that slot.
		IngredientGridWithNavigationLayout.Layout blockedLayout = IngredientGridWithNavigationLayout.calculate(
			disabledGridConfig,
			availableArea,
			Set.of(),
			mouseExclusionPoint,
			0
		);
		IngredientGridWithNavigationLayout.Layout autoHideLayout = IngredientGridWithNavigationLayout.calculate(
			autoHideGridConfig,
			availableArea,
			Set.of(),
			mouseExclusionPoint,
			unblockedLayout.availableSlotCount()
		);

		// Assertions: the blocked slot reduces capacity, so auto-hide reserves navigation for the overflow item.
		assertEquals(unblockedLayout.availableSlotCount() - 1, blockedLayout.availableSlotCount());
		assertPositiveArea(autoHideLayout.navigationArea());
	}

	@Test
	public void autoHideNavigationUsesGuiExclusionBlockedSlotCapacity() {
		// Setup: a GUI exclusion blocks one otherwise available slot in an auto-hide grid.
		ImmutableRect2i availableArea = largeAvailableArea();
		TestGridConfig disabledGridConfig = config()
			.maxColumns(4)
			.maxRows(3)
			.drawBackground(false)
			.buttonNavigationVisibility(NavigationVisibility.DISABLED);
		TestGridConfig autoHideGridConfig = config()
			.maxColumns(4)
			.maxRows(3)
			.drawBackground(false)
			.buttonNavigationVisibility(NavigationVisibility.AUTO_HIDE);
		IngredientGridWithNavigationLayout.Layout unblockedLayout = IngredientGridWithNavigationLayout.calculate(
			disabledGridConfig,
			availableArea,
			Set.of(),
			null,
			0
		);
		Set<ImmutableRect2i> guiExclusionAreas = Set.of(new ImmutableRect2i(
			unblockedLayout.ingredientGridArea().x(),
			unblockedLayout.ingredientGridArea().y(),
			1,
			1
		));

		// Operation: calculate layout with one slot blocked and enough ingredients to need that slot.
		IngredientGridWithNavigationLayout.Layout blockedLayout = IngredientGridWithNavigationLayout.calculate(
			disabledGridConfig,
			availableArea,
			guiExclusionAreas,
			null,
			0
		);
		IngredientGridWithNavigationLayout.Layout autoHideLayout = IngredientGridWithNavigationLayout.calculate(
			autoHideGridConfig,
			availableArea,
			guiExclusionAreas,
			null,
			unblockedLayout.availableSlotCount()
		);

		// Assertions: the blocked slot reduces capacity, so auto-hide reserves navigation for the overflow item.
		assertEquals(unblockedLayout.availableSlotCount() - 1, blockedLayout.availableSlotCount());
		assertPositiveArea(autoHideLayout.navigationArea());
	}

	@Test
	public void navigationAreaIsOnlyReservedWhenNavigationIsEnabled() {
		// Setup: a grid area is available, and the background is disabled so the visible bounds come from content.
		TestGridConfig gridConfig = config()
			.drawBackground(false);
		ImmutableRect2i gridArea = new ImmutableRect2i(
			30,
			50,
			4 * IngredientGridLayout.INGREDIENT_WIDTH,
			3 * IngredientGridLayout.INGREDIENT_HEIGHT
		);

		// Operation: calculate layout with navigation enabled and disabled.
		IngredientGridWithNavigationLayout.Layout enabledLayout =
			IngredientGridWithNavigationLayout.calculateFromGridArea(gridConfig, ImmutableRect2i.EMPTY, gridArea, true);
		IngredientGridWithNavigationLayout.Layout disabledLayout =
			IngredientGridWithNavigationLayout.calculateFromGridArea(gridConfig, ImmutableRect2i.EMPTY, gridArea, false);

		// Assertions: enabled navigation reserves an area above the grid, while disabled navigation has no area.
		assertPositiveArea(enabledLayout.navigationArea());
		assertTrue(
			bottom(enabledLayout.navigationArea()) <= enabledLayout.slotBackgroundArea().y(),
			"navigation should be above the grid slots"
		);
		assertEquals(ImmutableRect2i.EMPTY, disabledLayout.navigationArea());
		assertEquals(gridArea, disabledLayout.backgroundArea());
	}

	@Test
	public void drawBackgroundAddsPaddingAroundSlotsAndOuterBackground() {
		// Setup: the same grid area is laid out with and without the ingredient-list background.
		ImmutableRect2i gridArea = new ImmutableRect2i(
			30,
			50,
			4 * IngredientGridLayout.INGREDIENT_WIDTH,
			3 * IngredientGridLayout.INGREDIENT_HEIGHT
		);
		TestGridConfig withoutBackgroundConfig = config()
			.drawBackground(false);
		TestGridConfig withBackgroundConfig = config()
			.drawBackground(true);

		// Operation: calculate layout with navigation disabled so background padding is isolated.
		IngredientGridWithNavigationLayout.Layout withoutBackground =
			IngredientGridWithNavigationLayout.calculateFromGridArea(withoutBackgroundConfig, ImmutableRect2i.EMPTY, gridArea, false);
		IngredientGridWithNavigationLayout.Layout withBackground =
			IngredientGridWithNavigationLayout.calculateFromGridArea(withBackgroundConfig, ImmutableRect2i.EMPTY, gridArea, false);

		// Assertions: background drawing adds padding around slots and around the final background area.
		assertEquals(gridArea, withoutBackground.slotBackgroundArea());
		assertEquals(gridArea, withoutBackground.backgroundArea());
		assertContainedBy(gridArea, withBackground.slotBackgroundArea());
		assertContainedBy(withBackground.slotBackgroundArea(), withBackground.backgroundArea());
		assertTrue(withBackground.slotBackgroundArea().width() > withoutBackground.slotBackgroundArea().width());
		assertTrue(withBackground.backgroundArea().width() > withBackground.slotBackgroundArea().width());
	}

	@Test
	public void drawBackgroundAndNavigationCombineIntoOneOuterBackgroundArea() {
		// Setup: both the background and button navigation are enabled.
		TestGridConfig gridConfig = config()
			.drawBackground(true);
		ImmutableRect2i gridArea = new ImmutableRect2i(
			30,
			50,
			4 * IngredientGridLayout.INGREDIENT_WIDTH,
			3 * IngredientGridLayout.INGREDIENT_HEIGHT
		);

		// Operation: calculate the combined layout.
		IngredientGridWithNavigationLayout.Layout layout =
			IngredientGridWithNavigationLayout.calculateFromGridArea(gridConfig, ImmutableRect2i.EMPTY, gridArea, true);

		// Assertions: the outer background contains both the slot background and the navigation area.
		assertPositiveArea(layout.navigationArea());
		assertContainedBy(layout.slotBackgroundArea(), layout.backgroundArea());
		assertContainedBy(layout.navigationArea(), layout.backgroundArea());
		assertTrue(
			layout.backgroundArea().y() < layout.slotBackgroundArea().y(),
			"combined background should extend above the slots when navigation is visible"
		);
	}

	@Test
	public void drawBackgroundReducesAvailableGridAreaBeforeSizing() {
		// Setup: the same screen area is available with and without background padding.
		ImmutableRect2i availableArea = new ImmutableRect2i(
			0,
			0,
			16 * IngredientGridLayout.INGREDIENT_WIDTH,
			12 * IngredientGridLayout.INGREDIENT_HEIGHT
		);
		TestGridConfig withoutBackgroundConfig = config()
			.drawBackground(false);
		TestGridConfig withBackgroundConfig = config()
			.drawBackground(true);

		// Operation: calculate the area passed down to ingredient grid sizing.
		ImmutableRect2i withoutBackground = IngredientGridWithNavigationLayout.getAvailableGridArea(
			withoutBackgroundConfig,
			availableArea,
			Set.of(),
			false
		);
		ImmutableRect2i withBackground = IngredientGridWithNavigationLayout.getAvailableGridArea(
			withBackgroundConfig,
			availableArea,
			Set.of(),
			false
		);

		// Assertions: background padding reduces the usable grid area while keeping it inside the unpadded area.
		assertContainedBy(withBackground, withoutBackground);
		assertTrue(withBackground.x() > withoutBackground.x(), "background padding should inset the left edge");
		assertTrue(withBackground.y() > withoutBackground.y(), "background padding should inset the top edge");
		assertTrue(withBackground.width() < withoutBackground.width(), "background padding should reduce width");
		assertTrue(withBackground.height() < withoutBackground.height(), "background padding should reduce height");
	}

	@Test
	public void exclusionInNavigationBackgroundPaddingMovesLayout() {
		// Setup: an exclusion overlaps the padded background just above visible navigation.
		ImmutableRect2i availableArea = largeAvailableArea();
		TestGridConfig gridConfig = config()
			.maxColumns(4)
			.maxRows(3)
			.drawBackground(true)
			.buttonNavigationVisibility(NavigationVisibility.ENABLED);
		IngredientGridWithNavigationLayout.Layout unobstructedLayout = IngredientGridWithNavigationLayout.calculate(
			gridConfig,
			availableArea,
			Set.of(),
			null,
			0
		);
		ImmutableRect2i navigationPaddingExclusion = new ImmutableRect2i(
			unobstructedLayout.navigationArea().x(),
			unobstructedLayout.navigationArea().y() - 1,
			1,
			1
		);

		// Operation: recalculate layout with the padded navigation background occupied.
		IngredientGridWithNavigationLayout.Layout obstructedLayout = IngredientGridWithNavigationLayout.calculate(
			gridConfig,
			availableArea,
			Set.of(navigationPaddingExclusion),
			null,
			0
		);

		// Assertions: the exclusion is outside the buttons but still inside their background padding, so the
		// layout moves the whole background away from it.
		assertFalse(unobstructedLayout.navigationArea().intersects(navigationPaddingExclusion));
		assertTrue(unobstructedLayout.backgroundArea().intersects(navigationPaddingExclusion));
		assertFalse(obstructedLayout.backgroundArea().intersects(navigationPaddingExclusion));
		assertTrue(obstructedLayout.ingredientGridArea().y() > unobstructedLayout.ingredientGridArea().y());
	}

	@Test
	public void disabledNavigationDoesNotCreateBackgroundPaddingExclusionArea() {
		// Setup: navigation is disabled, and the exclusion is near the origin where an expanded empty rectangle
		// would appear if the absent navigation area were padded.
		ImmutableRect2i availableArea = largeAvailableArea();
		TestGridConfig gridConfig = config()
			.maxColumns(4)
			.maxRows(3)
			.drawBackground(true)
			.buttonNavigationVisibility(NavigationVisibility.DISABLED);
		IngredientGridWithNavigationLayout.Layout unobstructedLayout = IngredientGridWithNavigationLayout.calculate(
			gridConfig,
			availableArea,
			Set.of(),
			null,
			0
		);
		ImmutableRect2i falseNavigationExclusion = new ImmutableRect2i(0, 0, 1, 1);

		// Operation: recalculate layout with an exclusion that should not intersect any disabled navigation area.
		IngredientGridWithNavigationLayout.Layout obstructedLayout = IngredientGridWithNavigationLayout.calculate(
			gridConfig,
			availableArea,
			Set.of(falseNavigationExclusion),
			null,
			0
		);

		// Assertions: disabled navigation remains absent, and its nonexistent background padding does not move
		// the ingredient grid.
		assertEquals(ImmutableRect2i.EMPTY, unobstructedLayout.navigationArea());
		assertEquals(ImmutableRect2i.EMPTY, obstructedLayout.navigationArea());
		assertEquals(unobstructedLayout.ingredientGridArea(), obstructedLayout.ingredientGridArea());
		assertEquals(unobstructedLayout.backgroundArea(), obstructedLayout.backgroundArea());
	}

	private static void assertSize(ImmutableRect2i bounds, ImmutableSize2i expectedSize) {
		assertEquals(expectedSize.width(), bounds.width());
		assertEquals(expectedSize.height(), bounds.height());
	}

	private static void assertAlignedToLeft(ImmutableRect2i bounds, ImmutableRect2i availableArea) {
		assertEquals(availableArea.x(), bounds.x());
		assertContainedBy(bounds, availableArea);
	}

	private static void assertAlignedToTop(ImmutableRect2i bounds, ImmutableRect2i availableArea) {
		assertEquals(availableArea.y(), bounds.y());
		assertContainedBy(bounds, availableArea);
	}

	private static void assertCenteredHorizontally(ImmutableRect2i bounds, ImmutableRect2i availableArea) {
		int leftSpace = bounds.x() - availableArea.x();
		int rightSpace = right(availableArea) - right(bounds);
		assertTrue(
			Math.abs(leftSpace - rightSpace) <= 1,
			() -> bounds + " should have balanced horizontal space inside " + availableArea
		);
		assertContainedBy(bounds, availableArea);
	}

	private static void assertCenteredVertically(ImmutableRect2i bounds, ImmutableRect2i availableArea) {
		int topSpace = bounds.y() - availableArea.y();
		int bottomSpace = bottom(availableArea) - bottom(bounds);
		assertTrue(
			Math.abs(topSpace - bottomSpace) <= 1,
			() -> bounds + " should have balanced vertical space inside " + availableArea
		);
		assertContainedBy(bounds, availableArea);
	}

	private static void assertAlignedToRight(ImmutableRect2i bounds, ImmutableRect2i availableArea) {
		assertEquals(right(availableArea), right(bounds));
		assertContainedBy(bounds, availableArea);
	}

	private static void assertAlignedToBottom(ImmutableRect2i bounds, ImmutableRect2i availableArea) {
		assertEquals(bottom(availableArea), bottom(bounds));
		assertContainedBy(bounds, availableArea);
	}

	private static void assertContainedBy(ImmutableRect2i inner, ImmutableRect2i outer) {
		assertTrue(inner.x() >= outer.x(), () -> inner + " should not start left of " + outer);
		assertTrue(inner.y() >= outer.y(), () -> inner + " should not start above " + outer);
		assertTrue(right(inner) <= right(outer), () -> inner + " should not extend right of " + outer);
		assertTrue(bottom(inner) <= bottom(outer), () -> inner + " should not extend below " + outer);
	}

	private static void assertPositiveArea(ImmutableRect2i area) {
		assertTrue(area.width() > 0, () -> area + " should have positive width");
		assertTrue(area.height() > 0, () -> area + " should have positive height");
	}

	private static int right(ImmutableRect2i area) {
		return area.x() + area.width();
	}

	private static int bottom(ImmutableRect2i area) {
		return area.y() + area.height();
	}

	private static TestGridConfig config() {
		return new TestGridConfig();
	}

	private static ImmutableRect2i largeAvailableArea() {
		return new ImmutableRect2i(
			0,
			0,
			16 * IngredientGridLayout.INGREDIENT_WIDTH,
			12 * IngredientGridLayout.INGREDIENT_HEIGHT
		);
	}

	private static class TestGridConfig implements IIngredientGridConfig {
		private int maxColumns = 9;
		private int minColumns = 1;
		private int maxRows = 6;
		private int minRows = 1;
		private boolean drawBackground = true;
		private HorizontalAlignment horizontalAlignment = HorizontalAlignment.LEFT;
		private VerticalAlignment verticalAlignment = VerticalAlignment.TOP;
		private NavigationVisibility buttonNavigationVisibility = NavigationVisibility.AUTO_HIDE;

		public TestGridConfig maxColumns(int maxColumns) {
			this.maxColumns = maxColumns;
			return this;
		}

		public TestGridConfig minColumns(int minColumns) {
			this.minColumns = minColumns;
			return this;
		}

		public TestGridConfig maxRows(int maxRows) {
			this.maxRows = maxRows;
			return this;
		}

		public TestGridConfig minRows(int minRows) {
			this.minRows = minRows;
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

		public TestGridConfig buttonNavigationVisibility(NavigationVisibility buttonNavigationVisibility) {
			this.buttonNavigationVisibility = buttonNavigationVisibility;
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
		public HorizontalAlignment getHorizontalAlignment() {
			return horizontalAlignment;
		}

		@Override
		public VerticalAlignment getVerticalAlignment() {
			return verticalAlignment;
		}

		@Override
		public NavigationVisibility getButtonNavigationVisibility() {
			return buttonNavigationVisibility;
		}
	}
}
