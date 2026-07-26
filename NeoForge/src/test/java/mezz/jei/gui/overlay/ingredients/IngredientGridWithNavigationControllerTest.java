package mezz.jei.gui.overlay.ingredients;

import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.gui.handlers.IGuiClickableArea;
import mezz.jei.api.gui.handlers.IGuiProperties;
import mezz.jei.api.gui.placement.HorizontalAlignment;
import mezz.jei.api.gui.placement.VerticalAlignment;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.runtime.IClickableIngredient;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IRecipesGui;
import mezz.jei.api.runtime.IScreenHelper;
import mezz.jei.common.config.IIngredientGridConfig;
import mezz.jei.common.config.IngredientGridNavigationMode;
import mezz.jei.common.network.IConnectionToServer;
import mezz.jei.common.network.packets.PlayToServerPacket;
import mezz.jei.common.util.NavigationVisibility;
import mezz.jei.gui.ghost.GhostIngredientQuickMoveManager;
import mezz.jei.gui.input.IClickableIngredientInternal;
import mezz.jei.gui.input.IDraggableIngredientInternal;
import mezz.jei.gui.input.IRecipeFocusSource;
import mezz.jei.gui.input.IUserInputHandler;
import mezz.jei.gui.overlay.elements.IElement;
import mezz.jei.gui.overlay.elements.IngredientElement;
import mezz.jei.gui.util.CommandUtil;
import mezz.jei.gui.util.FocusUtil;
import mezz.jei.library.focus.FocusFactory;
import mezz.jei.library.ingredients.subtypes.SubtypeInterpreters;
import mezz.jei.library.ingredients.subtypes.SubtypeManager;
import mezz.jei.library.load.registration.IngredientManagerBuilder;
import mezz.jei.test.lib.TestClientConfig;
import mezz.jei.test.lib.TestClientToggleState;
import mezz.jei.test.lib.TestColorHelper;
import mezz.jei.test.lib.TestIngredient;
import mezz.jei.test.lib.TestPlugin;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class IngredientGridWithNavigationControllerTest {
	@Test
	public void singlePageHasNoNextOrPreviousPage() {
		// Setup: the grid has enough slots to show every ingredient on one page.
		Fixture fixture = Fixture.create(3, 3, true);
		fixture.controller.updateLayoutToFirstPage();
		fixture.clearLayoutChanges();

		// Operation: ask the controller for page state and try both explicit page directions.
		boolean hasNext = fixture.controller.hasNext();
		boolean hasPrevious = fixture.controller.hasPrevious();
		boolean nextPage = fixture.controller.nextPage();
		boolean previousPage = fixture.controller.previousPage();

		// Assertions: one-page grids expose no page movement and do not relayout on failed navigation.
		assertFalse(hasNext);
		assertFalse(hasPrevious);
		assertFalse(nextPage);
		assertFalse(previousPage);
		assertEquals(0, fixture.layoutChanges);
		assertEquals(0, fixture.controller.getPageNumber());
	}

	@Test
	public void scrollDownOnSinglePageIsNotConsumed() {
		// Setup: the mouse is over a one-page grid.
		Fixture fixture = Fixture.create(3, 3, true);
		fixture.controller.updateLayoutToFirstPage();
		fixture.clearLayoutChanges();

		// Operation: scroll toward the next page.
		Optional<IUserInputHandler> handler = fixture.controller.handleMouseScrolled(1, 1, 0, -1);

		// Assertions: the grid does not consume scroll events that cannot change pages.
		assertEquals(Optional.empty(), handler);
		assertEquals(0, fixture.layoutChanges);
		assertEquals(0, fixture.controller.getPageNumber());
	}

	@Test
	public void scrollUpOnSinglePageIsNotConsumed() {
		// Setup: the mouse is over a one-page grid.
		Fixture fixture = Fixture.create(3, 3, true);
		fixture.controller.updateLayoutToFirstPage();
		fixture.clearLayoutChanges();

		// Operation: scroll toward the previous page.
		Optional<IUserInputHandler> handler = fixture.controller.handleMouseScrolled(1, 1, 0, 1);

		// Assertions: the grid does not consume scroll events that cannot change pages.
		assertEquals(Optional.empty(), handler);
		assertEquals(0, fixture.layoutChanges);
		assertEquals(0, fixture.controller.getPageNumber());
	}

	@Test
	public void scrollOutsideGridIsNotConsumed() {
		// Setup: the grid has multiple pages, but the mouse is outside its active area.
		Fixture fixture = Fixture.create(3, 7, false);
		fixture.controller.updateLayoutToFirstPage();
		fixture.clearLayoutChanges();

		// Operation: scroll toward the next page outside the grid.
		Optional<IUserInputHandler> handler = fixture.controller.handleMouseScrolled(1, 1, 0, -1);

		// Assertions: page state is unchanged because the grid was not under the mouse.
		assertEquals(Optional.empty(), handler);
		assertEquals(0, fixture.layoutChanges);
		assertEquals(0, fixture.controller.getPageNumber());
	}

	@Test
	public void scrollDownOnMultiplePagesMovesNextPage() {
		// Setup: the mouse is over a grid with more ingredients than visible slots.
		Fixture fixture = Fixture.create(3, 7, true);
		fixture.controller.updateLayoutToFirstPage();
		fixture.clearLayoutChanges();

		// Operation: scroll toward the next page.
		Optional<IUserInputHandler> handler = fixture.controller.handleMouseScrolled(1, 1, 0, -1);

		// Assertions: the controller consumes the scroll and advances one page.
		assertEquals(Optional.of(fixture.controller), handler);
		assertEquals(1, fixture.layoutChanges);
		assertEquals(1, fixture.controller.getPageNumber());
	}

	@Test
	public void scrollUpOnMultiplePagesMovesPreviousPage() {
		// Setup: the grid starts on the second page.
		Fixture fixture = Fixture.create(3, 7, true);
		fixture.controller.updateLayoutToFirstPage();
		assertTrue(fixture.controller.nextPage());
		fixture.clearLayoutChanges();

		// Operation: scroll toward the previous page.
		Optional<IUserInputHandler> handler = fixture.controller.handleMouseScrolled(1, 1, 0, 1);

		// Assertions: the controller consumes the scroll and moves back to page one.
		assertEquals(Optional.of(fixture.controller), handler);
		assertEquals(1, fixture.layoutChanges);
		assertEquals(0, fixture.controller.getPageNumber());
	}

	@Test
	public void nextPageWrapsFromLastPageToFirstPage() {
		// Setup: seven ingredients in a three-slot grid produce three pages, and the controller is on the last page.
		Fixture fixture = Fixture.create(3, 7, true);
		fixture.controller.updateLayoutToFirstPage();
		assertTrue(fixture.controller.nextPage());
		assertTrue(fixture.controller.nextPage());
		fixture.clearLayoutChanges();

		// Operation: move to the next page from the last page.
		boolean moved = fixture.controller.nextPage();

		// Assertions: next-page navigation wraps to the first page.
		assertTrue(moved);
		assertEquals(1, fixture.layoutChanges);
		assertEquals(0, fixture.controller.getPageNumber());
	}

	@Test
	public void previousPageWrapsFromFirstPageToLastPage() {
		// Setup: seven ingredients in a three-slot grid produce three pages, and the controller is on the first page.
		Fixture fixture = Fixture.create(3, 7, true);
		fixture.controller.updateLayoutToFirstPage();
		fixture.clearLayoutChanges();

		// Operation: move to the previous page from the first page.
		boolean moved = fixture.controller.previousPage();

		// Assertions: previous-page navigation wraps to the last page.
		assertTrue(moved);
		assertEquals(1, fixture.layoutChanges);
		assertEquals(2, fixture.controller.getPageNumber());
	}

	@Test
	public void pagedModeKeepsNavigatedPageWhenOverlayReopens() {
		// Setup: the user navigates to the second page, then the overlay closes so the grid no longer exposes
		// visible elements as a fallback anchor.
		Fixture fixture = Fixture.create(3, 7, true);
		fixture.controller.updateLayoutToFirstPage();
		assertTrue(fixture.controller.nextPage());
		fixture.closeOverlay();

		// Operation: reopen the overlay using the controller's page anchor.
		fixture.reopenOverlay();

		// Assertions: the controller remembered the first element on the navigated-to page before the overlay
		// closed, so reopening does not reset to the first page.
		assertEquals(1, fixture.layoutChanges);
		assertEquals(3, fixture.grid.firstItemIndex);
		assertEquals(1, fixture.controller.getPageNumber());
	}

	@Test
	public void zeroScrollDeltaIsNotConsumed() {
		// Setup: the mouse is over a grid with multiple pages.
		Fixture fixture = Fixture.create(3, 7, true);
		fixture.controller.updateLayoutToFirstPage();
		fixture.clearLayoutChanges();

		// Operation: send a scroll event with no vertical delta.
		Optional<IUserInputHandler> handler = fixture.controller.handleMouseScrolled(1, 1, 0, 0);

		// Assertions: no page movement is requested and the event is not consumed.
		assertEquals(Optional.empty(), handler);
		assertEquals(0, fixture.layoutChanges);
		assertEquals(0, fixture.controller.getPageNumber());
	}

	@Test
	public void scrollingModeScrollsRowsWithoutPaging() {
		// Setup: scrollbar mode is enabled for a three-column grid with three rows of ingredients.
		Fixture fixture = Fixture.create(3, 7, true, IngredientGridNavigationMode.SCROLLING);
		fixture.controller.updateLayoutToFirstPage();
		fixture.clearLayoutChanges();

		// Operation: scroll down by one wheel notch.
		Optional<IUserInputHandler> handler = fixture.controller.handleMouseScrolled(1, 1, 0, -1);

		// Assertions: scrollbar mode consumes the scroll and moves down one row instead of a page.
		assertEquals(Optional.of(fixture.controller), handler);
		assertEquals(1, fixture.layoutChanges);
		assertEquals(1, fixture.controller.getPageNumber());
		assertEquals(3, fixture.grid.firstItemIndex);
		assertEquals(0, fixture.grid.scrollOffsetY);
	}

	@Test
	public void scrollingModeKeepsScrolledRowWhenOverlayReopensAfterGridSizeChanges() {
		// Setup: the user scrolls down to the fourth row, then the overlay closes so the grid no longer exposes
		// visible elements as a fallback anchor.
		Fixture fixture = Fixture.create(3, 3, 30, true, IngredientGridNavigationMode.SCROLLING);
		fixture.controller.updateLayoutToFirstPage();
		int hiddenRows = IngredientGridScrollState.getHiddenRows(30, 3, 3);
		fixture.controller.setScrollOffsetY(3 / (float) hiddenRows);
		fixture.closeOverlay();

		// Operation: reopen the overlay after its grid size changes.
		fixture.grid.setGridSize(3, 5);
		fixture.reopenOverlay();

		// Assertions: the scrollbar controller remembered the first visible element at the scrolled row before
		// the overlay closed, so reopening does not reset or drift to another row.
		assertEquals(1, fixture.layoutChanges);
		assertEquals(9, fixture.grid.firstItemIndex);
		assertEquals(3, fixture.controller.getPageNumber());
		assertEquals(0, fixture.grid.scrollOffsetY);
	}

	@Test
	public void smoothScrollingModeScrollsByPixels() {
		// Setup: smooth scrollbar mode is enabled for a three-column grid with three rows of ingredients.
		Fixture fixture = Fixture.create(3, 9, true, IngredientGridNavigationMode.SMOOTH_SCROLLING);
		fixture.controller.updateLayoutToFirstPage();
		fixture.clearLayoutChanges();

		// Operation: scroll down by one wheel notch.
		Optional<IUserInputHandler> handler = fixture.controller.handleMouseScrolled(1, 1, 0, -1);

		// Assertions: smooth mode consumes the scroll and moves by pixels instead of snapping to the next row.
		assertEquals(Optional.of(fixture.controller), handler);
		assertEquals(1, fixture.layoutChanges);
		assertEquals(0, fixture.controller.getPageNumber());
		assertEquals(0, fixture.grid.firstItemIndex);
		assertEquals(6, fixture.grid.scrollOffsetY);
	}

	@Test
	public void scrollingModeDoesNotWrapFromFirstRowToLastRow() {
		// Setup: scrollbar mode starts at the top of a multi-row list.
		Fixture fixture = Fixture.create(3, 7, true, IngredientGridNavigationMode.SCROLLING);
		fixture.controller.updateLayoutToFirstPage();
		fixture.clearLayoutChanges();

		// Operation: request previous-page movement from the first scroll row.
		boolean moved = fixture.controller.previousPage();

		// Assertions: scrollbars clamp at the top instead of wrapping.
		assertFalse(moved);
		assertEquals(0, fixture.layoutChanges);
		assertEquals(0, fixture.controller.getPageNumber());
	}

	@Test
	public void scrollingModeConsumesScrollAtBottom() {
		// Setup: scrollbar mode is already at the bottom of a multi-row list.
		Fixture fixture = Fixture.create(3, 7, true, IngredientGridNavigationMode.SCROLLING);
		fixture.controller.updateLayoutToFirstPage();
		fixture.controller.setScrollOffsetY(1);
		fixture.clearLayoutChanges();

		// Operation: scroll farther toward the bottom.
		Optional<IUserInputHandler> handler = fixture.controller.handleMouseScrolled(1, 1, 0, -1);

		// Assertions: JEI consumes the scroll even though the clamped position does not change.
		assertEquals(Optional.of(fixture.controller), handler);
		assertEquals(0, fixture.layoutChanges);
		assertEquals(2, fixture.controller.getPageNumber());
		assertEquals(6, fixture.grid.firstItemIndex);
	}

	@Test
	public void smoothScrollingModeConsumesScrollAtBottom() {
		// Setup: smooth scrollbar mode is already at the bottom of a multi-row list.
		Fixture fixture = Fixture.create(3, 9, true, IngredientGridNavigationMode.SMOOTH_SCROLLING);
		fixture.controller.updateLayoutToFirstPage();
		fixture.controller.setScrollOffsetY(1);
		fixture.clearLayoutChanges();

		// Operation: scroll farther toward the bottom.
		Optional<IUserInputHandler> handler = fixture.controller.handleMouseScrolled(1, 1, 0, -1);

		// Assertions: JEI consumes the scroll even though the clamped position does not change.
		assertEquals(Optional.of(fixture.controller), handler);
		assertEquals(0, fixture.layoutChanges);
		assertEquals(2, fixture.controller.getPageNumber());
		assertEquals(6, fixture.grid.firstItemIndex);
		assertEquals(0, fixture.grid.scrollOffsetY);
	}

	@Test
	public void scrollingModeCanScrollWhenExclusionReducesVisibleSlots() {
		// Setup: 50 ingredients fit in six nine-column rows, but exclusions leave only 45 usable visible slots.
		Fixture fixture = Fixture.create(9, 6, 50, true, IngredientGridNavigationMode.SCROLLING);
		fixture.grid.setVisibleSlotCount(45);
		fixture.controller.updateLayoutToFirstPage();
		fixture.clearLayoutChanges();

		// Operation: move to the bottom of the scroll range.
		fixture.controller.setScrollOffsetY(1);

		// Assertions: the controller treats the blocked cells as reducing visible capacity.
		assertEquals(1, fixture.layoutChanges);
		assertEquals(5, fixture.grid.firstItemIndex);
	}

	@Test
	public void scrollingModeBottomShowsLastItemsWhenExclusionReducesVisibleSlots() {
		// Setup: exclusions leave fewer usable slots than the raw row count can display.
		Fixture fixture = Fixture.create(9, 6, 100, true, IngredientGridNavigationMode.SCROLLING);
		fixture.grid.setVisibleSlotCount(45);
		fixture.controller.updateLayoutToFirstPage();
		fixture.clearLayoutChanges();

		// Operation: move to the bottom of the scroll range.
		fixture.controller.setScrollOffsetY(1);

		// Assertions: the last 45 visible slots can include the final ingredient.
		assertEquals(1, fixture.layoutChanges);
		assertEquals(55, fixture.grid.firstItemIndex);
	}

	@Test
	public void smoothScrollingModeBottomShowsLastItemsWhenExclusionReducesVisibleSlots() {
		// Setup: smooth scrolling uses a separate pixel-offset render path.
		Fixture fixture = Fixture.create(9, 6, 100, true, IngredientGridNavigationMode.SMOOTH_SCROLLING);
		fixture.grid.setVisibleSlotCount(45);
		fixture.controller.updateLayoutToFirstPage();
		fixture.clearLayoutChanges();

		// Operation: move to the bottom of the scroll range.
		fixture.controller.setScrollOffsetY(1);

		// Assertions: the exact bottom starts late enough for the final ingredient and has no partial-row offset.
		assertEquals(1, fixture.layoutChanges);
		assertEquals(55, fixture.grid.firstItemIndex);
		assertEquals(0, fixture.grid.scrollOffsetY);
	}

	@Test
	public void scrollingModeKeepsClickedAnchorAtRelativePositionWhenVisibleRowsChange() {
		// Setup: a clicked ingredient is one row down in a ten-row viewport.
		Fixture fixture = Fixture.create(10, 10, 1000, true, IngredientGridNavigationMode.SCROLLING);
		fixture.controller.updateLayoutToFirstPage();
		int hiddenRows = IngredientGridScrollState.getHiddenRows(1000, 10, 10);
		fixture.controller.setScrollOffsetY(20 / (float) hiddenRows);
		IElement<?> clickedElement = fixture.source.getElements().get(210);
		IClickableIngredientInternal<?> clickableIngredient = fixture.controller.createPageAnchorIngredient(
			createClickableIngredient(clickedElement)
		);
		clickableIngredient.show(fixture.recipesGui, fixture.focusUtil, List.of());
		fixture.clearLayoutChanges();

		// Operation: the GUI changes to show twenty rows.
		fixture.grid.setGridSize(10, 20);
		fixture.controller.updateLayoutKeepingPageAnchorVisible(fixture.controller.getPageAnchorElement());

		// Assertions: the clicked ingredient is kept near 10% down the new viewport instead of moving to the top.
		assertEquals(1, fixture.layoutChanges);
		assertEquals(190, fixture.grid.firstItemIndex);
	}

	private static class Fixture {
		final IngredientGridWithNavigationController controller;
		final TestNavigationGrid grid;
		final TestIngredientGridSource source;
		final IRecipesGui recipesGui;
		final FocusUtil focusUtil;
		int layoutChanges;

		private Fixture(
			IngredientGridWithNavigationController controller,
			TestNavigationGrid grid,
			TestIngredientGridSource source,
			FocusUtil focusUtil
		) {
			this.controller = controller;
			this.grid = grid;
			this.source = source;
			this.recipesGui = new TestRecipesGui();
			this.focusUtil = focusUtil;
			this.controller.setOnLayoutChanged(() -> this.layoutChanges++);
		}

		static Fixture create(int gridSlots, int itemCount, boolean mouseOver) {
			return create(gridSlots, itemCount, mouseOver, IngredientGridNavigationMode.PAGED);
		}

		static Fixture create(int gridSlots, int itemCount, boolean mouseOver, IngredientGridNavigationMode navigationMode) {
			return create(gridSlots, 1, itemCount, mouseOver, navigationMode);
		}

		static Fixture create(int columns, int rows, int itemCount, boolean mouseOver, IngredientGridNavigationMode navigationMode) {
			TestClientConfig clientConfig = new TestClientConfig(false);
			TestConnectionToServer connection = new TestConnectionToServer();
			IIngredientManager ingredientManager = createIngredientManager();
			FocusUtil focusUtil = new FocusUtil(new FocusFactory(ingredientManager), clientConfig, ingredientManager);
			IRecipeFocusSource emptyFocusSource = new EmptyRecipeFocusSource();
			GhostIngredientQuickMoveManager quickMoveManager = new GhostIngredientQuickMoveManager(emptyFocusSource, new TestScreenHelper());
			CommandUtil commandUtil = new CommandUtil(clientConfig, connection);
			TestNavigationGrid grid = new TestNavigationGrid(columns, rows);
			TestIngredientGridSource source = new TestIngredientGridSource(itemCount);
			IngredientGridWithNavigationController controller = new IngredientGridWithNavigationController(
				source,
				grid,
				new TestGridConfig(navigationMode),
				new TestClientToggleState(),
				clientConfig,
				commandUtil,
				ingredientManager,
				(x, y) -> mouseOver,
				quickMoveManager
			);
			return new Fixture(controller, grid, source, focusUtil);
		}

		void clearLayoutChanges() {
			this.layoutChanges = 0;
		}

		void closeOverlay() {
			this.grid.clearVisibleElements();
			clearLayoutChanges();
		}

		void reopenOverlay() {
			this.controller.updateLayoutKeepingPageAnchorVisible(this.controller.getPageAnchorElement());
		}
	}

	private record TestGridConfig(IngredientGridNavigationMode navigationMode) implements IIngredientGridConfig {
		@Override
		public int getMaxColumns() {
			return 9;
		}

		@Override
		public int getMinColumns() {
			return 1;
		}

		@Override
		public int getMaxRows() {
			return 16;
		}

		@Override
		public int getMinRows() {
			return 1;
		}

		@Override
		public boolean drawBackground() {
			return false;
		}

		@Override
		public IngredientGridNavigationMode getNavigationMode() {
			return navigationMode;
		}

		@Override
		public HorizontalAlignment getHorizontalAlignment() {
			return HorizontalAlignment.RIGHT;
		}

		@Override
		public VerticalAlignment getVerticalAlignment() {
			return VerticalAlignment.TOP;
		}

		@Override
		public NavigationVisibility getNavigationVisibility() {
			return NavigationVisibility.ENABLED;
		}
	}

	private static IIngredientManager createIngredientManager() {
		SubtypeInterpreters subtypeInterpreters = new SubtypeInterpreters();
		SubtypeManager subtypeManager = new SubtypeManager(subtypeInterpreters);
		IngredientManagerBuilder builder = new IngredientManagerBuilder(subtypeManager, new TestColorHelper());
		new TestPlugin().registerIngredients(builder);
		return builder.build();
	}

	private static class TestNavigationGrid implements IIngredientGrid {
		private int columns;
		private int rows;
		private int visibleSlotCount;
		private int firstItemIndex;
		private int scrollOffsetY;
		private List<IElement<?>> visibleElements = List.of();

		private TestNavigationGrid(int slotCount) {
			this(slotCount, 1);
		}

		private TestNavigationGrid(int columns, int rows) {
			this.columns = columns;
			this.rows = rows;
			this.visibleSlotCount = columns * rows;
		}

		private void setGridSize(int columns, int rows) {
			this.columns = columns;
			this.rows = rows;
			this.visibleSlotCount = columns * rows;
		}

		private void setVisibleSlotCount(int visibleSlotCount) {
			this.visibleSlotCount = visibleSlotCount;
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
		public void set(int firstItemIndex, int scrollOffsetY, List<IElement<?>> ingredientList) {
			this.firstItemIndex = firstItemIndex;
			this.scrollOffsetY = scrollOffsetY;
			int startIndex = Math.clamp(firstItemIndex, 0, ingredientList.size());
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
	}

	private static IClickableIngredientInternal<?> createClickableIngredient(IElement<?> element) {
		return createClickableIngredientTyped(element);
	}

	private static class TestRecipesGui implements IRecipesGui {
		@Override
		public void show(List<IFocus<?>> focuses) {
		}

		@Override
		public void showTypes(List<IRecipeType<?>> recipeTypes) {
		}

		@Override
		public <T> void showRecipes(IRecipeCategory<T> recipeCategory, List<T> recipes, List<IFocus<?>> focuses) {
		}

		@Override
		public <T> Optional<T> getIngredientUnderMouse(IIngredientType<T> ingredientType) {
			return Optional.empty();
		}

		@Override
		public Optional<Screen> getParentScreen() {
			return Optional.empty();
		}
	}

	private static <T> IClickableIngredientInternal<T> createClickableIngredientTyped(IElement<T> element) {
		return new TestClickableIngredient<>(element);
	}

	private record TestClickableIngredient<T>(IElement<T> element) implements IClickableIngredientInternal<T> {
		@Override
		public ITypedIngredient<T> getTypedIngredient() {
			return element.getTypedIngredient();
		}

		@Override
		public IElement<T> getElement() {
			return element;
		}

		@Override
		public boolean isMouseOver(double mouseX, double mouseY) {
			return false;
		}

		@Override
		public ItemStack getCheatItemStack(IIngredientManager ingredientManager) {
			return ItemStack.EMPTY;
		}

		@Override
		public boolean canClickToFocus() {
			return false;
		}

		@Override
		public void show(IRecipesGui recipesGui, FocusUtil focusUtil, List<RecipeIngredientRole> roles) {
		}
	}

	private record TestIngredientGridSource(List<IElement<?>> elements) implements IIngredientGridSource {
		private TestIngredientGridSource(int itemCount) {
			this(createElements(itemCount));
		}

		@Override
		public List<IElement<?>> getElements() {
			return elements;
		}

		@Override
		public void addSourceListChangedListener(SourceListChangedListener listener) {
		}
	}

	private static List<IElement<?>> createElements(int itemCount) {
		List<IElement<?>> elements = new ArrayList<>();
		for (int i = 0; i < itemCount; i++) {
			elements.add(new IngredientElement<>(new TestTypedIngredient(new TestIngredient(i))));
		}
		return List.copyOf(elements);
	}

	private record TestTypedIngredient(TestIngredient ingredient) implements mezz.jei.api.ingredients.ITypedIngredient<TestIngredient> {
		@Override
		public mezz.jei.api.ingredients.IIngredientType<TestIngredient> getType() {
			return TestIngredient.TYPE;
		}

		@Override
		public TestIngredient getIngredient() {
			return ingredient;
		}
	}

	private static class EmptyRecipeFocusSource implements IRecipeFocusSource {
		@Override
		public Stream<IClickableIngredientInternal<?>> getIngredientUnderMouse(double mouseX, double mouseY) {
			return Stream.of();
		}

		@Override
		public Stream<IDraggableIngredientInternal<?>> getDraggableIngredientUnderMouse(double mouseX, double mouseY) {
			return Stream.of();
		}
	}

	private static class TestConnectionToServer implements IConnectionToServer {
		@Override
		public boolean isJeiOnServer() {
			return false;
		}

		@Override
		public boolean isSameModLoader() {
			return false;
		}

		@Override
		public boolean canSendPacket(CustomPacketPayload.Type<?> packetType) {
			return false;
		}

		@Override
		public <T extends PlayToServerPacket<T>> void sendPacketToServer(T packet) {
			throw new UnsupportedOperationException("Unexpected packet send");
		}

		@Override
		public void onRuntimeStopped() {
		}
	}

	private static class TestScreenHelper implements IScreenHelper {
		@Override
		public Stream<IClickableIngredient<?>> getClickableIngredientUnderMouse(Screen screen, double mouseX, double mouseY) {
			return Stream.of();
		}

		@Override
		public <T extends Screen> Optional<IGuiProperties> getGuiProperties(T screen) {
			return Optional.empty();
		}

		@Override
		public Stream<IGuiClickableArea> getGuiClickableArea(AbstractContainerScreen<?> guiContainer, double guiMouseX, double guiMouseY) {
			return Stream.of();
		}

		@Override
		public Stream<Rect2i> getGuiExclusionAreas(Screen screen) {
			return Stream.of();
		}

		@Override
		public <T extends Screen> List<IGhostIngredientHandler<T>> getGhostIngredientHandlers(T guiScreen) {
			return List.of();
		}
	}
}
