package mezz.jei.gui.input.handlers;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.api.gui.handlers.IGuiProperties;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.gui.input.IGuiInputLayer;
import mezz.jei.gui.input.IUserInputHandler;
import mezz.jei.gui.input.UserInput;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

public class CombinedInputHandlerTest {
	@Test
	public void firstHandledInputWinsOverLaterHandlers() {
		// Setup: two handlers can both handle the same click.
		RecordingInputHandler transferButtonHandler = new RecordingInputHandler(true);
		RecordingInputHandler recipeIngredientHandler = new RecordingInputHandler(true);

		// Operation: route input through the same first-match loop used by CombinedInputHandler.
		Optional<IUserInputHandler> handled = handleInput(transferButtonHandler, recipeIngredientHandler);

		// Assertions: the first handler wins and the later handler is unfocused without being invoked.
		Assertions.assertSame(transferButtonHandler, handled.orElseThrow());
		Assertions.assertEquals(1, transferButtonHandler.handleCount);
		Assertions.assertEquals(0, transferButtonHandler.unfocusCount);
		Assertions.assertEquals(0, recipeIngredientHandler.handleCount);
		Assertions.assertEquals(1, recipeIngredientHandler.unfocusCount);
	}

	@Test
	public void laterHandlerCanHandleWhenEarlierHandlerDeclines() {
		// Setup: the first handler declines the click and the second handler accepts it.
		RecordingInputHandler firstHandler = new RecordingInputHandler(false);
		RecordingInputHandler secondHandler = new RecordingInputHandler(true);

		// Operation: route input through the ordered handler list.
		Optional<IUserInputHandler> handled = handleInput(firstHandler, secondHandler);

		// Assertions: the first handler is unfocused after declining, and the second handler handles the click.
		Assertions.assertSame(secondHandler, handled.orElseThrow());
		Assertions.assertEquals(1, firstHandler.handleCount);
		Assertions.assertEquals(1, firstHandler.unfocusCount);
		Assertions.assertEquals(1, secondHandler.handleCount);
		Assertions.assertEquals(0, secondHandler.unfocusCount);
	}

	@Test
	public void allHandlersUnfocusWhenNoneHandleInput() {
		// Setup: no handler can handle the click.
		RecordingInputHandler firstHandler = new RecordingInputHandler(false);
		RecordingInputHandler secondHandler = new RecordingInputHandler(false);

		// Operation: route input through every handler.
		Optional<IUserInputHandler> handled = handleInput(firstHandler, secondHandler);

		// Assertions: no handler is returned, and every handler is unfocused after declining.
		Assertions.assertTrue(handled.isEmpty());
		Assertions.assertEquals(1, firstHandler.handleCount);
		Assertions.assertEquals(1, firstHandler.unfocusCount);
		Assertions.assertEquals(1, secondHandler.handleCount);
		Assertions.assertEquals(1, secondHandler.unfocusCount);
	}

	@Test
	public void emptyHandlerListHandlesNoInput() {
		// Setup: a combined handler has no children.
		// Operation: route input through the empty handler list.
		Optional<IUserInputHandler> handled = CombinedInputHandler.handleClickInternal(
			List.of(),
			inputHandler -> ((RecordingInputHandler) inputHandler).handleInput()
		);

		// Assertions: no handler is returned.
		Assertions.assertTrue(handled.isEmpty());
	}

	@Test
	public void firstScrollHandlerWins() {
		// Setup: the first scroll handler declines and two later handlers can handle the scroll.
		RecordingInputHandler firstHandler = new RecordingInputHandler(false, false);
		RecordingInputHandler secondHandler = new RecordingInputHandler(false, true);
		RecordingInputHandler thirdHandler = new RecordingInputHandler(false, true);
		CombinedInputHandler combinedInputHandler = new CombinedInputHandler(
			"scroll handlers",
			List.of(firstHandler, secondHandler, thirdHandler)
		);

		// Operation: route a scroll event through the combined handler.
		Optional<IUserInputHandler> handled = combinedInputHandler.handleMouseScrolled(1, 2, 0, -1);

		// Assertions: scroll handling stops at the first handler that accepts the event.
		Assertions.assertSame(secondHandler, handled.orElseThrow());
		Assertions.assertEquals(1, firstHandler.scrollCount);
		Assertions.assertEquals(1, secondHandler.scrollCount);
		Assertions.assertEquals(0, thirdHandler.scrollCount);
	}

	@Test
	public void noScrollHandlerReturnsEmpty() {
		// Setup: none of the child handlers can handle scroll input.
		RecordingInputHandler firstHandler = new RecordingInputHandler(false, false);
		RecordingInputHandler secondHandler = new RecordingInputHandler(false, false);
		CombinedInputHandler combinedInputHandler = new CombinedInputHandler(
			"scroll handlers",
			List.of(firstHandler, secondHandler)
		);

		// Operation: route a scroll event through the combined handler.
		Optional<IUserInputHandler> handled = combinedInputHandler.handleMouseScrolled(1, 2, 0, -1);

		// Assertions: every child sees the scroll event, and no handler is returned.
		Assertions.assertTrue(handled.isEmpty());
		Assertions.assertEquals(1, firstHandler.scrollCount);
		Assertions.assertEquals(1, secondHandler.scrollCount);
	}

	@Test
	public void hoveredInputLayerBlocksScrollFromLaterHandlers() {
		RecordingInputLayer inputLayer = new RecordingInputLayer(true);
		RecordingInputHandler laterHandler = new RecordingInputHandler(false, true);
		CombinedInputHandler combinedInputHandler = new CombinedInputHandler(
			"scroll handlers",
			List.of(inputLayer, laterHandler)
		);

		Optional<IUserInputHandler> handled = combinedInputHandler.handleMouseScrolled(1, 2, 0, -1);

		Assertions.assertSame(inputLayer, handled.orElseThrow());
		Assertions.assertEquals(1, inputLayer.getScrollCount());
		Assertions.assertEquals(0, laterHandler.scrollCount);
	}

	@Test
	public void unhoveredInputLayerAllowsScrollToReachLaterHandlers() {
		RecordingInputLayer inputLayer = new RecordingInputLayer(false);
		RecordingInputHandler laterHandler = new RecordingInputHandler(false, true);
		CombinedInputHandler combinedInputHandler = new CombinedInputHandler(
			"scroll handlers",
			List.of(inputLayer, laterHandler)
		);

		Optional<IUserInputHandler> handled = combinedInputHandler.handleMouseScrolled(1, 2, 0, -1);

		Assertions.assertSame(laterHandler, handled.orElseThrow());
		Assertions.assertEquals(1, inputLayer.getScrollCount());
		Assertions.assertEquals(1, laterHandler.scrollCount);
	}

	@Test
	public void firstDragHandlerWins() {
		// Setup: the first drag handler declines and two later handlers can handle the drag.
		RecordingInputHandler firstHandler = new RecordingInputHandler(false, false, false);
		RecordingInputHandler secondHandler = new RecordingInputHandler(false, false, true);
		RecordingInputHandler thirdHandler = new RecordingInputHandler(false, false, true);
		CombinedInputHandler combinedInputHandler = new CombinedInputHandler(
			"drag handlers",
			List.of(firstHandler, secondHandler, thirdHandler)
		);

		// Operation: route a drag event through the combined handler.
		InputConstants.Key leftClick = InputConstants.Type.MOUSE.getOrCreate(InputConstants.MOUSE_BUTTON_LEFT);
		Optional<IUserInputHandler> handled = combinedInputHandler.handleMouseDragged(1, 2, leftClick, 0, 1);

		// Assertions: drag handling stops at the first handler that accepts the event.
		Assertions.assertSame(secondHandler, handled.orElseThrow());
		Assertions.assertEquals(1, firstHandler.dragCount);
		Assertions.assertEquals(1, secondHandler.dragCount);
		Assertions.assertEquals(0, thirdHandler.dragCount);
	}

	@Test
	public void noDragHandlerReturnsEmpty() {
		// Setup: none of the child handlers can handle drag input.
		RecordingInputHandler firstHandler = new RecordingInputHandler(false, false, false);
		RecordingInputHandler secondHandler = new RecordingInputHandler(false, false, false);
		CombinedInputHandler combinedInputHandler = new CombinedInputHandler(
			"drag handlers",
			List.of(firstHandler, secondHandler)
		);

		// Operation: route a drag event through the combined handler.
		InputConstants.Key leftClick = InputConstants.Type.MOUSE.getOrCreate(InputConstants.MOUSE_BUTTON_LEFT);
		Optional<IUserInputHandler> handled = combinedInputHandler.handleMouseDragged(1, 2, leftClick, 0, 1);

		// Assertions: every child sees the drag event, and no handler is returned.
		Assertions.assertTrue(handled.isEmpty());
		Assertions.assertEquals(1, firstHandler.dragCount);
		Assertions.assertEquals(1, secondHandler.dragCount);
	}

	private static Optional<IUserInputHandler> handleInput(RecordingInputHandler... handlers) {
		return CombinedInputHandler.handleClickInternal(
			List.of(handlers),
			inputHandler -> ((RecordingInputHandler) inputHandler).handleInput()
		);
	}

	private static class RecordingInputHandler implements IUserInputHandler {
		private final boolean handlesInput;
		private final boolean handlesScroll;
		private final boolean handlesDrag;
		private int handleCount = 0;
		private int unfocusCount = 0;
		private int scrollCount = 0;
		private int dragCount = 0;

		private RecordingInputHandler(boolean handlesInput) {
			this(handlesInput, false);
		}

		private RecordingInputHandler(boolean handlesInput, boolean handlesScroll) {
			this(handlesInput, handlesScroll, false);
		}

		private RecordingInputHandler(boolean handlesInput, boolean handlesScroll, boolean handlesDrag) {
			this.handlesInput = handlesInput;
			this.handlesScroll = handlesScroll;
			this.handlesDrag = handlesDrag;
		}

		private Optional<IUserInputHandler> handleInput() {
			handleCount++;
			if (handlesInput) {
				return Optional.of(this);
			}
			return Optional.empty();
		}

		final int getScrollCount() {
			return this.scrollCount;
		}

		@Override
		public Optional<IUserInputHandler> handleUserInput(
			Screen screen,
			IGuiProperties guiProperties,
			UserInput input,
			IInternalKeyMappings keyBindings
		) {
			return handleInput();
		}

		@Override
		public void unfocus() {
			unfocusCount++;
		}

		@Override
		public Optional<IUserInputHandler> handleMouseScrolled(double mouseX, double mouseY, double scrollDeltaX, double scrollDeltaY) {
			scrollCount++;
			if (handlesScroll) {
				return Optional.of(this);
			}
			return Optional.empty();
		}

		@Override
		public Optional<IUserInputHandler> handleMouseDragged(double mouseX, double mouseY, InputConstants.Key mouseKey, double dragX, double dragY) {
			dragCount++;
			if (handlesDrag) {
				return Optional.of(this);
			}
			return Optional.empty();
		}
	}

	private static class RecordingInputLayer extends RecordingInputHandler implements IGuiInputLayer {
		private final boolean mouseOver;

		private RecordingInputLayer(boolean mouseOver) {
			super(false);
			this.mouseOver = mouseOver;
		}

		@Override
		public boolean isMouseOver(double mouseX, double mouseY) {
			return this.mouseOver;
		}

		@Override
		public void draw(GuiGraphics guiGraphics, int mouseX, int mouseY) {
		}
	}
}
