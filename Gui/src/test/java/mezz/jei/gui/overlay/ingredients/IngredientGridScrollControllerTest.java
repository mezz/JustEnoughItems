package mezz.jei.gui.overlay.ingredients;

import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.common.config.IIngredientGridConfig;
import mezz.jei.common.config.IngredientGridNavigationMode;
import mezz.jei.common.util.HorizontalAlignment;
import mezz.jei.common.util.NavigationVisibility;
import mezz.jei.common.util.VerticalAlignment;
import mezz.jei.gui.input.IClickableIngredientInternal;
import mezz.jei.gui.input.IDraggableIngredientInternal;
import mezz.jei.gui.overlay.elements.IElement;
import mezz.jei.gui.overlay.elements.IngredientElement;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class IngredientGridScrollControllerTest {
	private static final IIngredientType<Integer> INTEGER_TYPE = () -> Integer.class;

	@Test
	public void scrollingModeKeepsScrolledRowWhenOverlayReopensAfterGridSizeChanges() {
		// Setup: the user scrolls down to the fourth row, then the overlay closes so the grid no longer exposes
		// visible elements as a fallback anchor.
		List<IElement<?>> elements = createElements(30);
		TestIngredientGridSource source = new TestIngredientGridSource(elements);
		TestIngredientGrid grid = new TestIngredientGrid(3, 3);
		IngredientGridScrollController controller = new IngredientGridScrollController(
			source,
			grid,
			new TestGridConfig(IngredientGridNavigationMode.SCROLLING),
			null
		);
		controller.updateLayoutStartingAt(0);
		int hiddenRows = IngredientGridScrollState.getHiddenRows(elements.size(), grid.getColumnCount(), grid.getRowCount());
		assertTrue(controller.setScrollOffsetY(3 / (float) hiddenRows));
		grid.clearVisibleElements();

		// Operation: reopen the overlay after its grid size changes.
		grid.setGridSize(3, 5);
		controller.updateLayoutKeepingScrollAnchorVisible(controller.getScrollAnchorElement());

		// Assertions: the scrollbar controller remembered the first visible element at the scrolled row before
		// the overlay closed, so reopening does not reset or drift to another row.
		assertEquals(9, grid.firstItemIndex);
		assertEquals(0, grid.scrollOffsetY);
		assertEquals(3, controller.getFirstVisibleScrollRow());
	}

	private static List<IElement<?>> createElements(int itemCount) {
		List<IElement<?>> elements = new ArrayList<>();
		for (int i = 0; i < itemCount; i++) {
			elements.add(new IngredientElement<>(new TestTypedIngredient(i)));
		}
		return List.copyOf(elements);
	}

	private record TestTypedIngredient(Integer ingredient) implements ITypedIngredient<Integer> {
		@Override
		public IIngredientType<Integer> getType() {
			return INTEGER_TYPE;
		}

		@Override
		public Integer getIngredient() {
			return ingredient;
		}

		@Override
		public <V> ITypedIngredient<V> cast(IIngredientType<V> ingredientType) {
			if (getType().equals(ingredientType)) {
				@SuppressWarnings("unchecked")
				ITypedIngredient<V> cast = (ITypedIngredient<V>) this;
				return cast;
			}
			return null;
		}
	}

	private record TestIngredientGridSource(List<IElement<?>> elements) implements IIngredientGridSource {
		@Override
		public List<IElement<?>> getElements() {
			return elements;
		}

		@Override
		public void addSourceListChangedListener(SourceListChangedListener listener) {

		}
	}

	private record TestGridConfig(IngredientGridNavigationMode navigationMode) implements IIngredientGridConfig {
		@Override
		public int getMaxColumns() {
			return 0;
		}

		@Override
		public int getMinColumns() {
			return 0;
		}

		@Override
		public int getMaxRows() {
			return 0;
		}

		@Override
		public int getMinRows() {
			return 0;
		}

		@Override
		public boolean drawBackground() {
			return false;
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
			return NavigationVisibility.ENABLED;
		}

		@Override
		public IngredientGridNavigationMode getNavigationMode() {
			return navigationMode;
		}

		@Override
		public void addLayoutListener(Runnable listener) {

		}
	}

	private static class TestIngredientGrid implements IIngredientGrid {
		private int columns;
		private int rows;
		private int visibleSlotCount;
		private int firstItemIndex;
		private int scrollOffsetY;
		private List<IElement<?>> visibleElements = List.of();

		private TestIngredientGrid(int columns, int rows) {
			setGridSize(columns, rows);
		}

		private void setGridSize(int columns, int rows) {
			this.columns = columns;
			this.rows = rows;
			this.visibleSlotCount = columns * rows;
		}

		private void clearVisibleElements() {
			this.visibleElements = List.of();
		}

		@Override
		public boolean isMouseOver(double mouseX, double mouseY) {
			return false;
		}

		@Override
		public int size() {
			return visibleSlotCount;
		}

		@Override
		public int getColumnCount() {
			return columns;
		}

		@Override
		public int getRowCount() {
			return rows;
		}

		@Override
		public void set(int firstItemIndex, List<IElement<?>> ingredientList) {
			set(firstItemIndex, 0, ingredientList);
		}

		@Override
		public void set(int firstItemIndex, int smoothScrollRowPixelOffset, List<IElement<?>> ingredientList) {
			this.firstItemIndex = firstItemIndex;
			this.scrollOffsetY = smoothScrollRowPixelOffset;
			int startIndex = clamp(firstItemIndex, 0, ingredientList.size());
			int endIndex = Math.min(startIndex + this.visibleSlotCount, ingredientList.size());
			this.visibleElements = List.copyOf(ingredientList.subList(startIndex, endIndex));
		}

		@Override
		public Stream<IElement<?>> getVisibleElements() {
			return visibleElements.stream();
		}

		@Override
		public void tick() {

		}

		@Override
		public Stream<IClickableIngredientInternal<?>> getIngredientUnderMouse(double mouseX, double mouseY) {
			return Stream.of();
		}

		@Override
		public Stream<IDraggableIngredientInternal<?>> getDraggableIngredientUnderMouse(double mouseX, double mouseY) {
			return Stream.of();
		}

		private static int clamp(int value, int min, int max) {
			return Math.max(min, Math.min(value, max));
		}
	}
}
