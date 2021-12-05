package mezz.jei.input.mouse.handlers;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.input.UserInput;
import mezz.jei.input.mouse.IUserInputHandler;
import net.minecraft.client.gui.screens.Screen;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CombinedUserInputHandler implements IUserInputHandler {
	private final List<IUserInputHandler> inputHandlers;
	private final Map<InputConstants.Key, IUserInputHandler> mousedDown = new HashMap<>();
	@Nullable
	private IUserInputHandler dragStarted;

	public CombinedUserInputHandler(IUserInputHandler... inputHandlers) {
		this.inputHandlers = List.of(inputHandlers);
	}

	public CombinedUserInputHandler(List<IUserInputHandler> inputHandlers) {
		this.inputHandlers = List.copyOf(inputHandlers);
	}

	@Override
	public IUserInputHandler handleUserInput(Screen screen, UserInput input) {
		return switch (input.getClickState()) {
			case IMMEDIATE -> handleImmediateClick(screen, input);
			case SIMULATE -> handleSimulateClick(screen, input);
			case EXECUTE -> handleExecuteClick(screen, input);
		};
	}

	/*
	 * A vanilla click or key-down will be handled immediately.
	 * We do not track the mousedDown for it,
	 * the first handler to use it will be the "winner", the rest will get a clicked-out.
	 */
	@Nullable
	private IUserInputHandler handleImmediateClick(Screen screen, UserInput input) {
		this.mousedDown.remove(input.getKey());

		IUserInputHandler handled = handleClickInternal(screen, input);
		if (handled == null) {
			return null;
		}
		return this;
	}

	/*
	 * For JEI-controlled clicks.
	 * JEI activates clicks when the player clicks down on it and releases the mouse on the same element.
	 *
	 * In the first click pass, it is a "simulate" to check if the handler can handle the click,
	 * and it will be added to mousedDown.
	 * In the second pass, all handlers that were in mousedDown will be sent the real click.
	 */
	@Nullable
	private IUserInputHandler handleSimulateClick(Screen screen, UserInput input) {
		this.mousedDown.remove(input.getKey());

		IUserInputHandler clickHandled = handleClickInternal(screen, input);
		if (clickHandled == null) {
			return null;
		}
		this.mousedDown.put(input.getKey(), clickHandled);
		return this;
	}

	@Nullable
	private IUserInputHandler handleExecuteClick(Screen screen, UserInput input) {
		IUserInputHandler inputHandler = this.mousedDown.remove(input.getKey());
		if (inputHandler == null) {
			return null;
		}
		IUserInputHandler handled = inputHandler.handleUserInput(screen, input);
		if (handled == null) {
			return null;
		}
		return this;
	}

	/**
	 * Calls handleClick on each mouse handler until one handles the click (returns non-null).
	 *
	 * handleMouseClickedOut will be called on:
	 * 1. every mouse handler that fails to handleClick (returned null).
	 * 2. every mouse handler that never got a chance to handleClick because something else handled it first.
	 */
	@Nullable
	private IUserInputHandler handleClickInternal(Screen screen, UserInput input) {
		IUserInputHandler firstHandled = null;
		for (IUserInputHandler inputHandler : this.inputHandlers) {
			if (firstHandled == null) {
				firstHandled = inputHandler.handleUserInput(screen, input);
				if (firstHandled == null) {
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
		Set<InputConstants.Key> keys = this.mousedDown.keySet();
		keys.forEach(this::handleMouseClickedOut);
		handleDragCanceled();
	}

	@Override
	public boolean handleMouseScrolled(double mouseX, double mouseY, double scrollDelta) {
		return inputHandlers.stream()
			.anyMatch(inputHandler -> inputHandler.handleMouseScrolled(mouseX, mouseY, scrollDelta));
	}

	@Nullable
	@Override
	public IUserInputHandler handleDragStart(Screen screen, UserInput input) {
		if (this.dragStarted != null) {
			this.dragStarted.handleDragCanceled();
		}

		this.dragStarted = this.inputHandlers.stream()
			.map(i -> i.handleDragStart(screen, input))
			.findFirst()
			.orElse(null);

		if (this.dragStarted == null) {
			return null;
		}
		return this;
	}

	@Nullable
	@Override
	public IUserInputHandler handleDragComplete(Screen screen, UserInput input) {
		if (this.dragStarted == null) {
			return null;
		}
		this.dragStarted.handleDragComplete(screen, input);
		this.dragStarted = null;
		return this;
	}

	@Override
	public void handleDragCanceled() {
		if (this.dragStarted != null) {
			this.dragStarted.handleDragCanceled();
			this.dragStarted = null;
		}
	}
}
