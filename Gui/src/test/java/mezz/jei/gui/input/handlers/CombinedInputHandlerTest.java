package mezz.jei.gui.input.handlers;

import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.gui.input.IUserInputHandler;
import mezz.jei.gui.input.UserInput;
import net.minecraft.client.gui.screens.Screen;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

public class CombinedInputHandlerTest {
	@Test
	public void firstHandledInputWinsOverLaterHandlers() {
		RecordingInputHandler transferButtonHandler = new RecordingInputHandler(true);
		RecordingInputHandler recipeIngredientHandler = new RecordingInputHandler(true);

		Optional<IUserInputHandler> handled = handleInput(transferButtonHandler, recipeIngredientHandler);

		Assertions.assertSame(transferButtonHandler, handled.orElseThrow());
		Assertions.assertEquals(1, transferButtonHandler.handleCount);
		Assertions.assertEquals(0, transferButtonHandler.unfocusCount);
		Assertions.assertEquals(0, recipeIngredientHandler.handleCount);
		Assertions.assertEquals(1, recipeIngredientHandler.unfocusCount);
	}

	@Test
	public void laterHandlerCanHandleWhenEarlierHandlerDeclines() {
		RecordingInputHandler firstHandler = new RecordingInputHandler(false);
		RecordingInputHandler secondHandler = new RecordingInputHandler(true);

		Optional<IUserInputHandler> handled = handleInput(firstHandler, secondHandler);

		Assertions.assertSame(secondHandler, handled.orElseThrow());
		Assertions.assertEquals(1, firstHandler.handleCount);
		Assertions.assertEquals(1, firstHandler.unfocusCount);
		Assertions.assertEquals(1, secondHandler.handleCount);
		Assertions.assertEquals(0, secondHandler.unfocusCount);
	}

	@Test
	public void allHandlersUnfocusWhenNoneHandleInput() {
		RecordingInputHandler firstHandler = new RecordingInputHandler(false);
		RecordingInputHandler secondHandler = new RecordingInputHandler(false);

		Optional<IUserInputHandler> handled = handleInput(firstHandler, secondHandler);

		Assertions.assertTrue(handled.isEmpty());
		Assertions.assertEquals(1, firstHandler.handleCount);
		Assertions.assertEquals(1, firstHandler.unfocusCount);
		Assertions.assertEquals(1, secondHandler.handleCount);
		Assertions.assertEquals(1, secondHandler.unfocusCount);
	}

	@Test
	public void emptyHandlerListHandlesNoInput() {
		Optional<IUserInputHandler> handled = CombinedInputHandler.handleClickInternal(
			List.of(),
			inputHandler -> ((RecordingInputHandler) inputHandler).handleInput()
		);

		Assertions.assertTrue(handled.isEmpty());
	}

	@Test
	public void firstScrollHandlerWins() {
		RecordingInputHandler firstHandler = new RecordingInputHandler(false, false);
		RecordingInputHandler secondHandler = new RecordingInputHandler(false, true);
		RecordingInputHandler thirdHandler = new RecordingInputHandler(false, true);
		CombinedInputHandler combinedInputHandler = new CombinedInputHandler(
			"scroll handlers",
			List.of(firstHandler, secondHandler, thirdHandler)
		);

		Optional<IUserInputHandler> handled = combinedInputHandler.handleMouseScrolled(1, 2, -1);

		Assertions.assertSame(secondHandler, handled.orElseThrow());
		Assertions.assertEquals(1, firstHandler.scrollCount);
		Assertions.assertEquals(1, secondHandler.scrollCount);
		Assertions.assertEquals(0, thirdHandler.scrollCount);
	}

	@Test
	public void noScrollHandlerReturnsEmpty() {
		RecordingInputHandler firstHandler = new RecordingInputHandler(false, false);
		RecordingInputHandler secondHandler = new RecordingInputHandler(false, false);
		CombinedInputHandler combinedInputHandler = new CombinedInputHandler(
			"scroll handlers",
			List.of(firstHandler, secondHandler)
		);

		Optional<IUserInputHandler> handled = combinedInputHandler.handleMouseScrolled(1, 2, -1);

		Assertions.assertTrue(handled.isEmpty());
		Assertions.assertEquals(1, firstHandler.scrollCount);
		Assertions.assertEquals(1, secondHandler.scrollCount);
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
		private int handleCount = 0;
		private int unfocusCount = 0;
		private int scrollCount = 0;

		private RecordingInputHandler(boolean handlesInput) {
			this(handlesInput, false);
		}

		private RecordingInputHandler(boolean handlesInput, boolean handlesScroll) {
			this.handlesInput = handlesInput;
			this.handlesScroll = handlesScroll;
		}

		private Optional<IUserInputHandler> handleInput() {
			handleCount++;
			if (handlesInput) {
				return Optional.of(this);
			}
			return Optional.empty();
		}

		@Override
		public Optional<IUserInputHandler> handleUserInput(
			Screen screen,
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
		public Optional<IUserInputHandler> handleMouseScrolled(double mouseX, double mouseY, double scrollDelta) {
			scrollCount++;
			if (handlesScroll) {
				return Optional.of(this);
			}
			return Optional.empty();
		}
	}
}
