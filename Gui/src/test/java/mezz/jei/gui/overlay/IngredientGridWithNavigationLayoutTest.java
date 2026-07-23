package mezz.jei.gui.overlay;

import mezz.jei.common.config.IIngredientGridConfig;
import mezz.jei.common.util.HorizontalAlignment;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.common.util.NavigationVisibility;
import mezz.jei.common.util.VerticalAlignment;
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
			.buttonNavigationVisibility(NavigationVisibility.ENABLED);
		IngredientGridWithNavigationLayout.Layout unobstructedLayout = IngredientGridWithNavigationLayout.calculate(
			gridConfig, availableArea, Set.of(), null, 0
		);
		ImmutableRect2i navigationArea = unobstructedLayout.navigationArea();
		ImmutableRect2i fullWidthExclusion = new ImmutableRect2i(
			0, navigationArea.y(), availableArea.width(), navigationArea.height()
		);

		IngredientGridWithNavigationLayout.Layout obstructedLayout = IngredientGridWithNavigationLayout.calculate(
			gridConfig, availableArea, Set.of(fullWidthExclusion), null, 0
		);

		assertPositiveArea(obstructedLayout.navigationArea());
		assertFalse(obstructedLayout.navigationArea().intersects(fullWidthExclusion));
		assertTrue(
			obstructedLayout.navigationArea().y() >= fullWidthExclusion.y() + fullWidthExclusion.height(),
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
			.buttonNavigationVisibility(NavigationVisibility.ENABLED);
		IngredientGridWithNavigationLayout.Layout unobstructedLayout = IngredientGridWithNavigationLayout.calculate(
			gridConfig, availableArea, Set.of(), null, 0
		);
		ImmutableRect2i navigationArea = unobstructedLayout.navigationArea();
		ImmutableRect2i fullWidthExclusion = new ImmutableRect2i(
			0, navigationArea.y(), availableArea.width(), navigationArea.height()
		);

		IngredientGridWithNavigationLayout.Layout obstructedLayout = IngredientGridWithNavigationLayout.calculate(
			gridConfig, availableArea, Set.of(fullWidthExclusion), null, 0
		);

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
	public void navigationShiftsLeftWhenExclusionCoversRightPortion() {
		ImmutableRect2i availableArea = largeAvailableArea();
		TestGridConfig gridConfig = config()
			.maxColumns(4)
			.maxRows(3)
			.drawBackground(false)
			.buttonNavigationVisibility(NavigationVisibility.ENABLED);
		IngredientGridWithNavigationLayout.Layout unobstructedLayout = IngredientGridWithNavigationLayout.calculate(
			gridConfig, availableArea, Set.of(), null, 0
		);
		ImmutableRect2i navigationArea = unobstructedLayout.navigationArea();
		ImmutableRect2i rightHalfExclusion = new ImmutableRect2i(
			navigationArea.x() + navigationArea.width() / 2,
			navigationArea.y(),
			navigationArea.width() / 2,
			navigationArea.height()
		);

		IngredientGridWithNavigationLayout.Layout obstructedLayout = IngredientGridWithNavigationLayout.calculate(
			gridConfig, availableArea, Set.of(rightHalfExclusion), null, 0
		);

		assertPositiveArea(obstructedLayout.navigationArea());
		assertFalse(obstructedLayout.navigationArea().intersects(rightHalfExclusion));
		assertEquals(unobstructedLayout.ingredientGridArea(), obstructedLayout.ingredientGridArea());
		assertTrue(obstructedLayout.navigationArea().x() < navigationArea.x() + navigationArea.width() / 2);
	}

	@Test
	public void navigationShiftsRightWhenExclusionCoversLeftPortion() {
		ImmutableRect2i availableArea = largeAvailableArea();
		TestGridConfig gridConfig = config()
			.maxColumns(4)
			.maxRows(3)
			.drawBackground(false)
			.buttonNavigationVisibility(NavigationVisibility.ENABLED);
		IngredientGridWithNavigationLayout.Layout unobstructedLayout = IngredientGridWithNavigationLayout.calculate(
			gridConfig, availableArea, Set.of(), null, 0
		);
		ImmutableRect2i navigationArea = unobstructedLayout.navigationArea();
		ImmutableRect2i leftHalfExclusion = new ImmutableRect2i(
			navigationArea.x(),
			navigationArea.y(),
			navigationArea.width() / 2,
			navigationArea.height()
		);

		IngredientGridWithNavigationLayout.Layout obstructedLayout = IngredientGridWithNavigationLayout.calculate(
			gridConfig, availableArea, Set.of(leftHalfExclusion), null, 0
		);

		assertPositiveArea(obstructedLayout.navigationArea());
		assertFalse(obstructedLayout.navigationArea().intersects(leftHalfExclusion));
		assertEquals(unobstructedLayout.ingredientGridArea(), obstructedLayout.ingredientGridArea());
		assertTrue(obstructedLayout.navigationArea().x() >= navigationArea.x() + navigationArea.width() / 2);
	}

	@Test
	public void navigationRemainsAlignedWhenNoExclusionOverlaps() {
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
			availableArea.x() + 10,
			availableArea.y() + availableArea.height() - 20,
			30,
			10
		);

		IngredientGridWithNavigationLayout.Layout obstructedLayout = IngredientGridWithNavigationLayout.calculate(
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
			.buttonNavigationVisibility(NavigationVisibility.ENABLED);
		IngredientGridWithNavigationLayout.Layout unobstructedLayout = IngredientGridWithNavigationLayout.calculate(
			gridConfig, availableArea, Set.of(), null, 0
		);
		ImmutableRect2i navigationArea = unobstructedLayout.navigationArea();
		ImmutableRect2i rightExclusion = new ImmutableRect2i(
			navigationArea.x() + navigationArea.width() / 2,
			navigationArea.y(),
			availableArea.width(),
			navigationArea.height()
		);

		IngredientGridWithNavigationLayout.Layout obstructedLayout = IngredientGridWithNavigationLayout.calculate(
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
		ImmutableRect2i availableArea = baseAvailableArea.moveRight(baseAvailableArea.width());
		TestGridConfig gridConfig = config()
			.maxColumns(4)
			.maxRows(3)
			.drawBackground(false)
			.buttonNavigationVisibility(NavigationVisibility.ENABLED);
		IngredientGridWithNavigationLayout.Layout unobstructedLayout = IngredientGridWithNavigationLayout.calculate(
			gridConfig, availableArea, Set.of(), null, 0
		);
		ImmutableRect2i navigationArea = unobstructedLayout.navigationArea();
		ImmutableRect2i ownNavigationExclusion = new ImmutableRect2i(
			navigationArea.x(),
			navigationArea.y(),
			navigationArea.width(),
			navigationArea.height()
		);
		IngredientGridWithNavigationLayout.Layout shiftedLayout = IngredientGridWithNavigationLayout.calculate(
			gridConfig, availableArea, Set.of(ownNavigationExclusion), null, 0
		);
		int sideGap = navigationArea.width();
		int sideExclusionHeight = bottom(availableArea) - navigationArea.y();
		ImmutableRect2i leftSideExclusion = new ImmutableRect2i(
			baseAvailableArea.x(),
			navigationArea.y(),
			availableArea.x() - sideGap,
			sideExclusionHeight
		);
		ImmutableRect2i rightSideExclusion = new ImmutableRect2i(
			right(availableArea) + sideGap,
			navigationArea.y(),
			baseAvailableArea.width(),
			sideExclusionHeight
		);

		IngredientGridWithNavigationLayout.Layout withLeftSideExclusion = IngredientGridWithNavigationLayout.calculate(
			gridConfig, availableArea, Set.of(ownNavigationExclusion, leftSideExclusion), null, 0
		);
		IngredientGridWithNavigationLayout.Layout withRightSideExclusion = IngredientGridWithNavigationLayout.calculate(
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
			.buttonNavigationVisibility(NavigationVisibility.ENABLED);
		IngredientGridWithNavigationLayout.Layout unobstructedLayout = IngredientGridWithNavigationLayout.calculate(
			gridConfig, availableArea, Set.of(), null, 0
		);
		ImmutableRect2i navigationArea = unobstructedLayout.navigationArea();
		ImmutableRect2i overTallExclusion = new ImmutableRect2i(
			0,
			navigationArea.y(),
			availableArea.width(),
			availableArea.height()
		);

		IngredientGridWithNavigationLayout.Layout obstructedLayout = assertDoesNotThrow(() ->
			IngredientGridWithNavigationLayout.calculate(gridConfig, availableArea, Set.of(overTallExclusion), null, 0)
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
			.buttonNavigationVisibility(NavigationVisibility.ENABLED);
		IngredientGridWithNavigationLayout.Layout unobstructedLayout = IngredientGridWithNavigationLayout.calculate(
			gridConfig, availableArea, Set.of(), null, 0
		);
		ImmutableRect2i firstNavigationArea = unobstructedLayout.navigationArea();
		ImmutableRect2i firstExclusion = new ImmutableRect2i(
			availableArea.x(),
			firstNavigationArea.y(),
			availableArea.width(),
			firstNavigationArea.height()
		);
		IngredientGridWithNavigationLayout.Layout fallbackLayout = IngredientGridWithNavigationLayout.calculate(
			gridConfig, availableArea, Set.of(firstExclusion), null, 0
		);
		ImmutableRect2i fallbackNavigationArea = fallbackLayout.navigationArea();
		ImmutableRect2i fallbackExclusion = new ImmutableRect2i(
			availableArea.x(),
			fallbackNavigationArea.y(),
			availableArea.width(),
			fallbackNavigationArea.height()
		);

		IngredientGridWithNavigationLayout.Layout obstructedLayout = IngredientGridWithNavigationLayout.calculate(
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
			.buttonNavigationVisibility(NavigationVisibility.ENABLED);
		IngredientGridWithNavigationLayout.Layout unobstructedLayout = IngredientGridWithNavigationLayout.calculate(
			gridConfig, availableArea, Set.of(), null, 0
		);
		ImmutableRect2i navigationArea = unobstructedLayout.navigationArea();
		ImmutableRect2i overTallExclusion = new ImmutableRect2i(
			0,
			navigationArea.y(),
			availableArea.width(),
			availableArea.height()
		);

		IngredientGridWithNavigationLayout.Layout obstructedLayout = assertDoesNotThrow(() ->
			IngredientGridWithNavigationLayout.calculate(gridConfig, availableArea, Set.of(overTallExclusion), null, 0)
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
			.buttonNavigationVisibility(NavigationVisibility.DISABLED);
		IngredientGridWithNavigationLayout.Layout unobstructedLayout = IngredientGridWithNavigationLayout.calculate(
			gridConfig, availableArea, Set.of(), null, 0
		);
		ImmutableRect2i gridExclusion = unobstructedLayout.ingredientGridArea();

		IngredientGridWithNavigationLayout.Layout obstructedLayout = IngredientGridWithNavigationLayout.calculate(
			gridConfig, availableArea, Set.of(gridExclusion), null, 0
		);

		assertPositiveArea(obstructedLayout.ingredientGridArea());
		assertEquals(0, obstructedLayout.availableSlotCount());
		assertFalse(obstructedLayout.hasRoom());
	}

	@Test
	public void tooNarrowNavigationGapShiftsOverlayDown() {
		ImmutableRect2i availableArea = largeAvailableArea();
		TestGridConfig gridConfig = config()
			.maxColumns(4)
			.maxRows(3)
			.drawBackground(false)
			.buttonNavigationVisibility(NavigationVisibility.ENABLED);
		IngredientGridWithNavigationLayout.Layout unobstructedLayout = IngredientGridWithNavigationLayout.calculate(
			gridConfig, availableArea, Set.of(), null, 0
		);
		ImmutableRect2i navigationArea = unobstructedLayout.navigationArea();
		int tooNarrowGapWidth = navigationArea.height() + 6;
		ImmutableRect2i tooNarrowGapExclusion = new ImmutableRect2i(
			navigationArea.x() + tooNarrowGapWidth,
			navigationArea.y(),
			availableArea.width(),
			navigationArea.height()
		);

		IngredientGridWithNavigationLayout.Layout obstructedLayout = IngredientGridWithNavigationLayout.calculate(
			gridConfig, availableArea, Set.of(tooNarrowGapExclusion), null, 0
		);

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
		ImmutableRect2i availableArea = largeAvailableArea();
		TestGridConfig gridConfig = config()
			.maxColumns(4)
			.maxRows(3)
			.drawBackground(true)
			.buttonNavigationVisibility(NavigationVisibility.ENABLED);
		IngredientGridWithNavigationLayout.Layout unobstructedLayout = IngredientGridWithNavigationLayout.calculate(
			gridConfig, availableArea, Set.of(), null, 0
		);
		ImmutableRect2i navigationArea = unobstructedLayout.navigationArea();
		int tooNarrowGapWidth = navigationArea.height() + 6;
		ImmutableRect2i tooNarrowGapExclusion = new ImmutableRect2i(
			navigationArea.x() + tooNarrowGapWidth,
			navigationArea.y(),
			availableArea.width(),
			navigationArea.height()
		);

		IngredientGridWithNavigationLayout.Layout obstructedLayout = IngredientGridWithNavigationLayout.calculate(
			gridConfig, availableArea, Set.of(tooNarrowGapExclusion), null, 0
		);

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

		IngredientGridWithNavigationLayout.Layout obstructedLayout = IngredientGridWithNavigationLayout.calculate(
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
			.buttonNavigationVisibility(NavigationVisibility.ENABLED);
		IngredientGridWithNavigationLayout.Layout unobstructedLayout = IngredientGridWithNavigationLayout.calculate(
			gridConfig, availableArea, Set.of(), null, 0
		);
		ImmutableRect2i gridArea = unobstructedLayout.ingredientGridArea();
		ImmutableRect2i leftGridExclusion = new ImmutableRect2i(
			gridArea.x(), gridArea.y(), gridArea.width() / 2, gridArea.height()
		);

		IngredientGridWithNavigationLayout.Layout obstructedLayout = IngredientGridWithNavigationLayout.calculate(
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
			.buttonNavigationVisibility(NavigationVisibility.ENABLED);
		IngredientGridWithNavigationLayout.Layout unobstructedLayout = IngredientGridWithNavigationLayout.calculate(
			gridConfig, availableArea, Set.of(), null, 0
		);
		ImmutableRect2i gridArea = unobstructedLayout.ingredientGridArea();
		ImmutableRect2i rightGridExclusion = new ImmutableRect2i(
			gridArea.x() + gridArea.width() / 2,
			gridArea.y(),
			gridArea.width() / 2,
			gridArea.height()
		);

		IngredientGridWithNavigationLayout.Layout obstructedLayout = IngredientGridWithNavigationLayout.calculate(
			gridConfig, availableArea, Set.of(rightGridExclusion), null, 0
		);

		assertEquals(unobstructedLayout.navigationArea(), obstructedLayout.navigationArea());
		assertEquals(unobstructedLayout.ingredientGridArea(), obstructedLayout.ingredientGridArea());
		assertTrue(obstructedLayout.availableSlotCount() > 0);
		assertTrue(obstructedLayout.availableSlotCount() < unobstructedLayout.availableSlotCount());
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
		assertTrue(area.width() > 0, "width should be positive");
		assertTrue(area.height() > 0, "height should be positive");
	}

	private static void assertContainedBy(ImmutableRect2i inner, ImmutableRect2i outer) {
		assertTrue(inner.x() >= outer.x(), () -> inner + " should not start left of " + outer);
		assertTrue(inner.y() >= outer.y(), () -> inner + " should not start above " + outer);
		assertTrue(right(inner) <= right(outer), () -> inner + " should not extend right of " + outer);
		assertTrue(bottom(inner) <= bottom(outer), () -> inner + " should not extend below " + outer);
	}

	private static int right(ImmutableRect2i area) {
		return area.x() + area.width();
	}

	private static int bottom(ImmutableRect2i area) {
		return area.y() + area.height();
	}

	private static final class TestGridConfig implements IIngredientGridConfig {
		private int maxColumns = 9;
		private int minColumns = 1;
		private int maxRows = 16;
		private int minRows = 1;
		private boolean drawBackground;
		private HorizontalAlignment horizontalAlignment = HorizontalAlignment.LEFT;
		private VerticalAlignment verticalAlignment = VerticalAlignment.TOP;
		private NavigationVisibility buttonNavigationVisibility = NavigationVisibility.ENABLED;

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

		TestGridConfig buttonNavigationVisibility(NavigationVisibility buttonNavigationVisibility) {
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

		@Override
		public void addLayoutListener(Runnable listener) {

		}
	}
}
