package mezz.jei.gui.overlay.bookmarks.history;

import mezz.jei.api.gui.placement.HorizontalAlignment;
import mezz.jei.api.gui.placement.VerticalAlignment;
import mezz.jei.common.config.IIngredientGridConfig;
import mezz.jei.common.config.IngredientGridLayoutMode;
import mezz.jei.common.config.IngredientGridNavigationMode;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.common.util.NavigationVisibility;
import mezz.jei.gui.overlay.ingredients.IngredientGrid;
import mezz.jei.gui.overlay.ingredients.IngredientGridWithNavigationLayout;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LookupHistoryOverlayLayoutTest {
	@Test
	public void displayHeightIncludesBackgroundPaddingWhenBackgroundIsEnabled() {
		int rows = 2;
		int withoutBackground = LookupHistoryOverlayLayout.getDisplayHeight(rows, false);
		int withBackground = LookupHistoryOverlayLayout.getDisplayHeight(rows, true);

		assertEquals(rows * LookupHistoryOverlayLayout.SLOT_HEIGHT, withoutBackground);
		assertEquals(withoutBackground + 2 * backgroundPadding(), withBackground);
	}

	@Test
	public void backgroundLayoutContainsSlotBackground() {
		TestGridConfig gridConfig = new TestGridConfig(4, 2, true);
		ImmutableRect2i availableArea = new ImmutableRect2i(
			0,
			0,
			6 * IngredientGrid.INGREDIENT_WIDTH,
			LookupHistoryOverlayLayout.getDisplayHeight(2, true)
		);

		LookupHistoryOverlayLayout layout = LookupHistoryOverlayLayout.calculate(gridConfig, availableArea);

		assertContainedBy(layout.slotBackgroundArea(), layout.backgroundArea());
		assertEquals(availableArea.height(), layout.backgroundArea().height());
	}

	@Test
	public void noBackgroundLayoutUsesGridAreaAsBackgroundArea() {
		TestGridConfig gridConfig = new TestGridConfig(4, 2, false);
		ImmutableRect2i availableArea = new ImmutableRect2i(
			0,
			0,
			6 * IngredientGrid.INGREDIENT_WIDTH,
			LookupHistoryOverlayLayout.getDisplayHeight(2, false)
		);

		LookupHistoryOverlayLayout layout = LookupHistoryOverlayLayout.calculate(gridConfig, availableArea);

		assertEquals(layout.ingredientGridArea(), layout.slotBackgroundArea());
		assertEquals(layout.slotBackgroundArea(), layout.backgroundArea());
	}

	private static int backgroundPadding() {
		return IngredientGridWithNavigationLayout.BORDER_PADDING +
			IngredientGridWithNavigationLayout.INNER_PADDING;
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

	private record TestGridConfig(int getMaxColumns, int getMaxRows, boolean drawBackground) implements IIngredientGridConfig {
		@Override
		public int getMinColumns() {
			return 1;
		}

		@Override
		public int getMinRows() {
			return 1;
		}

		@Override
		public HorizontalAlignment getHorizontalAlignment() {
			return HorizontalAlignment.LEFT;
		}

		@Override
		public VerticalAlignment getVerticalAlignment() {
			return VerticalAlignment.TOP;
		}

		@Override
		public NavigationVisibility getNavigationVisibility() {
			return NavigationVisibility.DISABLED;
		}

		@Override
		public IngredientGridLayoutMode getLayoutMode() {
			return IngredientGridLayoutMode.RECTANGULAR;
		}

		@Override
		public IngredientGridNavigationMode getNavigationMode() {
			return IngredientGridNavigationMode.PAGED;
		}

		@Override
		public void addLayoutListener(Runnable listener) {

		}
	}
}
