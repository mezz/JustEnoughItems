package mezz.jei.gui.overlay.ingredients;

import mezz.jei.common.config.IIngredientGridConfig;
import mezz.jei.common.config.IngredientGridLayoutMode;
import mezz.jei.common.config.IngredientGridNavigationMode;
import mezz.jei.api.gui.placement.HorizontalAlignment;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.common.util.NavigationVisibility;
import mezz.jei.api.gui.placement.VerticalAlignment;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class IngredientGridWithNavigationLayoutTest {
	@Test
	public void fullWidthNavigationExclusionShiftsOverlayDown() {
		ImmutableRect2i availableArea = largeAvailableArea();
		TestGridConfig gridConfig = config()
			.maxColumns(4)
			.maxRows(3)
			.drawBackground(false)
			.navigationVisibility(NavigationVisibility.ENABLED);
		IngredientGridWithNavigationLayout unobstructedLayout = IngredientGridButtonNavigationLayout.calculate(
			gridConfig, availableArea, Set.of(), null, 0
		);
		ImmutableRect2i navigationArea = unobstructedLayout.navigationArea();
		ImmutableRect2i fullWidthExclusion = new ImmutableRect2i(
			0, navigationArea.getY(), availableArea.getWidth(), navigationArea.getHeight()
		);

		IngredientGridWithNavigationLayout obstructedLayout = IngredientGridButtonNavigationLayout.calculate(
			gridConfig, availableArea, Set.of(fullWidthExclusion), null, 0
		);

		assertPositiveArea(obstructedLayout.navigationArea());
		assertFalse(obstructedLayout.navigationArea().intersects(fullWidthExclusion));
		assertTrue(
			obstructedLayout.navigationArea().getY() >= fullWidthExclusion.getY() + fullWidthExclusion.getHeight(),
			"navigation should be below the exclusion"
		);
	}

	@Test
	public void fullWidthNavigationExclusionWithBackgroundShiftsDrawnAreasDown() {
		ImmutableRect2i availableArea = largeAvailableArea();
		TestGridConfig gridConfig = config()
			.maxColumns(4)
			.maxRows(3)
			.drawBackground(true)
			.navigationVisibility(NavigationVisibility.ENABLED);
		IngredientGridWithNavigationLayout unobstructedLayout = IngredientGridButtonNavigationLayout.calculate(
			gridConfig, availableArea, Set.of(), null, 0
		);
		ImmutableRect2i navigationArea = unobstructedLayout.navigationArea();
		ImmutableRect2i fullWidthExclusion = new ImmutableRect2i(
			0, navigationArea.getY(), availableArea.getWidth(), navigationArea.getHeight()
		);

		IngredientGridWithNavigationLayout obstructedLayout = IngredientGridButtonNavigationLayout.calculate(
			gridConfig, availableArea, Set.of(fullWidthExclusion), null, 0
		);

		assertPositiveArea(obstructedLayout.navigationArea());
		assertFalse(obstructedLayout.navigationArea().intersects(fullWidthExclusion));
		assertFalse(obstructedLayout.backgroundArea().intersects(fullWidthExclusion));
		assertFalse(obstructedLayout.slotBackgroundArea().intersects(fullWidthExclusion));
		assertTrue(
			obstructedLayout.navigationArea().getY() >= fullWidthExclusion.getY() + fullWidthExclusion.getHeight(),
			"navigation should be below the exclusion"
		);
	}

	@Test
	public void navigationShiftsLeftWhenExclusionCoversRightPortion() {
		ImmutableRect2i availableArea = largeAvailableArea();
		TestGridConfig gridConfig = config()
			.maxColumns(4)
			.maxRows(3)
			.drawBackground(false)
			.navigationVisibility(NavigationVisibility.ENABLED);
		IngredientGridWithNavigationLayout unobstructedLayout = IngredientGridButtonNavigationLayout.calculate(
			gridConfig, availableArea, Set.of(), null, 0
		);
		ImmutableRect2i navigationArea = unobstructedLayout.navigationArea();
		ImmutableRect2i rightHalfExclusion = new ImmutableRect2i(
			navigationArea.getX() + navigationArea.getWidth() / 2,
			navigationArea.getY(),
			navigationArea.getWidth() / 2,
			navigationArea.getHeight()
		);

		IngredientGridWithNavigationLayout obstructedLayout = IngredientGridButtonNavigationLayout.calculate(
			gridConfig, availableArea, Set.of(rightHalfExclusion), null, 0
		);

		assertPositiveArea(obstructedLayout.navigationArea());
		assertFalse(obstructedLayout.navigationArea().intersects(rightHalfExclusion));
		assertEquals(unobstructedLayout.ingredientGridArea(), obstructedLayout.ingredientGridArea());
		assertTrue(obstructedLayout.navigationArea().getX() < navigationArea.getX() + navigationArea.getWidth() / 2);
	}

	@Test
	public void navigationShiftsRightWhenExclusionCoversLeftPortion() {
		ImmutableRect2i availableArea = largeAvailableArea();
		TestGridConfig gridConfig = config()
			.maxColumns(4)
			.maxRows(3)
			.drawBackground(false)
			.layoutMode(IngredientGridLayoutMode.MAXIMIZE_AVAILABLE_SPACE)
			.navigationVisibility(NavigationVisibility.ENABLED);
		IngredientGridWithNavigationLayout unobstructedLayout = IngredientGridButtonNavigationLayout.calculate(
			gridConfig, availableArea, Set.of(), null, 0
		);
		ImmutableRect2i navigationArea = unobstructedLayout.navigationArea();
		ImmutableRect2i leftHalfExclusion = new ImmutableRect2i(
			navigationArea.getX(),
			navigationArea.getY(),
			navigationArea.getWidth() / 2,
			navigationArea.getHeight()
		);

		IngredientGridWithNavigationLayout obstructedLayout = IngredientGridButtonNavigationLayout.calculate(
			gridConfig, availableArea, Set.of(leftHalfExclusion), null, 0
		);

		assertPositiveArea(obstructedLayout.navigationArea());
		assertFalse(obstructedLayout.navigationArea().intersects(leftHalfExclusion));
		assertEquals(unobstructedLayout.ingredientGridArea(), obstructedLayout.ingredientGridArea());
		assertTrue(obstructedLayout.navigationArea().getX() >= navigationArea.getX() + navigationArea.getWidth() / 2);
	}

	@Test
	public void rectangularLayoutWithoutBackgroundMovesBelowPartialNavigationExclusion() {
		assertRectangularLayoutMovesBelowPartialNavigationExclusion(false);
	}

	@Test
	public void rectangularLayoutWithBackgroundMovesBelowPartialNavigationExclusion() {
		assertRectangularLayoutMovesBelowPartialNavigationExclusion(true);
	}

	private static void assertRectangularLayoutMovesBelowPartialNavigationExclusion(boolean drawBackground) {
		// Setup: a left-side exclusion leaves enough horizontal room for smaller navigation controls.
		ImmutableRect2i availableArea = largeAvailableArea();
		TestGridConfig gridConfig = config()
			.maxColumns(4)
			.maxRows(3)
			.drawBackground(drawBackground)
			.layoutMode(IngredientGridLayoutMode.RECTANGULAR)
			.navigationVisibility(NavigationVisibility.ENABLED);
		IngredientGridWithNavigationLayout unobstructedLayout = IngredientGridButtonNavigationLayout.calculate(
			gridConfig, availableArea, Set.of(), null, 0
		);
		ImmutableRect2i leftHalfExclusion = unobstructedLayout.navigationArea()
			.keepLeft(unobstructedLayout.navigationArea().getWidth() / 2);

		// Operation: recalculate with the original navigation partly obstructed.
		IngredientGridWithNavigationLayout obstructedLayout = IngredientGridButtonNavigationLayout.calculate(
			gridConfig, availableArea, Set.of(leftHalfExclusion), null, 0
		);

		// Assertions: the whole layout moves down and navigation retains the grid's width and alignment.
		ImmutableRect2i expectedNavigationArea = IngredientGridWithNavigationLayout.calculateNavigationArea(
			obstructedLayout.slotBackgroundArea(),
			true
		);
		assertEquals(expectedNavigationArea, obstructedLayout.navigationArea());
		assertFalse(obstructedLayout.navigationArea().intersects(leftHalfExclusion));
		assertTrue(obstructedLayout.navigationArea().getY() >= bottom(leftHalfExclusion));
		assertTrue(obstructedLayout.ingredientGridArea().getY() > unobstructedLayout.ingredientGridArea().getY());
	}

	@Test
	public void rectangularLayoutStillAllowsGridSlotCutouts() {
		// Setup: rectangular navigation is unobstructed, but one ingredient slot is excluded.
		ImmutableRect2i availableArea = largeAvailableArea();
		TestGridConfig gridConfig = config()
			.maxColumns(4)
			.maxRows(3)
			.drawBackground(false)
			.layoutMode(IngredientGridLayoutMode.RECTANGULAR)
			.navigationVisibility(NavigationVisibility.ENABLED);
		IngredientGridWithNavigationLayout unobstructedLayout = IngredientGridButtonNavigationLayout.calculate(
			gridConfig, availableArea, Set.of(), null, 0
		);
		ImmutableRect2i gridArea = unobstructedLayout.ingredientGridArea();
		ImmutableRect2i firstSlotExclusion = new ImmutableRect2i(
			gridArea.getX() + IngredientGrid.INGREDIENT_WIDTH / 2,
			gridArea.getY() + IngredientGrid.INGREDIENT_HEIGHT / 2,
			1,
			1
		);

		// Operation: recalculate with one ingredient slot obstructed.
		IngredientGridWithNavigationLayout obstructedLayout = IngredientGridButtonNavigationLayout.calculate(
			gridConfig, availableArea, Set.of(firstSlotExclusion), null, 0
		);

		// Assertions: the rectangular bounds stay fixed and only the intersecting slot is unavailable.
		assertEquals(unobstructedLayout.ingredientGridArea(), obstructedLayout.ingredientGridArea());
		assertEquals(unobstructedLayout.navigationArea(), obstructedLayout.navigationArea());
		assertEquals(unobstructedLayout.availableSlotCount() - 1, obstructedLayout.availableSlotCount());
	}

	@Test
	public void navigationRemainsAlignedWhenNoExclusionOverlaps() {
		ImmutableRect2i availableArea = largeAvailableArea();
		TestGridConfig gridConfig = config()
			.maxColumns(4)
			.maxRows(3)
			.drawBackground(false)
			.navigationVisibility(NavigationVisibility.ENABLED);
		IngredientGridWithNavigationLayout unobstructedLayout = IngredientGridButtonNavigationLayout.calculate(
			gridConfig, availableArea, Set.of(), null, 0
		);
		ImmutableRect2i farExclusion = new ImmutableRect2i(
			availableArea.getX() + 10,
			availableArea.getY() + availableArea.getHeight() - 20,
			30,
			10
		);

		IngredientGridWithNavigationLayout obstructedLayout = IngredientGridButtonNavigationLayout.calculate(
			gridConfig, availableArea, Set.of(farExclusion), null, 0
		);

		assertEquals(unobstructedLayout.navigationArea(), obstructedLayout.navigationArea());
	}

	@Test
	public void shiftedNavigationStaysOnRectangularBackground() {
		ImmutableRect2i availableArea = largeAvailableArea();
		TestGridConfig gridConfig = config()
			.maxColumns(4)
			.maxRows(3)
			.drawBackground(true)
			.navigationVisibility(NavigationVisibility.ENABLED);
		IngredientGridWithNavigationLayout unobstructedLayout = IngredientGridButtonNavigationLayout.calculate(
			gridConfig, availableArea, Set.of(), null, 0
		);
		ImmutableRect2i navigationArea = unobstructedLayout.navigationArea();
		ImmutableRect2i rightExclusion = new ImmutableRect2i(
			navigationArea.getX() + navigationArea.getWidth() / 2,
			navigationArea.getY(),
			availableArea.getWidth(),
			navigationArea.getHeight()
		);

		IngredientGridWithNavigationLayout obstructedLayout = IngredientGridButtonNavigationLayout.calculate(
			gridConfig, availableArea, Set.of(rightExclusion), null, 0
		);

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
	public void verticalNavigationFallbackIgnoresExclusionsOutsideHorizontalStrip() {
		ImmutableRect2i baseAvailableArea = largeAvailableArea();
		ImmutableRect2i availableArea = baseAvailableArea.moveRight(baseAvailableArea.getWidth());
		TestGridConfig gridConfig = config()
			.maxColumns(4)
			.maxRows(3)
			.drawBackground(false)
			.navigationVisibility(NavigationVisibility.ENABLED);
		IngredientGridWithNavigationLayout unobstructedLayout = IngredientGridButtonNavigationLayout.calculate(
			gridConfig, availableArea, Set.of(), null, 0
		);
		ImmutableRect2i navigationArea = unobstructedLayout.navigationArea();
		ImmutableRect2i ownNavigationExclusion = new ImmutableRect2i(
			navigationArea.getX(),
			navigationArea.getY(),
			navigationArea.getWidth(),
			navigationArea.getHeight()
		);
		IngredientGridWithNavigationLayout shiftedLayout = IngredientGridButtonNavigationLayout.calculate(
			gridConfig, availableArea, Set.of(ownNavigationExclusion), null, 0
		);
		int sideGap = navigationArea.getWidth();
		int sideExclusionHeight = bottom(availableArea) - navigationArea.getY();
		ImmutableRect2i leftSideExclusion = new ImmutableRect2i(
			baseAvailableArea.getX(),
			navigationArea.getY(),
			availableArea.getX() - sideGap,
			sideExclusionHeight
		);
		ImmutableRect2i rightSideExclusion = new ImmutableRect2i(
			right(availableArea) + sideGap,
			navigationArea.getY(),
			baseAvailableArea.getWidth(),
			sideExclusionHeight
		);

		IngredientGridWithNavigationLayout withLeftSideExclusion = IngredientGridButtonNavigationLayout.calculate(
			gridConfig, availableArea, Set.of(ownNavigationExclusion, leftSideExclusion), null, 0
		);
		IngredientGridWithNavigationLayout withRightSideExclusion = IngredientGridButtonNavigationLayout.calculate(
			gridConfig, availableArea, Set.of(ownNavigationExclusion, rightSideExclusion), null, 0
		);

		assertPositiveArea(shiftedLayout.navigationArea());
		assertFalse(shiftedLayout.navigationArea().intersects(ownNavigationExclusion));
		assertEquals(shiftedLayout, withLeftSideExclusion);
		assertEquals(shiftedLayout, withRightSideExclusion);
	}

	@Test
	public void overTallNavigationExclusionLeavesNoRoomWithoutThrowing() {
		ImmutableRect2i availableArea = largeAvailableArea();
		TestGridConfig gridConfig = config()
			.maxColumns(4)
			.maxRows(3)
			.drawBackground(false)
			.navigationVisibility(NavigationVisibility.ENABLED);
		IngredientGridWithNavigationLayout unobstructedLayout = IngredientGridButtonNavigationLayout.calculate(
			gridConfig, availableArea, Set.of(), null, 0
		);
		ImmutableRect2i navigationArea = unobstructedLayout.navigationArea();
		ImmutableRect2i overTallExclusion = new ImmutableRect2i(
			0,
			navigationArea.getY(),
			availableArea.getWidth(),
			availableArea.getHeight()
		);

		IngredientGridWithNavigationLayout obstructedLayout = assertDoesNotThrow(() ->
			IngredientGridButtonNavigationLayout.calculate(gridConfig, availableArea, Set.of(overTallExclusion), null, 0)
		);

		assertEquals(ImmutableRect2i.EMPTY, obstructedLayout.ingredientGridArea());
		assertEquals(ImmutableRect2i.EMPTY, obstructedLayout.navigationArea());
		assertFalse(obstructedLayout.hasRoom());
	}

	@Test
	public void blockedNavigationAfterVerticalFallbackLeavesNoRoom() {
		ImmutableRect2i availableArea = largeAvailableArea();
		TestGridConfig gridConfig = config()
			.maxColumns(4)
			.maxRows(3)
			.drawBackground(false)
			.navigationVisibility(NavigationVisibility.ENABLED);
		IngredientGridWithNavigationLayout unobstructedLayout = IngredientGridButtonNavigationLayout.calculate(
			gridConfig, availableArea, Set.of(), null, 0
		);
		ImmutableRect2i firstNavigationArea = unobstructedLayout.navigationArea();
		ImmutableRect2i firstExclusion = new ImmutableRect2i(
			availableArea.getX(),
			firstNavigationArea.getY(),
			availableArea.getWidth(),
			firstNavigationArea.getHeight()
		);
		IngredientGridWithNavigationLayout fallbackLayout = IngredientGridButtonNavigationLayout.calculate(
			gridConfig, availableArea, Set.of(firstExclusion), null, 0
		);
		ImmutableRect2i fallbackNavigationArea = fallbackLayout.navigationArea();
		ImmutableRect2i fallbackExclusion = new ImmutableRect2i(
			availableArea.getX(),
			fallbackNavigationArea.getY(),
			availableArea.getWidth(),
			fallbackNavigationArea.getHeight()
		);

		IngredientGridWithNavigationLayout obstructedLayout = IngredientGridButtonNavigationLayout.calculate(
			gridConfig, availableArea, Set.of(firstExclusion, fallbackExclusion), null, 0
		);

		assertPositiveArea(obstructedLayout.ingredientGridArea());
		assertTrue(obstructedLayout.availableSlotCount() > 0);
		assertEquals(ImmutableRect2i.EMPTY, obstructedLayout.navigationArea());
		assertFalse(obstructedLayout.hasRoom());
	}

	@Test
	public void overTallNavigationExclusionWithBackgroundLeavesNoRoomWithoutThrowing() {
		ImmutableRect2i availableArea = largeAvailableArea();
		TestGridConfig gridConfig = config()
			.maxColumns(4)
			.maxRows(3)
			.drawBackground(true)
			.navigationVisibility(NavigationVisibility.ENABLED);
		IngredientGridWithNavigationLayout unobstructedLayout = IngredientGridButtonNavigationLayout.calculate(
			gridConfig, availableArea, Set.of(), null, 0
		);
		ImmutableRect2i navigationArea = unobstructedLayout.navigationArea();
		ImmutableRect2i overTallExclusion = new ImmutableRect2i(
			0,
			navigationArea.getY(),
			availableArea.getWidth(),
			availableArea.getHeight()
		);

		IngredientGridWithNavigationLayout obstructedLayout = assertDoesNotThrow(() ->
			IngredientGridButtonNavigationLayout.calculate(gridConfig, availableArea, Set.of(overTallExclusion), null, 0)
		);

		assertEquals(ImmutableRect2i.EMPTY, obstructedLayout.ingredientGridArea());
		assertEquals(ImmutableRect2i.EMPTY, obstructedLayout.slotBackgroundArea());
		assertEquals(ImmutableRect2i.EMPTY, obstructedLayout.navigationArea());
		assertEquals(ImmutableRect2i.EMPTY, obstructedLayout.backgroundArea());
		assertFalse(obstructedLayout.hasRoom());
	}

	@Test
	public void fullyBlockedGridSlotsLeaveNoRoom() {
		ImmutableRect2i availableArea = largeAvailableArea();
		TestGridConfig gridConfig = config()
			.maxColumns(4)
			.maxRows(3)
			.drawBackground(false)
			.navigationVisibility(NavigationVisibility.DISABLED);
		IngredientGridWithNavigationLayout unobstructedLayout = IngredientGridButtonNavigationLayout.calculate(
			gridConfig, availableArea, Set.of(), null, 0
		);
		ImmutableRect2i gridExclusion = unobstructedLayout.ingredientGridArea();

		IngredientGridWithNavigationLayout obstructedLayout = IngredientGridButtonNavigationLayout.calculate(
			gridConfig, availableArea, Set.of(gridExclusion), null, 0
		);

		assertPositiveArea(obstructedLayout.ingredientGridArea());
		assertEquals(0, obstructedLayout.availableSlotCount());
		assertFalse(obstructedLayout.hasRoom());
	}

	@Test
	public void negativeGuiExclusionOutsideAvailableAreaDoesNotMoveLayout() {
		// Setup: a GUI exclusion has negative screen coordinates but does not overlap the available grid area.
		ImmutableRect2i availableArea = largeAvailableArea();
		TestGridConfig gridConfig = config()
			.maxColumns(4)
			.maxRows(3)
			.drawBackground(false)
			.navigationVisibility(NavigationVisibility.DISABLED);
		ImmutableRect2i offscreenExclusion = new ImmutableRect2i(-20, -20, 10, 10);

		// Operation: calculate layout with and without the offscreen negative exclusion.
		IngredientGridWithNavigationLayout unobstructedLayout = IngredientGridButtonNavigationLayout.calculate(
			gridConfig,
			availableArea,
			Set.of(),
			null,
			0
		);
		IngredientGridWithNavigationLayout obstructedLayout = IngredientGridButtonNavigationLayout.calculate(
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
			.navigationVisibility(NavigationVisibility.DISABLED);
		IngredientGridWithNavigationLayout unobstructedLayout = IngredientGridButtonNavigationLayout.calculate(
			gridConfig,
			availableArea,
			Set.of(),
			null,
			0
		);
		ImmutableRect2i firstSlot = unobstructedLayout.ingredientGridArea()
			.keepTop(IngredientGrid.INGREDIENT_HEIGHT)
			.keepLeft(IngredientGrid.INGREDIENT_WIDTH);
		ImmutableRect2i negativeExclusion = new ImmutableRect2i(
			-10,
			firstSlot.y(),
			firstSlot.x() + 12,
			1
		);

		// Operation: calculate layout with the negative exclusion.
		IngredientGridWithNavigationLayout obstructedLayout = IngredientGridButtonNavigationLayout.calculate(
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
	public void tooNarrowNavigationGapShiftsOverlayDown() {
		ImmutableRect2i availableArea = largeAvailableArea();
		TestGridConfig gridConfig = config()
			.maxColumns(4)
			.maxRows(3)
			.drawBackground(false)
			.navigationVisibility(NavigationVisibility.ENABLED);
		IngredientGridWithNavigationLayout unobstructedLayout = IngredientGridButtonNavigationLayout.calculate(
			gridConfig, availableArea, Set.of(), null, 0
		);
		ImmutableRect2i navigationArea = unobstructedLayout.navigationArea();
		int tooNarrowGapWidth = navigationArea.getHeight() + 6;
		ImmutableRect2i tooNarrowGapExclusion = new ImmutableRect2i(
			navigationArea.getX() + tooNarrowGapWidth,
			navigationArea.getY(),
			availableArea.getWidth(),
			navigationArea.getHeight()
		);

		IngredientGridWithNavigationLayout obstructedLayout = IngredientGridButtonNavigationLayout.calculate(
			gridConfig, availableArea, Set.of(tooNarrowGapExclusion), null, 0
		);

		assertPositiveArea(obstructedLayout.navigationArea());
		assertFalse(obstructedLayout.navigationArea().intersects(tooNarrowGapExclusion));
		assertTrue(
			obstructedLayout.navigationArea().getY() >= tooNarrowGapExclusion.getY() + tooNarrowGapExclusion.getHeight(),
			"navigation should move below an exclusion when only a too-narrow horizontal gap remains"
		);
		assertTrue(
			obstructedLayout.navigationArea().getWidth() > tooNarrowGapWidth,
			"navigation should not shrink into the too-narrow gap"
		);
	}

	@Test
	public void backgroundPaddingCanProvideNavigationGapWhenControlsOverlap() {
		ImmutableRect2i availableArea = largeAvailableArea();
		TestGridConfig gridConfig = config()
			.maxColumns(4)
			.maxRows(3)
			.drawBackground(true)
			.navigationVisibility(NavigationVisibility.ENABLED);
		IngredientGridWithNavigationLayout unobstructedLayout = IngredientGridButtonNavigationLayout.calculate(
			gridConfig, availableArea, Set.of(), null, 0
		);
		ImmutableRect2i navigationArea = unobstructedLayout.navigationArea();
		int tooNarrowGapWidth = navigationArea.getHeight() + 6;
		ImmutableRect2i tooNarrowGapExclusion = new ImmutableRect2i(
			navigationArea.getX() + tooNarrowGapWidth,
			navigationArea.getY(),
			availableArea.getWidth(),
			navigationArea.getHeight()
		);

		IngredientGridWithNavigationLayout obstructedLayout = IngredientGridButtonNavigationLayout.calculate(
			gridConfig, availableArea, Set.of(tooNarrowGapExclusion), null, 0
		);

		assertPositiveArea(obstructedLayout.navigationArea());
		assertFalse(obstructedLayout.navigationArea().intersects(tooNarrowGapExclusion));
		assertEquals(unobstructedLayout.ingredientGridArea(), obstructedLayout.ingredientGridArea());
		assertEquals(unobstructedLayout.navigationArea().getY(), obstructedLayout.navigationArea().getY());
		assertTrue(
			obstructedLayout.navigationArea().getWidth() > tooNarrowGapWidth,
			"navigation should only move into a gap that can fit usable controls"
		);
	}

	@Test
	public void backgroundNavigationPaddingEdgeTouchDoesNotMoveLayout() {
		ImmutableRect2i availableArea = largeAvailableArea();
		TestGridConfig gridConfig = config()
			.maxColumns(4)
			.maxRows(3)
			.drawBackground(true)
			.navigationVisibility(NavigationVisibility.ENABLED);
		IngredientGridWithNavigationLayout unobstructedLayout = IngredientGridButtonNavigationLayout.calculate(
			gridConfig, availableArea, Set.of(), null, 0
		);
		int collisionPadding = unobstructedLayout.ingredientGridArea().getX() - unobstructedLayout.backgroundArea().getX();
		ImmutableRect2i paddedNavigationArea = unobstructedLayout.navigationArea().expandBy(collisionPadding);
		ImmutableRect2i touchingExclusion = new ImmutableRect2i(
			paddedNavigationArea.getX() + paddedNavigationArea.getWidth(),
			paddedNavigationArea.getY(),
			10,
			paddedNavigationArea.getHeight()
		);

		IngredientGridWithNavigationLayout obstructedLayout = IngredientGridButtonNavigationLayout.calculate(
			gridConfig, availableArea, Set.of(touchingExclusion), null, 0
		);

		assertFalse(paddedNavigationArea.intersects(touchingExclusion));
		assertEquals(unobstructedLayout.ingredientGridArea(), obstructedLayout.ingredientGridArea());
		assertEquals(unobstructedLayout.navigationArea(), obstructedLayout.navigationArea());
		assertEquals(unobstructedLayout.backgroundArea(), obstructedLayout.backgroundArea());
	}

	@Test
	public void gridFillsFullAreaWithBlockedSlotsWhenExclusionCoversGridOnly() {
		ImmutableRect2i availableArea = largeAvailableArea();
		TestGridConfig gridConfig = config()
			.maxColumns(4)
			.maxRows(3)
			.drawBackground(false)
			.navigationVisibility(NavigationVisibility.ENABLED);
		IngredientGridWithNavigationLayout unobstructedLayout = IngredientGridButtonNavigationLayout.calculate(
			gridConfig, availableArea, Set.of(), null, 0
		);
		ImmutableRect2i gridArea = unobstructedLayout.ingredientGridArea();
		ImmutableRect2i leftGridExclusion = new ImmutableRect2i(
			gridArea.getX(), gridArea.getY(), gridArea.getWidth() / 2, gridArea.getHeight()
		);

		IngredientGridWithNavigationLayout obstructedLayout = IngredientGridButtonNavigationLayout.calculate(
			gridConfig, availableArea, Set.of(leftGridExclusion), null, 0
		);

		assertEquals(unobstructedLayout.navigationArea(), obstructedLayout.navigationArea());
		assertEquals(unobstructedLayout.ingredientGridArea(), obstructedLayout.ingredientGridArea());
		assertTrue(obstructedLayout.availableSlotCount() > 0);
		assertTrue(obstructedLayout.availableSlotCount() < unobstructedLayout.availableSlotCount());
	}

	@Test
	public void gridFillsFullAreaWithBlockedSlotsWhenExclusionCoversRightGridOnly() {
		ImmutableRect2i availableArea = largeAvailableArea();
		TestGridConfig gridConfig = config()
			.maxColumns(4)
			.maxRows(3)
			.drawBackground(false)
			.navigationVisibility(NavigationVisibility.ENABLED);
		IngredientGridWithNavigationLayout unobstructedLayout = IngredientGridButtonNavigationLayout.calculate(
			gridConfig, availableArea, Set.of(), null, 0
		);
		ImmutableRect2i gridArea = unobstructedLayout.ingredientGridArea();
		ImmutableRect2i rightGridExclusion = new ImmutableRect2i(
			gridArea.getX() + gridArea.getWidth() / 2,
			gridArea.getY(),
			gridArea.getWidth() / 2,
			gridArea.getHeight()
		);

		IngredientGridWithNavigationLayout obstructedLayout = IngredientGridButtonNavigationLayout.calculate(
			gridConfig, availableArea, Set.of(rightGridExclusion), null, 0
		);

		assertEquals(unobstructedLayout.navigationArea(), obstructedLayout.navigationArea());
		assertEquals(unobstructedLayout.ingredientGridArea(), obstructedLayout.ingredientGridArea());
		assertTrue(obstructedLayout.availableSlotCount() > 0);
		assertTrue(obstructedLayout.availableSlotCount() < unobstructedLayout.availableSlotCount());
	}

	@Test
	public void scrollbarNavigationReservesRightSideForScrollbar() {
		ImmutableRect2i availableArea = largeAvailableArea();
		TestGridConfig gridConfig = config()
			.maxColumns(4)
			.maxRows(3)
			.drawBackground(false)
			.navigationMode(IngredientGridNavigationMode.SCROLLING)
			.navigationVisibility(NavigationVisibility.ENABLED);

		IngredientGridWithNavigationLayout layout = IngredientGridScrollbarLayout.calculate(
			gridConfig, availableArea, Set.of(), null, 100
		);

		assertTrue(layout.hasRoom());
		assertPositiveArea(layout.scrollbarArea());
		assertEquals(ImmutableRect2i.EMPTY, layout.navigationArea());
		assertTrue(right(layout.ingredientGridArea()) <= layout.scrollbarArea().getX());
	}

	@Test
	public void scrollbarAutoHideLeavesScrollbarEmptyWhenItemsFit() {
		ImmutableRect2i availableArea = largeAvailableArea();
		TestGridConfig gridConfig = config()
			.maxColumns(4)
			.maxRows(3)
			.drawBackground(false)
			.navigationMode(IngredientGridNavigationMode.SCROLLING)
			.navigationVisibility(NavigationVisibility.AUTO_HIDE);

		IngredientGridWithNavigationLayout layout = IngredientGridScrollbarLayout.calculate(
			gridConfig, availableArea, Set.of(), null, 4
		);

		assertTrue(layout.hasRoom());
		assertEquals(ImmutableRect2i.EMPTY, layout.scrollbarArea());
		assertEquals(ImmutableRect2i.EMPTY, layout.navigationArea());
	}

	@Test
	public void scrollbarWithBackgroundIsContainedByBackground() {
		ImmutableRect2i availableArea = largeAvailableArea();
		TestGridConfig gridConfig = config()
			.maxColumns(4)
			.maxRows(3)
			.drawBackground(true)
			.navigationMode(IngredientGridNavigationMode.SMOOTH_SCROLLING)
			.navigationVisibility(NavigationVisibility.ENABLED);

		IngredientGridWithNavigationLayout layout = IngredientGridScrollbarLayout.calculate(
			gridConfig, availableArea, Set.of(), null, 100
		);

		assertTrue(layout.hasRoom());
		assertPositiveArea(layout.scrollbarArea());
		assertContainedBy(layout.slotBackgroundArea(), layout.backgroundArea());
		assertContainedBy(layout.scrollbarArea(), layout.backgroundArea());
	}

	private static ImmutableRect2i largeAvailableArea() {
		return new ImmutableRect2i(
			0,
			0,
			16 * IngredientGrid.INGREDIENT_WIDTH,
			12 * IngredientGrid.INGREDIENT_HEIGHT
		);
	}

	private static TestGridConfig config() {
		return new TestGridConfig();
	}

	private static void assertPositiveArea(ImmutableRect2i area) {
		assertTrue(area.getWidth() > 0, "width should be positive");
		assertTrue(area.getHeight() > 0, "height should be positive");
	}

	private static void assertContainedBy(ImmutableRect2i inner, ImmutableRect2i outer) {
		assertTrue(inner.getX() >= outer.getX(), () -> inner + " should not start left of " + outer);
		assertTrue(inner.getY() >= outer.getY(), () -> inner + " should not start above " + outer);
		assertTrue(right(inner) <= right(outer), () -> inner + " should not extend right of " + outer);
		assertTrue(bottom(inner) <= bottom(outer), () -> inner + " should not extend below " + outer);
	}

	private static int right(ImmutableRect2i area) {
		return area.getX() + area.getWidth();
	}

	private static int bottom(ImmutableRect2i area) {
		return area.getY() + area.getHeight();
	}

	private static final class TestGridConfig implements IIngredientGridConfig {
		private int maxColumns = 9;
		private int minColumns = 1;
		private int maxRows = 16;
		private int minRows = 1;
		private boolean drawBackground;
		private IngredientGridLayoutMode layoutMode = IngredientGridLayoutMode.MAXIMIZE_AVAILABLE_SPACE;
		private HorizontalAlignment horizontalAlignment = HorizontalAlignment.LEFT;
		private VerticalAlignment verticalAlignment = VerticalAlignment.TOP;
		private NavigationVisibility navigationVisibility = NavigationVisibility.ENABLED;
		private IngredientGridNavigationMode navigationMode = IngredientGridNavigationMode.PAGED;

		TestGridConfig maxColumns(int maxColumns) {
			this.maxColumns = maxColumns;
			return this;
		}

		TestGridConfig minColumns(int minColumns) {
			this.minColumns = minColumns;
			return this;
		}

		TestGridConfig maxRows(int maxRows) {
			this.maxRows = maxRows;
			return this;
		}

		TestGridConfig minRows(int minRows) {
			this.minRows = minRows;
			return this;
		}

		TestGridConfig drawBackground(boolean drawBackground) {
			this.drawBackground = drawBackground;
			return this;
		}

		TestGridConfig layoutMode(IngredientGridLayoutMode layoutMode) {
			this.layoutMode = layoutMode;
			return this;
		}

		TestGridConfig navigationVisibility(NavigationVisibility navigationVisibility) {
			this.navigationVisibility = navigationVisibility;
			return this;
		}

		TestGridConfig navigationMode(IngredientGridNavigationMode navigationMode) {
			this.navigationMode = navigationMode;
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
		public IngredientGridLayoutMode getLayoutMode() {
			return layoutMode;
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

		@Override
		public IngredientGridNavigationMode getNavigationMode() {
			return navigationMode;
		}

		@Override
		public void addLayoutListener(Runnable listener) {

		}
	}
}
