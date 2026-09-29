package mezz.jei.common.input.interaction;

import mezz.jei.common.input.IMouseOverable;
import mezz.jei.common.input.UserInput;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * An interaction that runs an action chosen when a key or mouse button is pressed.
 * Before running, it checks whether the action is still allowed.
 *
 * <p>For example, a player clicks cobblestone to view its recipes. The action remembers
 * cobblestone when the player presses the mouse button and shows its recipes when
 * the player lets go, provided the click is still allowed.</p>
 *
 * @param action the action to run, receiving the release event for a mouse click
 *               or the original press event for a keyboard shortcut
 * @param canExecute checks whether the action is still allowed to run; returning false
 *                   skips the action
 */
public record InputAction(Consumer<UserInput> action, Predicate<UserInput> canExecute) implements IInputInteraction {
	public InputAction(Consumer<UserInput> action) {
		this(action, input -> true);
	}

	public static InputAction run(Runnable action) {
		return new InputAction(input -> action.run());
	}

	/** Requires the pointer to be over the given area when the action runs. */
	public InputAction within(IMouseOverable area) {
		return new InputAction(action, input -> canExecute.test(input) && area.isMouseOver(input.getMouseX(), input.getMouseY()));
	}

	/** Checks a condition before running, such as whether a button is still visible or enabled. */
	public InputAction when(BooleanSupplier available) {
		return new InputAction(action, input -> canExecute.test(input) && available.getAsBoolean());
	}

	@Override
	public void complete(UserInput input) {
		if (canExecute.test(input)) {
			action.accept(input);
		}
	}
}
