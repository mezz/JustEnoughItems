package mezz.jei.gui.overlay.bookmarks.history;

import mezz.jei.common.config.IIngredientGridConfig;
import mezz.jei.common.config.IngredientGridNavigationMode;
import mezz.jei.api.gui.placement.HorizontalAlignment;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.common.util.NavigationVisibility;
import mezz.jei.api.gui.placement.VerticalAlignment;
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
		assertEquals(
			withoutBackground + 2 * backgroundPadding(),
			withBackground
		);
	}

	@Test
	public void backgroundLayoutContainsSlotBackground() {
		TestGridConfig gridConfig = config()
			.maxColumns(4)
			.maxRows(2)
			.drawBackground(true);
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
		TestGridConfig gridConfig = config()
			.maxColumns(4)
			.maxRows(2)
			.drawBackground(false);
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

	private static TestGridConfig config() {
		return new TestGridConfig();
	}

	private static final class TestGridConfig implements IIngredientGridConfig {
		private int maxColumns = 9;
		private int minColumns = 1;
		private int maxRows = 16;
		private int minRows = 1;
		private boolean drawBackground;
		private HorizontalAlignment horizontalAlignment = HorizontalAlignment.LEFT;
		private VerticalAlignment verticalAlignment = VerticalAlignment.TOP;
		private NavigationVisibility navigationVisibility = NavigationVisibility.DISABLED;
		private IngredientGridNavigationMode navigationMode = IngredientGridNavigationMode.PAGED;

		TestGridConfig maxColumns(int maxColumns) {
			this.maxColumns = maxColumns;
			return this;
		}

		TestGridConfig maxRows(int maxRows) {
			this.maxRows = maxRows;
			return this;
		}

		TestGridConfig drawBackground(boolean drawBackground) {
			this.drawBackground = drawBackground;
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
