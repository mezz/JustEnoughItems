package mezz.jei.common.input.handlers;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.common.input.IKeyBindings;
import mezz.jei.common.input.UserInput;
import mezz.jei.common.input.IUserInputHandler;
import net.minecraft.client.gui.screens.Screen;

import org.jetbrains.annotations.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class CombinedInputHandler implements IUserInputHandler {
	private final List<IUserInputHandler> inputHandlers;
	private final Map<InputConstants.Key, IUserInputHandler> mousedDown = new HashMap<>();
	@Nullable
	private IUserInputHandler dragStarted;

	public CombinedInputHandler(IUserInputHandler... inputHandlers) {
		this.inputHandlers = List.of(inputHandlers);
	}

	public CombinedInputHandler(List<IUserInputHandler> inputHandlers) {
		this.inputHandlers = List.copyOf(inputHandlers);
	}

	@Override
	public Optional<IUserInputHandler> handleUserInput(Screen screen, UserInput input, IKeyBindings keyBindings) {
		return switch (input.getClickState()) {
			case IMMEDIATE -> handleImmediateClick(screen, input, keyBindings);
			case SIMULATE -> handleSimulateClick(screen, input, keyBindings);
			case EXECUTE -> handleExecuteClick(screen, input, keyBindings);
		};
	}

	/*
	 * A vanilla click or key-down will be handled immediately.
	 * We do not track the mousedDown for it,
	 * the first handler to use it will be the "winner", the rest will get a clicked-out.
	 */
	private Optional<IUserInputHandler> handleImmediateClick(Screen screen, UserInput input, IKeyBindings keyBindings) {
		this.mousedDown.remove(input.getKey());

		return handleClickInternal(screen, input, keyBindings)
			.map(handled -> this);
	}

	/*
	 * For JEI-controlled clicks.
	 * JEI activates clicks when the player clicks down on it and releases the mouse on the same element.
	 *
	 * In the first click pass, it is a "simulate" to check if the handler can handle the click,
	 * and it will be added to mousedDown.
	 * In the second pass, all handlers that were in mousedDown will be sent the real click.
	 */
	private Optional<IUserInputHandler> handleSimulateClick(Screen screen, UserInput input, IKeyBindings keyBindings) {
		this.mousedDown.remove(input.getKey());

		return handleClickInternal(screen, input, keyBindings)
			.map(handled -> {
				this.mousedDown.put(input.getKey(), handled);
				return this;
			});
	}

	private Optional<IUserInputHandler> handleExecuteClick(Screen screen, UserInput input, IKeyBindings keyBindings) {
		return Optional.ofNullable(this.mousedDown.remove(input.getKey()))
			.map(inputHandler -> inputHandler.handleUserInput(screen, input, keyBindings))
			.map(handled -> this);
	}

	/**
	 * Calls handleClick on each mouse handler until one handles the click (returns non-null).
	 *
	 * handleMouseClickedOut will be called on:
	 * 1. every mouse handler that fails to handleClick (returned null).
	 * 2. every mouse handler that never got a chance to handleClick because something else handled it first.
	 */
	private Optional<IUserInputHandler> handleClickInternal(Screen screen, UserInput input, IKeyBindings keyBindings) {
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
		this.mousedDown.remove(key);
		for (IUserInputHandler inputHandler : this.inputHandlers) {
			inputHandler.handleMouseClickedOut(key);
		}
	}

	public void handleGuiChange() {
		Set<InputConstants.Key> keys = Set.copyOf(this.mousedDown.keySet());
		for (InputConstants.Key key : keys) {
			handleMouseClickedOut(key);
		}
		handleDragCanceled();
	}

	@Override
	public boolean handleMouseScrolled(double mouseX, double mouseY, double scrollDelta) {
		return inputHandlers.stream()
			.anyMatch(inputHandler -> inputHandler.handleMouseScrolled(mouseX, mouseY, scrollDelta));
	}

	@Override
	public Optional<IUserInputHandler> handleDragStart(Screen screen, UserInput input) {
		if (this.dragStarted != null) {
			this.dragStarted.handleDragCanceled();
		}

		Optional<IUserInputHandler> dragStarted = this.inputHandlers.stream()
			.map(i -> i.handleDragStart(screen, input))
			.flatMap(Optional::stream)
			.findFirst();

		this.dragStarted = dragStarted
			.orElse(null);

		return dragStarted
			.map(started -> this);
	}

	@Override
	public Optional<IUserInputHandler> handleDragComplete(Screen screen, UserInput input) {
		if (this.dragStarted == null) {
			return Optional.empty();
		}
		this.dragStarted.handleDragComplete(screen, input);
		this.dragStarted = null;
		return Optional.of(this);
	}

	@Override
	public void handleDragCanceled() {
		if (this.dragStarted != null) {
			this.dragStarted.handleDragCanceled();
			this.dragStarted = null;
		}
	}
}
