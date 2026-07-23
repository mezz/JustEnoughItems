package mezz.jei.gui.overlay.ingredients;

import mezz.jei.api.gui.placement.HorizontalAlignment;
import mezz.jei.api.gui.placement.VerticalAlignment;
import mezz.jei.common.config.IIngredientGridConfig;
import mezz.jei.common.config.file.ConfigValue;
import mezz.jei.common.config.file.serializers.BooleanSerializer;
import mezz.jei.common.config.file.serializers.EnumSerializer;
import mezz.jei.common.config.file.serializers.IntegerSerializer;
import mezz.jei.common.util.ImmutablePoint2i;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.common.util.ImmutableSize2i;
import mezz.jei.common.util.NavigationVisibility;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
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
	public void ingredientGridHasNoRoomWhenAvailableAreaHasNoUsableSlots() {
		// Setup: a plain grid has enough outer area for slots, but no unblocked slots remain.
		ImmutableRect2i area = new ImmutableRect2i(
			0,
			0,
			2 * IngredientGridLayout.INGREDIENT_WIDTH,
			IngredientGridLayout.INGREDIENT_HEIGHT
		);

		// Operation: calculate room from an empty stream of usable slots.
		boolean hasRoom = IngredientGridRoom.hasRoom(area, Stream.empty());

		// Assertions: lookup-history style grids should stop displaying when no usable item slot remains.
		assertFalse(hasRoom);
	}

	@Test
	public void ingredientGridHasRoomWhenAtLeastOneSlotIsAvailable() {
		// Setup: a plain grid has enough outer area and one unblocked slot.
		ImmutableRect2i area = new ImmutableRect2i(
			0,
			0,
			2 * IngredientGridLayout.INGREDIENT_WIDTH,
			IngredientGridLayout.INGREDIENT_HEIGHT
		);
		IngredientListSlot availableSlot = new IngredientListSlot(
			0,
			0,
			IngredientGridLayout.INGREDIENT_WIDTH,
			IngredientGridLayout.INGREDIENT_HEIGHT,
			IngredientGridLayout.INGREDIENT_PADDING
		);

		// Operation: calculate room from one usable slot.
		boolean hasRoom = IngredientGridRoom.hasRoom(area, Stream.of(availableSlot));

		// Assertions: one usable slot is enough for the grid to keep displaying.
		assertTrue(hasRoom);
	}

	@Test
	public void negativeGuiExclusionOutsideAvailableAreaDoesNotMoveLayout() {
		// Setup: a GUI exclusion has negative screen coordinates but does not overlap the available grid area.
		ImmutableRect2i availableArea = largeAvailableArea();
		TestGridConfig gridConfig = config()
			.maxColumns(4)
			.maxRows(3)
			.drawBackground(false)
			.buttonNavigationVisibility(NavigationVisibility.DISABLED);
		ImmutableRect2i offscreenExclusion = new ImmutableRect2i(-20, -20, 10, 10);

		// Operation: calculate layout with and without the offscreen negative exclusion.
		IngredientGridWithNavigationLayout.Layout unobstructedLayout = IngredientGridWithNavigationLayout.calculate(
			gridConfig,
			availableArea,
			Set.of(),
			null,
			0
		);
		IngredientGridWithNavigationLayout.Layout obstructedLayout = IngredientGridWithNavigationLayout.calculate(
			gridConfig,
			availableArea,
			Set.of(offscreenExclusion),
			null,
			0
		);

		// Assertions: negative extra areas outside the screen do not move or shrink the overlay.
		assertEquals(unobstructedLayout.ingredientGridArea(), obstructedLayout.ingredientGridArea());
		assertEquals(unobstructedLayout.backgroundArea(), obstructedLayout.backgroundArea());
		assertEquals(unobstructedLayout.availableSlotCount(), obstructedLayout.availableSlotCount());
	}

	@Test
	public void negativeGuiExclusionOnlyBlocksOverlappingGridSlots() {
		// Setup: a GUI exclusion starts offscreen but reaches into the first visible ingredient slot.
		ImmutableRect2i availableArea = largeAvailableArea();
		TestGridConfig gridConfig = config()
			.maxColumns(4)
			.maxRows(3)
			.drawBackground(false)
			.buttonNavigationVisibility(NavigationVisibility.DISABLED);
		IngredientGridWithNavigationLayout.Layout unobstructedLayout = IngredientGridWithNavigationLayout.calculate(
			gridConfig,
			availableArea,
			Set.of(),
			null,
			0
		);
		ImmutableRect2i firstSlot = unobstructedLayout.ingredientGridArea()
			.keepTop(IngredientGridLayout.INGREDIENT_HEIGHT)
			.keepLeft(IngredientGridLayout.INGREDIENT_WIDTH);
		ImmutableRect2i negativeExclusion = new ImmutableRect2i(
			-10,
			firstSlot.y(),
			firstSlot.x() + 12,
			1
		);

		// Operation: calculate layout with the negative exclusion.
		IngredientGridWithNavigationLayout.Layout obstructedLayout = IngredientGridWithNavigationLayout.calculate(
			gridConfig,
			availableArea,
			Set.of(negativeExclusion),
			null,
			0
		);

		// Assertions: only the slot overlapped by the negative extra area is unavailable.
		assertTrue(negativeExclusion.x() < 0, "test exclusion should start offscreen");
		assertTrue(negativeExclusion.intersects(firstSlot), "test exclusion should overlap the first ingredient slot");
		assertEquals(unobstructedLayout.ingredientGridArea(), obstructedLayout.ingredientGridArea());
		assertEquals(unobstructedLayout.availableSlotCount() - 1, obstructedLayout.availableSlotCount());
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
			IngredientGridWithNavigationLayout.calculateFromGridArea(gridConfig, gridArea, true);
		IngredientGridWithNavigationLayout.Layout disabledLayout =
			IngredientGridWithNavigationLayout.calculateFromGridArea(gridConfig, gridArea, false);

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
			IngredientGridWithNavigationLayout.calculateFromGridArea(withoutBackgroundConfig, gridArea, false);
		IngredientGridWithNavigationLayout.Layout withBackground =
			IngredientGridWithNavigationLayout.calculateFromGridArea(withBackgroundConfig, gridArea, false);

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
			IngredientGridWithNavigationLayout.calculateFromGridArea(gridConfig, gridArea, true);

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
			withoutBackgroundConfig, availableArea
		);
		ImmutableRect2i withBackground = IngredientGridWithNavigationLayout.getAvailableGridArea(
			withBackgroundConfig, availableArea
		);

		// Assertions: background padding reduces the usable grid area while keeping it inside the unpadded area.
		assertContainedBy(withBackground, withoutBackground);
		assertTrue(withBackground.x() > withoutBackground.x(), "background padding should inset the left edge");
		assertTrue(withBackground.y() > withoutBackground.y(), "background padding should inset the top edge");
		assertTrue(withBackground.width() < withoutBackground.width(), "background padding should reduce width");
		assertTrue(withBackground.height() < withoutBackground.height(), "background padding should reduce height");
	}

	@Test
	public void exclusionBelowNavigationPaddingDoesNotMoveNavigation() {
		// Setup: an exclusion overlaps the background immediately below the visible navigation controls.
		ImmutableRect2i availableArea = largeAvailableArea();
		TestGridConfig gridConfig = config()
			.maxColumns(4)
			.maxRows(3)
			.drawBackground(true)
			.buttonNavigationVisibility(NavigationVisibility.ENABLED);
		IngredientGridWithNavigationLayout.Layout unobstructedLayout = IngredientGridWithNavigationLayout.calculate(
			gridConfig, availableArea, Set.of(), null, 0
		);
		ImmutableRect2i navigationPaddingExclusion = new ImmutableRect2i(
			unobstructedLayout.navigationArea().x(),
			unobstructedLayout.navigationArea().y() + unobstructedLayout.navigationArea().height(),
			1,
			1
		);

		// Operation: recalculate layout with the background below navigation occupied.
		IngredientGridWithNavigationLayout.Layout obstructedLayout = IngredientGridWithNavigationLayout.calculate(
			gridConfig, availableArea, Set.of(navigationPaddingExclusion), null, 0
		);

		// Assertions: the exclusion is outside the controls, so it does not move navigation.
		assertFalse(unobstructedLayout.navigationArea().intersects(navigationPaddingExclusion));
		assertTrue(unobstructedLayout.backgroundArea().intersects(navigationPaddingExclusion));
		assertEquals(unobstructedLayout.ingredientGridArea(), obstructedLayout.ingredientGridArea());
		assertEquals(unobstructedLayout.navigationArea(), obstructedLayout.navigationArea());
		assertEquals(unobstructedLayout.backgroundArea(), obstructedLayout.backgroundArea());
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

	@Test
	public void navigationShiftsLeftWhenExclusionCoversRightPortion() {
		// Setup: an exclusion covers the right half of the default navigation strip.
		ImmutableRect2i availableArea = largeAvailableArea();
		TestGridConfig gridConfig = config()
			.maxColumns(4)
			.maxRows(3)
			.drawBackground(false)
			.buttonNavigationVisibility(NavigationVisibility.ENABLED);
		IngredientGridWithNavigationLayout.Layout unobstructedLayout = IngredientGridWithNavigationLayout.calculate(
			gridConfig, availableArea, Set.of(), null, 0
		);
		ImmutableRect2i navArea = unobstructedLayout.navigationArea();
		ImmutableRect2i rightHalfExclusion = new ImmutableRect2i(
			navArea.x() + navArea.width() / 2, navArea.y(), navArea.width() / 2, navArea.height()
		);

		// Operation: recalculate layout with the right half of navigation obstructed.
		IngredientGridWithNavigationLayout.Layout obstructedLayout = IngredientGridWithNavigationLayout.calculate(
			gridConfig, availableArea, Set.of(rightHalfExclusion), null, 0
		);

		// Assertions: navigation shifts into the left gap, grid stays in place (L-shape).
		assertPositiveArea(obstructedLayout.navigationArea());
		assertFalse(obstructedLayout.navigationArea().intersects(rightHalfExclusion));
		assertEquals(unobstructedLayout.ingredientGridArea(), obstructedLayout.ingredientGridArea());
		assertTrue(obstructedLayout.navigationArea().x() < navArea.x() + navArea.width() / 2);
	}

	@Test
	public void navigationShiftsRightWhenExclusionCoversLeftPortion() {
		// Setup: an exclusion covers the left half of the default navigation strip.
		ImmutableRect2i availableArea = largeAvailableArea();
		TestGridConfig gridConfig = config()
			.maxColumns(4)
			.maxRows(3)
			.drawBackground(false)
			.buttonNavigationVisibility(NavigationVisibility.ENABLED);
		IngredientGridWithNavigationLayout.Layout unobstructedLayout = IngredientGridWithNavigationLayout.calculate(
			gridConfig, availableArea, Set.of(), null, 0
		);
		ImmutableRect2i navArea = unobstructedLayout.navigationArea();
		ImmutableRect2i leftHalfExclusion = new ImmutableRect2i(
			navArea.x(), navArea.y(), navArea.width() / 2, navArea.height()
		);

		// Operation: recalculate layout with the left half of navigation obstructed.
		IngredientGridWithNavigationLayout.Layout obstructedLayout = IngredientGridWithNavigationLayout.calculate(
			gridConfig, availableArea, Set.of(leftHalfExclusion), null, 0
		);

		// Assertions: navigation shifts into the right gap, grid stays in place (L-shape).
		assertPositiveArea(obstructedLayout.navigationArea());
		assertFalse(obstructedLayout.navigationArea().intersects(leftHalfExclusion));
		assertEquals(unobstructedLayout.ingredientGridArea(), obstructedLayout.ingredientGridArea());
		assertTrue(obstructedLayout.navigationArea().x() >= navArea.x() + navArea.width() / 2);
	}

	@Test
	public void navigationRemainsAlignedWhenNoExclusionOverlaps() {
		// Setup: an exclusion is far from the navigation strip.
		ImmutableRect2i availableArea = largeAvailableArea();
		TestGridConfig gridConfig = config()
			.maxColumns(4)
			.maxRows(3)
			.drawBackground(false)
			.buttonNavigationVisibility(NavigationVisibility.ENABLED);
		IngredientGridWithNavigationLayout.Layout unobstructedLayout = IngredientGridWithNavigationLayout.calculate(
			gridConfig, availableArea, Set.of(), null, 0
		);
		ImmutableRect2i farExclusion = new ImmutableRect2i(
			availableArea.x() + 10, availableArea.y() + availableArea.height() - 20, 30, 10
		);

		// Operation: recalculate layout with a far-away exclusion.
		IngredientGridWithNavigationLayout.Layout obstructedLayout = IngredientGridWithNavigationLayout.calculate(
			gridConfig, availableArea, Set.of(farExclusion), null, 0
		);

		// Assertions: navigation stays aligned with the grid (no L-shape needed).
		assertEquals(unobstructedLayout.navigationArea(), obstructedLayout.navigationArea());
	}

	@Test
	public void shiftedNavigationStaysOnRectangularBackground() {
		// Setup: with background drawing enabled, an exclusion in the navigation strip shifts the buttons.
		ImmutableRect2i availableArea = largeAvailableArea();
		TestGridConfig gridConfig = config()
			.maxColumns(4)
			.maxRows(3)
			.drawBackground(true)
			.buttonNavigationVisibility(NavigationVisibility.ENABLED);
		IngredientGridWithNavigationLayout.Layout unobstructedLayout = IngredientGridWithNavigationLayout.calculate(
			gridConfig, availableArea, Set.of(), null, 0
		);
		ImmutableRect2i navArea = unobstructedLayout.navigationArea();
		ImmutableRect2i rightExclusion = new ImmutableRect2i(
			navArea.x() + navArea.width() / 2, navArea.y(), availableArea.width(), navArea.height()
		);

		// Operation: recalculate layout with the right portion of navigation obstructed.
		IngredientGridWithNavigationLayout.Layout obstructedLayout = IngredientGridWithNavigationLayout.calculate(
			gridConfig, availableArea, Set.of(rightExclusion), null, 0
		);

		// Assertions: shifted navigation is still contained by the single rectangular background.
		assertPositiveArea(obstructedLayout.navigationArea());
		assertFalse(obstructedLayout.navigationArea().intersects(rightExclusion));
		assertEquals(unobstructedLayout.ingredientGridArea(), obstructedLayout.ingredientGridArea());
		assertContainedBy(obstructedLayout.navigationArea(), obstructedLayout.backgroundArea());
		assertContainedBy(obstructedLayout.slotBackgroundArea(), obstructedLayout.backgroundArea());
		assertTrue(
			obstructedLayout.backgroundArea().intersects(rightExclusion),
			"rectangular background should span the gap instead of carving out a navigation tab"
		);
		assertFalse(obstructedLayout.slotBackgroundArea().intersects(rightExclusion));
	}

	@Test
	public void alignedNavigationStaysOnRectangularBackground() {
		// Setup: no exclusion overlaps navigation, so it stays aligned with the grid.
		ImmutableRect2i availableArea = largeAvailableArea();
		TestGridConfig gridConfig = config()
			.maxColumns(4)
			.maxRows(3)
			.drawBackground(true)
			.buttonNavigationVisibility(NavigationVisibility.ENABLED);

		// Operation: calculate layout without any exclusions.
		IngredientGridWithNavigationLayout.Layout layout = IngredientGridWithNavigationLayout.calculate(
			gridConfig, availableArea, Set.of(), null, 0
		);

		// Assertions: aligned navigation is part of the main rectangular background.
		assertPositiveArea(layout.navigationArea());
		assertContainedBy(layout.navigationArea(), layout.backgroundArea());
		assertContainedBy(layout.slotBackgroundArea(), layout.backgroundArea());
	}

	@Test
	public void drawBackgroundMovesNavigationWithGridPadding() {
		// Setup: the same grid is laid out with and without background drawing.
		ImmutableRect2i availableArea = largeAvailableArea();
		TestGridConfig withoutBackgroundConfig = config()
			.maxColumns(4)
			.maxRows(3)
			.drawBackground(false)
			.buttonNavigationVisibility(NavigationVisibility.ENABLED);
		TestGridConfig withBackgroundConfig = config()
			.maxColumns(4)
			.maxRows(3)
			.drawBackground(true)
			.buttonNavigationVisibility(NavigationVisibility.ENABLED);

		// Operation: calculate both layouts.
		IngredientGridWithNavigationLayout.Layout withoutBackground = IngredientGridWithNavigationLayout.calculate(
			withoutBackgroundConfig, availableArea, Set.of(), null, 0
		);
		IngredientGridWithNavigationLayout.Layout withBackground = IngredientGridWithNavigationLayout.calculate(
			withBackgroundConfig, availableArea, Set.of(), null, 0
		);

		// Assertions: background padding moves the navigation buttons along with the inset grid.
		assertPositiveArea(withoutBackground.navigationArea());
		assertPositiveArea(withBackground.navigationArea());
		assertTrue(withBackground.navigationArea().x() > withoutBackground.navigationArea().x());
		assertTrue(withBackground.navigationArea().y() > withoutBackground.navigationArea().y());
		assertTrue(withBackground.navigationArea().width() > withoutBackground.navigationArea().width());
		assertEquals(withoutBackground.navigationArea().height(), withBackground.navigationArea().height());
		assertContainedBy(withBackground.navigationArea(), withBackground.backgroundArea());
	}

	@Test
	public void fullWidthNavigationExclusionShiftsOverlayDown() {
		// Setup: an exclusion covers the entire navigation strip width, leaving no room to shift horizontally.
		ImmutableRect2i availableArea = largeAvailableArea();
		TestGridConfig gridConfig = config()
			.maxColumns(4)
			.maxRows(3)
			.drawBackground(false)
			.buttonNavigationVisibility(NavigationVisibility.ENABLED);
		IngredientGridWithNavigationLayout.Layout unobstructedLayout = IngredientGridWithNavigationLayout.calculate(
			gridConfig, availableArea, Set.of(), null, 0
		);
		ImmutableRect2i navArea = unobstructedLayout.navigationArea();
		ImmutableRect2i fullWidthExclusion = new ImmutableRect2i(
			0, navArea.y(), availableArea.width(), navArea.height()
		);

		// Operation: recalculate layout with the entire navigation strip obstructed.
		IngredientGridWithNavigationLayout.Layout obstructedLayout = IngredientGridWithNavigationLayout.calculate(
			gridConfig, availableArea, Set.of(fullWidthExclusion), null, 0
		);

		// Assertions: overlay shifts down, navigation stays on top but below the exclusion.
		assertPositiveArea(obstructedLayout.navigationArea());
		assertFalse(obstructedLayout.navigationArea().intersects(fullWidthExclusion));
		assertTrue(
			obstructedLayout.navigationArea().y() >= fullWidthExclusion.y() + fullWidthExclusion.height(),
			"navigation should be below the exclusion"
		);
	}

	@Test
	public void fullWidthNavigationExclusionWithBackgroundShiftsDrawnAreasDown() {
		// Setup: a full-width exclusion overlaps the navigation strip and its drawn background.
		ImmutableRect2i availableArea = largeAvailableArea();
		TestGridConfig gridConfig = config()
			.maxColumns(4)
			.maxRows(3)
			.drawBackground(true)
			.buttonNavigationVisibility(NavigationVisibility.ENABLED);
		IngredientGridWithNavigationLayout.Layout unobstructedLayout = IngredientGridWithNavigationLayout.calculate(
			gridConfig, availableArea, Set.of(), null, 0
		);
		ImmutableRect2i navArea = unobstructedLayout.navigationArea();
		ImmutableRect2i fullWidthExclusion = new ImmutableRect2i(
			0, navArea.y(), availableArea.width(), navArea.height()
		);

		// Operation: recalculate layout with the original navigation strip obstructed.
		IngredientGridWithNavigationLayout.Layout obstructedLayout = IngredientGridWithNavigationLayout.calculate(
			gridConfig, availableArea, Set.of(fullWidthExclusion), null, 0
		);

		// Assertions: the navigation and drawn background move below the exclusion.
		assertPositiveArea(obstructedLayout.navigationArea());
		assertFalse(obstructedLayout.navigationArea().intersects(fullWidthExclusion));
		assertFalse(obstructedLayout.backgroundArea().intersects(fullWidthExclusion));
		assertFalse(obstructedLayout.slotBackgroundArea().intersects(fullWidthExclusion));
		assertTrue(
			obstructedLayout.navigationArea().y() >= fullWidthExclusion.y() + fullWidthExclusion.height(),
			"navigation should be below the exclusion"
		);
	}

	@Test
	public void verticalNavigationFallbackIgnoresExclusionsOutsideHorizontalStrip() {
		// Setup: the overlay is already forced downward by its own blocked navigation strip.
		ImmutableRect2i baseAvailableArea = largeAvailableArea();
		ImmutableRect2i availableArea = baseAvailableArea
			.moveRight(baseAvailableArea.width());
		TestGridConfig gridConfig = config()
			.maxColumns(4)
			.maxRows(3)
			.drawBackground(false)
			.buttonNavigationVisibility(NavigationVisibility.ENABLED);
		IngredientGridWithNavigationLayout.Layout unobstructedLayout = IngredientGridWithNavigationLayout.calculate(
			gridConfig, availableArea, Set.of(), null, 0
		);
		ImmutableRect2i navArea = unobstructedLayout.navigationArea();
		ImmutableRect2i ownNavigationExclusion = new ImmutableRect2i(
			navArea.x(), navArea.y(), navArea.width(), navArea.height()
		);
		IngredientGridWithNavigationLayout.Layout shiftedLayout = IngredientGridWithNavigationLayout.calculate(
			gridConfig, availableArea, Set.of(ownNavigationExclusion), null, 0
		);
		int sideGap = navArea.width();
		int sideExclusionHeight = bottom(availableArea) - navArea.y();
		ImmutableRect2i leftSideExclusion = new ImmutableRect2i(
			baseAvailableArea.x(),
			navArea.y(),
			availableArea.x() - sideGap,
			sideExclusionHeight
		);
		ImmutableRect2i rightSideExclusion = new ImmutableRect2i(
			right(availableArea) + sideGap,
			navArea.y(),
			baseAvailableArea.width(),
			sideExclusionHeight
		);

		// Operation: recalculate with tall exclusions outside this overlay's navigation strip.
		IngredientGridWithNavigationLayout.Layout withLeftSideExclusion = IngredientGridWithNavigationLayout.calculate(
			gridConfig, availableArea, Set.of(ownNavigationExclusion, leftSideExclusion), null, 0
		);
		IngredientGridWithNavigationLayout.Layout withRightSideExclusion = IngredientGridWithNavigationLayout.calculate(
			gridConfig, availableArea, Set.of(ownNavigationExclusion, rightSideExclusion), null, 0
		);

		// Assertions: side exclusions do not further squish an overlay that already needed vertical fallback.
		assertPositiveArea(shiftedLayout.navigationArea());
		assertFalse(shiftedLayout.navigationArea().intersects(ownNavigationExclusion));
		assertEquals(shiftedLayout, withLeftSideExclusion);
		assertEquals(shiftedLayout, withRightSideExclusion);
	}

	@Test
	public void overTallNavigationExclusionLeavesNoRoomWithoutThrowing() {
		// Setup: an exclusion covers the navigation strip and extends beyond the available overlay area.
		ImmutableRect2i availableArea = largeAvailableArea();
		TestGridConfig gridConfig = config()
			.maxColumns(4)
			.maxRows(3)
			.drawBackground(false)
			.buttonNavigationVisibility(NavigationVisibility.ENABLED);
		IngredientGridWithNavigationLayout.Layout unobstructedLayout = IngredientGridWithNavigationLayout.calculate(
			gridConfig, availableArea, Set.of(), null, 0
		);
		ImmutableRect2i navArea = unobstructedLayout.navigationArea();
		ImmutableRect2i overTallExclusion = new ImmutableRect2i(
			0, navArea.y(), availableArea.width(), availableArea.height()
		);

		// Operation: recalculate layout with no vertical space left after avoiding navigation.
		IngredientGridWithNavigationLayout.Layout obstructedLayout = assertDoesNotThrow(() ->
			IngredientGridWithNavigationLayout.calculate(gridConfig, availableArea, Set.of(overTallExclusion), null, 0)
		);

		// Assertions: no valid overlay remains, but layout calculation handles it without invalid rectangles.
		assertEquals(ImmutableRect2i.EMPTY, obstructedLayout.ingredientGridArea());
		assertEquals(ImmutableRect2i.EMPTY, obstructedLayout.navigationArea());
		assertFalse(obstructedLayout.hasRoom());
	}

	@Test
	public void blockedNavigationAfterVerticalFallbackLeavesNoRoom() {
		// Setup: the first exclusion forces navigation downward, and a second exclusion blocks that fallback.
		ImmutableRect2i availableArea = largeAvailableArea();
		TestGridConfig gridConfig = config()
			.maxColumns(4)
			.maxRows(3)
			.drawBackground(false)
			.buttonNavigationVisibility(NavigationVisibility.ENABLED);
		IngredientGridWithNavigationLayout.Layout unobstructedLayout = IngredientGridWithNavigationLayout.calculate(
			gridConfig, availableArea, Set.of(), null, 0
		);
		ImmutableRect2i firstNavigationArea = unobstructedLayout.navigationArea();
		ImmutableRect2i firstExclusion = new ImmutableRect2i(
			availableArea.x(), firstNavigationArea.y(), availableArea.width(), firstNavigationArea.height()
		);
		IngredientGridWithNavigationLayout.Layout fallbackLayout = IngredientGridWithNavigationLayout.calculate(
			gridConfig, availableArea, Set.of(firstExclusion), null, 0
		);
		ImmutableRect2i fallbackNavigationArea = fallbackLayout.navigationArea();
		ImmutableRect2i fallbackExclusion = new ImmutableRect2i(
			availableArea.x(), fallbackNavigationArea.y(), availableArea.width(), fallbackNavigationArea.height()
		);

		// Operation: recalculate layout with no available navigation position.
		IngredientGridWithNavigationLayout.Layout obstructedLayout = IngredientGridWithNavigationLayout.calculate(
			gridConfig, availableArea, Set.of(firstExclusion, fallbackExclusion), null, 0
		);

		// Assertions: item slots would fit, but the overlay is unusable because required navigation cannot draw.
		assertPositiveArea(obstructedLayout.ingredientGridArea());
		assertTrue(obstructedLayout.availableSlotCount() > 0);
		assertEquals(ImmutableRect2i.EMPTY, obstructedLayout.navigationArea());
		assertFalse(obstructedLayout.hasRoom());
	}

	@Test
	public void overTallNavigationExclusionWithBackgroundLeavesNoRoomWithoutThrowing() {
		// Setup: an exclusion covers the drawn navigation strip and extends beyond the available overlay area.
		ImmutableRect2i availableArea = largeAvailableArea();
		TestGridConfig gridConfig = config()
			.maxColumns(4)
			.maxRows(3)
			.drawBackground(true)
			.buttonNavigationVisibility(NavigationVisibility.ENABLED);
		IngredientGridWithNavigationLayout.Layout unobstructedLayout = IngredientGridWithNavigationLayout.calculate(
			gridConfig, availableArea, Set.of(), null, 0
		);
		ImmutableRect2i navArea = unobstructedLayout.navigationArea();
		ImmutableRect2i overTallExclusion = new ImmutableRect2i(
			0, navArea.y(), availableArea.width(), availableArea.height()
		);

		// Operation: recalculate layout with no vertical space left after avoiding navigation and its padding.
		IngredientGridWithNavigationLayout.Layout obstructedLayout = assertDoesNotThrow(() ->
			IngredientGridWithNavigationLayout.calculate(gridConfig, availableArea, Set.of(overTallExclusion), null, 0)
		);

		// Assertions: no valid drawn area remains, but layout calculation handles it without invalid rectangles.
		assertEquals(ImmutableRect2i.EMPTY, obstructedLayout.ingredientGridArea());
		assertEquals(ImmutableRect2i.EMPTY, obstructedLayout.slotBackgroundArea());
		assertEquals(ImmutableRect2i.EMPTY, obstructedLayout.navigationArea());
		assertEquals(ImmutableRect2i.EMPTY, obstructedLayout.backgroundArea());
		assertFalse(obstructedLayout.hasRoom());
	}

	@Test
	public void fullyBlockedGridSlotsLeaveNoRoom() {
		// Setup: navigation is disabled, but a GUI exclusion covers every item slot.
		ImmutableRect2i availableArea = largeAvailableArea();
		TestGridConfig gridConfig = config()
			.maxColumns(4)
			.maxRows(3)
			.drawBackground(false)
			.buttonNavigationVisibility(NavigationVisibility.DISABLED);
		IngredientGridWithNavigationLayout.Layout unobstructedLayout = IngredientGridWithNavigationLayout.calculate(
			gridConfig, availableArea, Set.of(), null, 0
		);
		ImmutableRect2i gridExclusion = unobstructedLayout.ingredientGridArea();

		// Operation: recalculate layout with the whole grid covered by an exclusion.
		IngredientGridWithNavigationLayout.Layout obstructedLayout = IngredientGridWithNavigationLayout.calculate(
			gridConfig, availableArea, Set.of(gridExclusion), null, 0
		);

		// Assertions: the grid bounds still exist, but no item slot is drawable.
		assertPositiveArea(obstructedLayout.ingredientGridArea());
		assertEquals(0, obstructedLayout.availableSlotCount());
		assertFalse(obstructedLayout.hasRoom());
	}

	@Test
	public void tooNarrowNavigationGapShiftsOverlayDown() {
		// Setup: the exclusion leaves a horizontal navigation gap that cannot fit the minimum navigation area.
		ImmutableRect2i availableArea = largeAvailableArea();
		TestGridConfig gridConfig = config()
			.maxColumns(4)
			.maxRows(3)
			.drawBackground(false)
			.buttonNavigationVisibility(NavigationVisibility.ENABLED);
		IngredientGridWithNavigationLayout.Layout unobstructedLayout = IngredientGridWithNavigationLayout.calculate(
			gridConfig, availableArea, Set.of(), null, 0
		);
		ImmutableRect2i navArea = unobstructedLayout.navigationArea();
		int tooNarrowGapWidth = navArea.height() + 6;
		ImmutableRect2i tooNarrowGapExclusion = new ImmutableRect2i(
			navArea.x() + tooNarrowGapWidth,
			navArea.y(),
			availableArea.width(),
			navArea.height()
		);

		// Operation: recalculate layout with only the too-narrow gap available at the original navigation Y.
		IngredientGridWithNavigationLayout.Layout obstructedLayout = IngredientGridWithNavigationLayout.calculate(
			gridConfig, availableArea, Set.of(tooNarrowGapExclusion), null, 0
		);

		// Assertions: navigation falls back below the exclusion instead of shrinking the buttons.
		assertPositiveArea(obstructedLayout.navigationArea());
		assertFalse(obstructedLayout.navigationArea().intersects(tooNarrowGapExclusion));
		assertTrue(
			obstructedLayout.navigationArea().y() >= tooNarrowGapExclusion.y() + tooNarrowGapExclusion.height(),
			"navigation should move below an exclusion when only a too-narrow horizontal gap remains"
		);
		assertTrue(
			obstructedLayout.navigationArea().width() > tooNarrowGapWidth,
			"navigation should not shrink into the too-narrow gap"
		);
	}

	@Test
	public void backgroundPaddingCanProvideNavigationGapWhenControlsOverlap() {
		// Setup: an exclusion overlaps the right side of navigation, but the background area leaves enough room on the left.
		ImmutableRect2i availableArea = largeAvailableArea();
		TestGridConfig gridConfig = config()
			.maxColumns(4)
			.maxRows(3)
			.drawBackground(true)
			.buttonNavigationVisibility(NavigationVisibility.ENABLED);
		IngredientGridWithNavigationLayout.Layout unobstructedLayout = IngredientGridWithNavigationLayout.calculate(
			gridConfig, availableArea, Set.of(), null, 0
		);
		ImmutableRect2i navArea = unobstructedLayout.navigationArea();
		int tooNarrowGapWidth = navArea.height() + 6;
		ImmutableRect2i tooNarrowGapExclusion = new ImmutableRect2i(
			navArea.x() + tooNarrowGapWidth,
			navArea.y(),
			availableArea.width(),
			navArea.height()
		);

		// Operation: recalculate layout with only the background-expanded gap available at the original Y.
		IngredientGridWithNavigationLayout.Layout obstructedLayout = IngredientGridWithNavigationLayout.calculate(
			gridConfig, availableArea, Set.of(tooNarrowGapExclusion), null, 0
		);

		// Assertions: navigation moves horizontally because its controls overlap the exclusion.
		assertPositiveArea(obstructedLayout.navigationArea());
		assertFalse(obstructedLayout.navigationArea().intersects(tooNarrowGapExclusion));
		assertEquals(unobstructedLayout.ingredientGridArea(), obstructedLayout.ingredientGridArea());
		assertEquals(unobstructedLayout.navigationArea().y(), obstructedLayout.navigationArea().y());
		assertTrue(
			obstructedLayout.navigationArea().width() > tooNarrowGapWidth,
			"navigation should only move into a gap that can fit usable controls"
		);
	}

	@Test
	public void backgroundNavigationPaddingEdgeTouchDoesNotMoveLayout() {
		// Setup: an exclusion touches, but does not overlap, the padded navigation bounds.
		ImmutableRect2i availableArea = largeAvailableArea();
		TestGridConfig gridConfig = config()
			.maxColumns(4)
			.maxRows(3)
			.drawBackground(true)
			.buttonNavigationVisibility(NavigationVisibility.ENABLED);
		IngredientGridWithNavigationLayout.Layout unobstructedLayout = IngredientGridWithNavigationLayout.calculate(
			gridConfig, availableArea, Set.of(), null, 0
		);
		int collisionPadding = unobstructedLayout.ingredientGridArea().x() - unobstructedLayout.backgroundArea().x();
		ImmutableRect2i paddedNavigationArea = unobstructedLayout.navigationArea().expandBy(collisionPadding);
		ImmutableRect2i touchingExclusion = new ImmutableRect2i(
			paddedNavigationArea.x() + paddedNavigationArea.width(),
			paddedNavigationArea.y(),
			10,
			paddedNavigationArea.height()
		);

		// Operation: recalculate layout with the edge-touching exclusion.
		IngredientGridWithNavigationLayout.Layout obstructedLayout = IngredientGridWithNavigationLayout.calculate(
			gridConfig, availableArea, Set.of(touchingExclusion), null, 0
		);

		// Assertions: touching edges are not intersections, so the layout should remain unchanged.
		assertFalse(paddedNavigationArea.intersects(touchingExclusion));
		assertEquals(unobstructedLayout.ingredientGridArea(), obstructedLayout.ingredientGridArea());
		assertEquals(unobstructedLayout.navigationArea(), obstructedLayout.navigationArea());
		assertEquals(unobstructedLayout.backgroundArea(), obstructedLayout.backgroundArea());
	}

	@Test
	public void gridFillsFullAreaWithBlockedSlotsWhenExclusionCoversGridOnly() {
		// Setup: an exclusion covers the left half of the grid area but not the navigation.
		ImmutableRect2i availableArea = largeAvailableArea();
		TestGridConfig gridConfig = config()
			.maxColumns(4)
			.maxRows(3)
			.drawBackground(false)
			.buttonNavigationVisibility(NavigationVisibility.ENABLED);
		IngredientGridWithNavigationLayout.Layout unobstructedLayout = IngredientGridWithNavigationLayout.calculate(
			gridConfig, availableArea, Set.of(), null, 0
		);
		ImmutableRect2i gridArea = unobstructedLayout.ingredientGridArea();
		ImmutableRect2i leftGridExclusion = new ImmutableRect2i(
			gridArea.x(), gridArea.y(), gridArea.width() / 2, gridArea.height()
		);

		// Operation: recalculate layout with the left half of the grid obstructed.
		IngredientGridWithNavigationLayout.Layout obstructedLayout = IngredientGridWithNavigationLayout.calculate(
			gridConfig, availableArea, Set.of(leftGridExclusion), null, 0
		);

		// Assertions: grid fills the full area (slots blocked individually), navigation stays in place.
		assertEquals(unobstructedLayout.navigationArea(), obstructedLayout.navigationArea());
		assertEquals(unobstructedLayout.ingredientGridArea(), obstructedLayout.ingredientGridArea());
		assertTrue(obstructedLayout.availableSlotCount() > 0);
		assertTrue(obstructedLayout.availableSlotCount() < unobstructedLayout.availableSlotCount());
	}

	@Test
	public void gridFillsFullAreaWithBlockedSlotsWhenExclusionCoversRightGridOnly() {
		// Setup: an exclusion covers the right half of the grid area but not the navigation.
		ImmutableRect2i availableArea = largeAvailableArea();
		TestGridConfig gridConfig = config()
			.maxColumns(4)
			.maxRows(3)
			.drawBackground(false)
			.buttonNavigationVisibility(NavigationVisibility.ENABLED);
		IngredientGridWithNavigationLayout.Layout unobstructedLayout = IngredientGridWithNavigationLayout.calculate(
			gridConfig, availableArea, Set.of(), null, 0
		);
		ImmutableRect2i gridArea = unobstructedLayout.ingredientGridArea();
		ImmutableRect2i rightGridExclusion = new ImmutableRect2i(
			gridArea.x() + gridArea.width() / 2, gridArea.y(), gridArea.width() / 2, gridArea.height()
		);

		// Operation: recalculate layout with the right half of the grid obstructed.
		IngredientGridWithNavigationLayout.Layout obstructedLayout = IngredientGridWithNavigationLayout.calculate(
			gridConfig, availableArea, Set.of(rightGridExclusion), null, 0
		);

		// Assertions: grid fills the full area (slots blocked individually), navigation stays in place.
		assertEquals(unobstructedLayout.navigationArea(), obstructedLayout.navigationArea());
		assertEquals(unobstructedLayout.ingredientGridArea(), obstructedLayout.ingredientGridArea());
		assertTrue(obstructedLayout.availableSlotCount() > 0);
		assertTrue(obstructedLayout.availableSlotCount() < unobstructedLayout.availableSlotCount());
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
		private final ConfigValue<Integer> maxColumns = integerValue("maxColumns", 9);
		private final ConfigValue<Integer> maxRows = integerValue("maxRows", 6);
		private final ConfigValue<Boolean> drawBackground = booleanValue("drawBackground", true);
		private final ConfigValue<HorizontalAlignment> horizontalAlignment = enumValue("horizontalAlignment", HorizontalAlignment.LEFT, HorizontalAlignment.class);
		private final ConfigValue<VerticalAlignment> verticalAlignment = enumValue("verticalAlignment", VerticalAlignment.TOP, VerticalAlignment.class);
		private final ConfigValue<NavigationVisibility> buttonNavigationVisibility = enumValue("buttonNavigationVisibility", NavigationVisibility.AUTO_HIDE, NavigationVisibility.class);
		private int minColumns = 1;
		private int minRows = 1;

		public TestGridConfig maxColumns(int maxColumns) {
			this.maxColumns.set(maxColumns);
			return this;
		}

		public TestGridConfig minColumns(int minColumns) {
			this.minColumns = minColumns;
			return this;
		}

		public TestGridConfig maxRows(int maxRows) {
			this.maxRows.set(maxRows);
			return this;
		}

		public TestGridConfig minRows(int minRows) {
			this.minRows = minRows;
			return this;
		}

		public TestGridConfig drawBackground(boolean drawBackground) {
			this.drawBackground.set(drawBackground);
			return this;
		}

		public TestGridConfig horizontalAlignment(HorizontalAlignment horizontalAlignment) {
			this.horizontalAlignment.set(horizontalAlignment);
			return this;
		}

		public TestGridConfig verticalAlignment(VerticalAlignment verticalAlignment) {
			this.verticalAlignment.set(verticalAlignment);
			return this;
		}

		public TestGridConfig buttonNavigationVisibility(NavigationVisibility buttonNavigationVisibility) {
			this.buttonNavigationVisibility.set(buttonNavigationVisibility);
			return this;
		}

		@Override
		public ConfigValue<Integer> maxColumns() {
			return maxColumns;
		}

		@Override
		public int getMinColumns() {
			return minColumns;
		}

		@Override
		public ConfigValue<Integer> maxRows() {
			return maxRows;
		}

		@Override
		public int getMinRows() {
			return minRows;
		}

		@Override
		public ConfigValue<Boolean> drawBackground() {
			return drawBackground;
		}

		@Override
		public ConfigValue<HorizontalAlignment> horizontalAlignment() {
			return horizontalAlignment;
		}

		@Override
		public ConfigValue<VerticalAlignment> verticalAlignment() {
			return verticalAlignment;
		}

		@Override
		public ConfigValue<NavigationVisibility> buttonNavigationVisibility() {
			return buttonNavigationVisibility;
		}

		private static ConfigValue<Integer> integerValue(String name, int defaultValue) {
			return new ConfigValue<>(
				"jei.config.test",
				name,
				defaultValue,
				new IntegerSerializer(Integer.MIN_VALUE, Integer.MAX_VALUE)
			);
		}

		private static ConfigValue<Boolean> booleanValue(String name, boolean defaultValue) {
			return new ConfigValue<>(
				"jei.config.test",
				name,
				defaultValue,
				BooleanSerializer.INSTANCE
			);
		}

		private static <T extends Enum<T>> ConfigValue<T> enumValue(String name, T defaultValue, Class<T> enumClass) {
			return new ConfigValue<>(
				"jei.config.test",
				name,
				defaultValue,
				new EnumSerializer<>(enumClass)
			);
		}
	}
}
