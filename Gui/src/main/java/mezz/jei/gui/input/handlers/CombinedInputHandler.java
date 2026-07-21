package mezz.jei.gui.input.handlers;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.api.gui.handlers.IGuiProperties;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.gui.input.IUserInputHandler;
import mezz.jei.gui.input.UserInput;
import net.minecraft.client.gui.screens.Screen;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CombinedInputHandler implements IUserInputHandler {
	private final String debugName;
	private final List<IUserInputHandler> inputHandlers;

	public CombinedInputHandler(String debugName, IUserInputHandler... inputHandlers) {
		this.debugName = debugName;
		this.inputHandlers = List.of(inputHandlers);
	}

	public CombinedInputHandler(String debugName, List<IUserInputHandler> inputHandlers) {
		this.debugName = debugName;
		this.inputHandlers = List.copyOf(inputHandlers);
	}

	@Override
	public Optional<IUserInputHandler> handleUserInput(Screen screen, IGuiProperties guiProperties, UserInput input, IInternalKeyMappings keyBindings) {
		return switch (input.getInputType()) {
			case IMMEDIATE, SIMULATE -> handleClickInternal(screen, guiProperties, input, keyBindings);
			case EXECUTE -> Optional.empty();
		};
	}

	/**
	 * Calls handleClick on each mouse handler until one handles the click (returns non-null).
	 * <p>
	 * handleMouseClickedOut will be called on:
	 * 1. every mouse handler that fails to handleClick (returned null).
	 * 2. every mouse handler that never got a chance to handleClick because something else handled it first.
	 */
	private Optional<IUserInputHandler> handleClickInternal(Screen screen, IGuiProperties guiProperties, UserInput input, IInternalKeyMappings keyBindings) {
		return handleClickInternal(this.inputHandlers, inputHandler -> inputHandler.handleUserInput(screen, guiProperties, input, keyBindings));
	}

	static Optional<IUserInputHandler> handleClickInternal(
		List<IUserInputHandler> inputHandlers,
		Function<IUserInputHandler, Optional<IUserInputHandler>> handleInput
	) {
		Optional<IUserInputHandler> firstHandled = Optional.empty();
		for (IUserInputHandler inputHandler : inputHandlers) {
			if (firstHandled.isEmpty()) {
				firstHandled = handleInput.apply(inputHandler);
				if (firstHandled.isEmpty()) {
					inputHandler.unfocus();
				}
			} else {
				inputHandler.unfocus();
			}
		}
		return firstHandled;
	}

	@Override
	public void unfocus() {
		for (IUserInputHandler inputHandler : this.inputHandlers) {
			inputHandler.unfocus();
		}
	}

	@Override
	public Optional<IUserInputHandler> handleMouseScrolled(double mouseX, double mouseY, double scrollDeltaX, double scrollDeltaY) {
		return inputHandlers.stream()
			.flatMap(inputHandler -> inputHandler.handleMouseScrolled(mouseX, mouseY, scrollDeltaX, scrollDeltaY).stream())
			.findFirst();
	}

	@Override
	public Optional<IUserInputHandler> handleMouseDragged(double mouseX, double mouseY, InputConstants.Key mouseKey, double dragX, double dragY) {
		return inputHandlers.stream()
			.flatMap(inputHandler -> inputHandler.handleMouseDragged(mouseX, mouseY, mouseKey, dragX, dragY).stream())
			.findFirst();
	}

	@Override
	public String toString() {
		String inputHandlersString = inputHandlers.stream().map(IUserInputHandler::toString).collect(Collectors.joining(", ", "[", "]"));
		return "CombinedInputHandler{" +
			"name=" + debugName + " " +
			"inputHandlers=" + inputHandlersString +
			'}';
	}
}
