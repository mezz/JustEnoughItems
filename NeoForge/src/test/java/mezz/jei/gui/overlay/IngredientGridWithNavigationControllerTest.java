package mezz.jei.gui.overlay;

import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.gui.handlers.IGuiClickableArea;
import mezz.jei.api.gui.handlers.IGuiProperties;
import mezz.jei.api.runtime.IClickableIngredient;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IScreenHelper;
import mezz.jei.common.network.IConnectionToServer;
import mezz.jei.common.network.packets.PlayToServerPacket;
import mezz.jei.gui.ghost.GhostIngredientQuickMoveManager;
import mezz.jei.gui.input.IClickableIngredientInternal;
import mezz.jei.gui.input.IDraggableIngredientInternal;
import mezz.jei.gui.input.IRecipeFocusSource;
import mezz.jei.gui.input.IUserInputHandler;
import mezz.jei.gui.overlay.elements.IElement;
import mezz.jei.gui.overlay.elements.IngredientElement;
import mezz.jei.gui.util.CommandUtil;
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

	private static class Fixture {
		final IngredientGridWithNavigationController controller;
		int layoutChanges;

		private Fixture(IngredientGridWithNavigationController controller) {
			this.controller = controller;
			this.controller.setOnLayoutChanged(() -> this.layoutChanges++);
		}

		static Fixture create(int gridSlots, int itemCount, boolean mouseOver) {
			TestClientConfig clientConfig = new TestClientConfig(false);
			TestConnectionToServer connection = new TestConnectionToServer();
			IIngredientManager ingredientManager = createIngredientManager();
			IRecipeFocusSource emptyFocusSource = new EmptyRecipeFocusSource();
			GhostIngredientQuickMoveManager quickMoveManager = new GhostIngredientQuickMoveManager(emptyFocusSource, new TestScreenHelper());
			CommandUtil commandUtil = new CommandUtil(clientConfig, connection);
			IngredientGridWithNavigationController controller = new IngredientGridWithNavigationController(
				new TestIngredientGridSource(itemCount),
				new TestNavigationGrid(gridSlots),
				new TestClientToggleState(),
				clientConfig,
				commandUtil,
				ingredientManager,
				(x, y) -> mouseOver,
				quickMoveManager
			);
			return new Fixture(controller);
		}

		void clearLayoutChanges() {
			this.layoutChanges = 0;
		}
	}

	private static IIngredientManager createIngredientManager() {
		SubtypeInterpreters subtypeInterpreters = new SubtypeInterpreters();
		SubtypeManager subtypeManager = new SubtypeManager(subtypeInterpreters);
		IngredientManagerBuilder builder = new IngredientManagerBuilder(subtypeManager, new TestColorHelper());
		new TestPlugin().registerIngredients(builder);
		return builder.build();
	}

	private record TestNavigationGrid(int slotCount) implements IIngredientGrid {
		@Override
		public boolean isMouseOver(double mouseX, double mouseY) {
			return false;
		}

		@Override
		public int size() {
			return slotCount;
		}

		@Override
		public void set(int firstItemIndex, List<IElement<?>> ingredientList) {
		}

		@Override
		public Stream<IElement<?>> getVisibleElements() {
			return Stream.of();
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
