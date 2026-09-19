package mezz.jei.gui.overlay.bookmarks;

import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IFocusFactory;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IRecipesGui;
import mezz.jei.common.gui.JeiTooltip;
import mezz.jei.common.transfer.RecipeTransferService;
import mezz.jei.gui.bookmarks.BookmarkList;
import mezz.jei.gui.bookmarks.IBookmark;
import mezz.jei.gui.config.IBookmarkConfig;
import mezz.jei.gui.overlay.elements.IElement;
import mezz.jei.gui.overlay.ingredients.IngredientGridTooltipHelper;
import mezz.jei.gui.overlay.ingredients.IngredientListSlot;
import mezz.jei.gui.util.FocusUtil;
import net.minecraft.core.RegistryAccess;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BookmarkDragTargetsTest {
	private static final int SLOT_SIZE = 18;

	@Test
	public void occupiedSlotsUseTheirPositionInTheFullBookmarkList() {
		FakeBookmark earlierPage = new FakeBookmark();
		FakeBookmark first = new FakeBookmark();
		FakeBookmark second = new FakeBookmark();
		FakeBookmark laterPage = new FakeBookmark();
		List<IElement<?>> elements = elements(earlierPage, first, second, laterPage);
		List<IngredientListSlot> slots = List.of(occupiedSlot(0, first), occupiedSlot(1, second));

		for (FakeBookmark dragged : List.of(earlierPage, laterPage)) {
			List<BookmarkDragTarget> targets = BookmarkDragTarget.createSlotTargets(slots, elements, dragged);
			assertEquals(List.of(
				new BookmarkDragTarget(slots.get(0).getArea(), 1),
				new BookmarkDragTarget(slots.get(1).getArea(), 2)
			), targets);
		}
	}

	@Test
	public void gapBeforeABookmarkAccountsForRemovingAnEarlierBookmark() {
		FakeBookmark dragged = new FakeBookmark();
		FakeBookmark first = new FakeBookmark();
		FakeBookmark last = new FakeBookmark();
		List<IngredientListSlot> slots = List.of(occupiedSlot(0, first), emptySlot(1), occupiedSlot(2, last));

		List<BookmarkDragTarget> targets = BookmarkDragTarget.createSlotTargets(slots, elements(dragged, first, last), dragged);

		assertEquals(new BookmarkDragTarget(slots.get(1).getArea(), 1), targets.get(1));
	}

	@Test
	public void gapBeforeABookmarkDoesNotShiftWhenDraggingFromALaterPage() {
		FakeBookmark first = new FakeBookmark();
		FakeBookmark last = new FakeBookmark();
		FakeBookmark dragged = new FakeBookmark();
		List<IngredientListSlot> slots = List.of(occupiedSlot(0, first), emptySlot(1), occupiedSlot(2, last));

		List<BookmarkDragTarget> targets = BookmarkDragTarget.createSlotTargets(slots, elements(first, last, dragged), dragged);

		assertEquals(new BookmarkDragTarget(slots.get(1).getArea(), 1), targets.get(1));
	}

	@Test
	public void leadingGapsInsertBeforeTheFirstBookmark() {
		FakeBookmark first = new FakeBookmark();
		FakeBookmark dragged = new FakeBookmark();
		List<IngredientListSlot> slots = List.of(emptySlot(0), emptySlot(1), occupiedSlot(2, first));

		List<BookmarkDragTarget> targets = BookmarkDragTarget.createSlotTargets(slots, elements(first, dragged), dragged);

		assertEquals(List.of(
			new BookmarkDragTarget(slots.get(0).getArea(), 0),
			new BookmarkDragTarget(slots.get(1).getArea(), 0),
			new BookmarkDragTarget(slots.get(2).getArea(), 0)
		), targets);
	}

	@Test
	public void trailingGapsUseThePageFallback() {
		FakeBookmark first = new FakeBookmark();
		FakeBookmark dragged = new FakeBookmark();
		List<IngredientListSlot> slots = List.of(occupiedSlot(0, first), emptySlot(1));

		List<BookmarkDragTarget> targets = BookmarkDragTarget.createSlotTargets(slots, elements(first, dragged), dragged);

		assertEquals(List.of(new BookmarkDragTarget(slots.get(0).getArea(), 0)), targets);
	}

	@Test
	public void pageWithNoVisibleBookmarksUsesThePageFallback() {
		FakeBookmark dragged = new FakeBookmark();

		List<BookmarkDragTarget> targets = BookmarkDragTarget.createSlotTargets(List.of(emptySlot(0)), elements(dragged), dragged);

		assertTrue(targets.isEmpty());
	}

	@Test
	@SuppressWarnings("DataFlowIssue")
	public void droppingIntoAGapFromEitherDirectionSavesTheSameOrder() {
		FakeBookmark first = new FakeBookmark();
		FakeBookmark last = new FakeBookmark();
		FakeBookmark dragged = new FakeBookmark();
		List<IngredientListSlot> slots = List.of(occupiedSlot(0, first), emptySlot(1), occupiedSlot(2, last));
		for (List<IBookmark> initialOrder : List.<List<IBookmark>>of(List.of(dragged, first, last), List.of(first, last, dragged))) {
			RecordingBookmarkConfig config = new RecordingBookmarkConfig();
			BookmarkList bookmarks = new BookmarkList(null, null, null, null, config, null, null);
			initialOrder.forEach(bookmark -> bookmarks.addToListWithoutNotifying(bookmark, false));
			List<BookmarkDragTarget> targets = BookmarkDragTarget.createSlotTargets(slots, bookmarks.getElements(), dragged);

			bookmarks.moveBookmark(dragged, targets.get(1).index());

			assertEquals(elements(first, dragged, last), bookmarks.getElements());
			assertEquals(List.of(first, dragged, last), config.savedBookmarks);
		}
	}

	private static class RecordingBookmarkConfig implements IBookmarkConfig {
		private List<IBookmark> savedBookmarks = List.of();

		@Override
		public void saveBookmarks(IRecipeManager recipeManager, IFocusFactory focusFactory, IGuiHelper guiHelper, IIngredientManager ingredientManager, RegistryAccess registryAccess, Collection<IBookmark> bookmarks) {
			this.savedBookmarks = List.copyOf(bookmarks);
		}

		@Override
		public void loadBookmarks(IRecipeManager recipeManager, IFocusFactory focusFactory, IGuiHelper guiHelper, IIngredientManager ingredientManager, RegistryAccess registryAccess, BookmarkList bookmarkList, RecipeTransferService recipeTransferService) {
			throw new UnsupportedOperationException();
		}
	}

	private static List<IElement<?>> elements(FakeBookmark... bookmarks) {
		return Arrays.stream(bookmarks)
			.map(FakeBookmark::getElement)
			.toList();
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
