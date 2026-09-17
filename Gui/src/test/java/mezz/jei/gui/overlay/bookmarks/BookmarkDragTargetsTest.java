package mezz.jei.gui.overlay.bookmarks;

import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IRecipesGui;
import mezz.jei.common.gui.JeiTooltip;
import mezz.jei.gui.bookmarks.BookmarkType;
import mezz.jei.gui.bookmarks.IBookmark;
import mezz.jei.gui.overlay.elements.IElement;
import mezz.jei.gui.overlay.ingredients.IngredientGridTooltipHelper;
import mezz.jei.gui.overlay.ingredients.IngredientListSlot;
import mezz.jei.gui.util.FocusUtil;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BookmarkDragTargetsTest {
	private static final int SLOT_SIZE = 18;

	@Test
	public void occupiedSlotsInsertAfterTheirBookmark() {
		// Setup: a fully occupied page of slots.
		FakeBookmark first = new FakeBookmark();
		FakeBookmark second = new FakeBookmark();
		FakeBookmark third = new FakeBookmark();
		IngredientListSlot firstSlot = occupiedSlot(0, first);
		IngredientListSlot secondSlot = occupiedSlot(1, second);
		IngredientListSlot thirdSlot = occupiedSlot(2, third);

		// Operation: build the drag target specs for the slots.
		List<BookmarkOverlay.DragTargetSpec> specs = BookmarkOverlay.buildSlotDragTargetSpecs(
			List.of(firstSlot, secondSlot, thirdSlot)
		);

		// Assertions: dropping on an occupied slot inserts after that slot's bookmark.
		assertEquals(3, specs.size());
		assertSpec(specs.get(0), firstSlot, first, 0);
		assertSpec(specs.get(1), secondSlot, second, 0);
		assertSpec(specs.get(2), thirdSlot, third, 0);
	}

	@Test
	public void emptySlotsBeforeABookmarkInsertBeforeIt() {
		// Setup: the slot under the drag cursor is empty, with occupied slots around it.
		FakeBookmark first = new FakeBookmark();
		FakeBookmark last = new FakeBookmark();
		IngredientListSlot firstSlot = occupiedSlot(0, first);
		IngredientListSlot gapSlot = emptySlot(1);
		IngredientListSlot lastSlot = occupiedSlot(2, last);

		// Operation: build the drag target specs with the gap between two bookmarks.
		List<BookmarkOverlay.DragTargetSpec> specs = BookmarkOverlay.buildSlotDragTargetSpecs(
			List.of(firstSlot, gapSlot, lastSlot)
		);

		// Assertions: dropping on the gap inserts the dragged bookmark before the bookmark after
		// the gap, so that it lands in the visible gap under the cursor.
		assertEquals(3, specs.size());
		assertSpec(specs.get(0), firstSlot, first, 0);
		assertSpec(specs.get(1), gapSlot, last, -1);
		assertSpec(specs.get(2), lastSlot, last, 0);
	}

	@Test
	public void leadingEmptySlotsInsertBeforeTheFirstBookmark() {
		// Setup: the gap is in the first slot of the page.
		FakeBookmark first = new FakeBookmark();
		IngredientListSlot gapSlot = emptySlot(0);
		IngredientListSlot firstSlot = occupiedSlot(1, first);

		// Operation: build the drag target specs with the gap before the first bookmark.
		List<BookmarkOverlay.DragTargetSpec> specs = BookmarkOverlay.buildSlotDragTargetSpecs(
			List.of(gapSlot, firstSlot)
		);

		// Assertions: dropping on the leading gap inserts before the first bookmark on the page.
		assertEquals(2, specs.size());
		assertSpec(specs.get(0), gapSlot, first, -1);
		assertSpec(specs.get(1), firstSlot, first, 0);
	}

	@Test
	public void trailingEmptySlotsHaveNoSpecs() {
		// Setup: the page ends with empty slots.
		FakeBookmark first = new FakeBookmark();
		FakeBookmark second = new FakeBookmark();
		IngredientListSlot firstSlot = occupiedSlot(0, first);
		IngredientListSlot secondSlot = occupiedSlot(1, second);
		IngredientListSlot trailingEmptySlot = emptySlot(2);

		// Operation: build the drag target specs for the page.
		List<BookmarkOverlay.DragTargetSpec> specs = BookmarkOverlay.buildSlotDragTargetSpecs(
			List.of(firstSlot, secondSlot, trailingEmptySlot)
		);

		// Assertions: trailing empty slots are covered by the whole-contents-area fallback target
		// instead of individual specs.
		assertEquals(2, specs.size());
	}

	@Test
	public void pageWithNoVisibleBookmarksHasNoSpecs() {
		// Setup: the dragged bookmark is the only one on this page and is hidden by the drag.
		IngredientListSlot emptySlot = emptySlot(0);

		// Operation: build the drag target specs for the empty page.
		List<BookmarkOverlay.DragTargetSpec> specs = BookmarkOverlay.buildSlotDragTargetSpecs(
			List.of(emptySlot)
		);

		// Assertions: there are no slot specs to insert around.
		assertTrue(specs.isEmpty());
	}

	@Test
	public void pageBookmarksFallBackToTheDraggedBookmarkOnAnEmptyPage() {
		// Setup: the dragged bookmark is the only one on this page and is hidden by the drag.
		List<BookmarkOverlay.DragTargetSpec> specs = List.of();
		FakeBookmark draggedBookmark = new FakeBookmark();
		List<IElement<?>> elements = List.of(draggedBookmark.getElement());

		// Operation: get the page boundary bookmarks.
		IBookmark firstPageBookmark = BookmarkOverlay.getFirstPageBookmark(specs, draggedBookmark, elements);
		IBookmark lastPageBookmark = BookmarkOverlay.getLastPageBookmark(specs, draggedBookmark, elements);

		// Assertions: both boundaries fall back to the dragged bookmark.
		assertEquals(draggedBookmark, firstPageBookmark);
		assertEquals(draggedBookmark, lastPageBookmark);
	}

	@Test
	public void pageBookmarksUseTheFirstAndLastVisibleBookmarks() {
		// Setup: a page shows two bookmarks, and the dragged bookmark sits between them in the list.
		FakeBookmark firstVisible = new FakeBookmark();
		FakeBookmark draggedBookmark = new FakeBookmark();
		FakeBookmark lastVisible = new FakeBookmark();
		IngredientListSlot firstSlot = occupiedSlot(0, firstVisible);
		IngredientListSlot lastSlot = occupiedSlot(1, lastVisible);
		List<BookmarkOverlay.DragTargetSpec> specs = BookmarkOverlay.buildSlotDragTargetSpecs(
			List.of(firstSlot, lastSlot)
		);
		List<IElement<?>> elements = List.of(
			firstVisible.getElement(),
			draggedBookmark.getElement(),
			lastVisible.getElement()
		);

		// Operation: get the page boundary bookmarks.
		IBookmark firstPageBookmark = BookmarkOverlay.getFirstPageBookmark(specs, draggedBookmark, elements);
		IBookmark lastPageBookmark = BookmarkOverlay.getLastPageBookmark(specs, draggedBookmark, elements);

		// Assertions: the visible page boundaries are used.
		assertEquals(firstVisible, firstPageBookmark);
		assertEquals(lastVisible, lastPageBookmark);
	}

	@Test
	public void pageBookmarksPreferTheDraggedBookmarkWhenItHidesABoundary() {
		// Setup: the dragged bookmark is hidden and sits before the first (or after the last)
		// visible bookmark of the page, so it defines the page boundary.
		FakeBookmark draggedAtStart = new FakeBookmark();
		FakeBookmark firstVisible = new FakeBookmark();
		FakeBookmark lastVisible = new FakeBookmark();
		FakeBookmark draggedAtEnd = new FakeBookmark();
		IngredientListSlot firstSlot = occupiedSlot(0, firstVisible);
		IngredientListSlot lastSlot = occupiedSlot(1, lastVisible);
		List<BookmarkOverlay.DragTargetSpec> specs = BookmarkOverlay.buildSlotDragTargetSpecs(
			List.of(firstSlot, lastSlot)
		);
		List<IElement<?>> elements = List.of(
			draggedAtStart.getElement(),
			firstVisible.getElement(),
			lastVisible.getElement(),
			draggedAtEnd.getElement()
		);

		// Operation: get the page boundary bookmarks for both hidden dragged positions.
		IBookmark firstPageBookmark = BookmarkOverlay.getFirstPageBookmark(specs, draggedAtStart, elements);
		IBookmark lastPageBookmark = BookmarkOverlay.getLastPageBookmark(specs, draggedAtEnd, elements);

		// Assertions: the dragged bookmark defines the boundary it hides, so dropping it on a page
		// button still moves it across that boundary.
		assertEquals(draggedAtStart, firstPageBookmark);
		assertEquals(draggedAtEnd, lastPageBookmark);
	}

	private static void assertSpec(BookmarkOverlay.DragTargetSpec spec, IngredientListSlot slot, IBookmark anchor, int offset) {
		assertEquals(slot.getArea(), spec.area());
		assertEquals(anchor, spec.anchor());
		assertEquals(offset, spec.offset());
	}

	private static IngredientListSlot occupiedSlot(int slotIndex, FakeBookmark bookmark) {
		IngredientListSlot slot = emptySlot(slotIndex);
		slot.setElement(bookmark.getElement());
		return slot;
	}

	private static IngredientListSlot emptySlot(int slotIndex) {
		return new IngredientListSlot(0, slotIndex * SLOT_SIZE, SLOT_SIZE, SLOT_SIZE, 1);
	}

	private static class FakeBookmark implements IBookmark {
		private final FakeElement element = new FakeElement(this);

		@Override
		public BookmarkType getType() {
			return BookmarkType.INGREDIENT;
		}

		@Override
		public IElement<?> getElement() {
			return element;
		}

		@Override
		public boolean isVisible() {
			return true;
		}

		@Override
		public void setVisible(boolean visible) {
		}
	}

	private static class FakeElement implements IElement<Object> {
		private final IBookmark bookmark;

		private FakeElement(IBookmark bookmark) {
			this.bookmark = bookmark;
		}

		@Override
		public ITypedIngredient<Object> getTypedIngredient() {
			throw new UnsupportedOperationException();
		}

		@Override
		public Optional<IBookmark> getBookmark() {
			return Optional.of(bookmark);
		}

		@Override
		public @Nullable IDrawable createRenderOverlay() {
			return null;
		}

		@Override
		public void show(IRecipesGui recipesGui, FocusUtil focusUtil, List<RecipeIngredientRole> roles) {
		}

		@Override
		public void getTooltip(JeiTooltip tooltip, IngredientGridTooltipHelper tooltipHelper, IIngredientRenderer<Object> ingredientRenderer, IIngredientHelper<Object> ingredientHelper) {
		}

		@Override
		public boolean isVisible() {
			return true;
		}

		@Override
		public void tick() {
		}
	}
}
