package mezz.jei.gui.input.handlers;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.gui.input.UserInput;
import mezz.jei.gui.input.IUserInputHandler;
import net.minecraft.client.gui.screens.Screen;

import java.util.List;
import java.util.Optional;

public class CombinedInputHandler implements IUserInputHandler {
	private final List<IUserInputHandler> inputHandlers;

	public CombinedInputHandler(IUserInputHandler... inputHandlers) {
		this.inputHandlers = List.of(inputHandlers);
	}

	public CombinedInputHandler(List<IUserInputHandler> inputHandlers) {
		this.inputHandlers = List.copyOf(inputHandlers);
	}

	@Override
	public Optional<IUserInputHandler> handleUserInput(Screen screen, UserInput input, IInternalKeyMappings keyBindings) {
		return switch (input.getClickState()) {
			case IMMEDIATE, SIMULATE -> handleClickInternal(screen, input, keyBindings);
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
	private Optional<IUserInputHandler> handleClickInternal(Screen screen, UserInput input, IInternalKeyMappings keyBindings) {
		Optional<IUserInputHandler> firstHandled = Optional.empty();
		for (IUserInputHandler inputHandler : this.inputHandlers) {
			if (firstHandled.isEmpty()) {
				firstHandled = inputHandler.handleUserInput(screen, input, keyBindings);
				if (firstHandled.isEmpty()) {
					inputHandler.handleMouseClickedOut(input.getKey());
				}
			} else {
				inputHandler.handleMouseClickedOut(input.getKey());
			}
		}
		return firstHandled;
	}

	@Override
	public void handleMouseClickedOut(InputConstants.Key key) {
		for (IUserInputHandler inputHandler : this.inputHandlers) {
			inputHandler.handleMouseClickedOut(key);
		}
	}

	@Override
	public Optional<IUserInputHandler> handleMouseScrolled(double mouseX, double mouseY, double scrollDelta) {
		return inputHandlers.stream()
			.flatMap(inputHandler -> inputHandler.handleMouseScrolled(mouseX, mouseY, scrollDelta).stream())
			.findFirst();
	}
}
