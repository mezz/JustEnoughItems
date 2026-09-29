package mezz.jei.gui.input;

import mezz.jei.api.runtime.IJeiKeyMapping;
import mezz.jei.common.config.DebugConfig;
import mezz.jei.common.input.UserInput;
import mezz.jei.common.input.interaction.IInputInteraction;
import mezz.jei.common.input.interaction.InputAction;
import net.minecraft.client.gui.screens.Screen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;

/**
 * Shortcuts tried in the order they were added. The first matching shortcut that can
 * handle the input wins. The same rules apply to keyboard and mouse bindings.
 * {@link GuiInputController} decides which collection of shortcuts to try first.
 *
 * <p>For example, if a player binds two shortcuts in this collection to the same key,
 * the one added first gets the first chance to handle it. The second can run only if
 * the first cannot handle that input, such as when it needs an ingredient under the
 * mouse and there is none.</p>
 */
public final class InputCommands {
	private static final Logger LOGGER = LogManager.getLogger();

	/** Whether a shortcut requires the mouse pointer to be over its input area, for either keyboard or mouse bindings. */
	public enum Scope {
		/**
		 * Works anywhere on a screen where this collection's shortcuts are available.
		 * For example, a player can use the Next Recipe Page shortcut with the mouse pointer
		 * outside the recipe display.
		 */
		SCREEN,
		/**
		 * Works only when this shortcut's input area is the frontmost one under the mouse pointer.
		 * For example, the player must move the mouse over the ingredient list to use its
		 * Quick Move shortcut on an ingredient there.
		 */
		HOVERED
	}

	/** Shortcuts in the order they are tried. */
	private final List<Command> commands = new ArrayList<>();
	/** Screens where any of this collection's shortcuts may run. */
	private final Predicate<Screen> available;

	public InputCommands() {
		this(screen -> true);
	}

	public InputCommands(Predicate<Screen> available) {
		this.available = available;
	}

	public void add(String name, Predicate<UserInput> matches, BiFunction<Screen, UserInput, Optional<IInputInteraction>> prepare) {
		add(Scope.SCREEN, name, matches, prepare);
	}

	public void add(Scope scope, String name, Predicate<UserInput> matches, BiFunction<Screen, UserInput, Optional<IInputInteraction>> prepare) {
		commands.add(new Command(scope, name, matches, prepare));
	}

	public void add(String name, IJeiKeyMapping key, BooleanSupplier enabled, Runnable action) {
		add(name, input -> input.is(key) && enabled.getAsBoolean(),
			(screen, input) -> Optional.of(InputAction.run(action).when(enabled)));
	}

	public Optional<IInputInteraction> resolve(Screen screen, UserInput input, boolean hovered) {
		if (!available.test(screen)) {
			return Optional.empty();
		}
		for (Command command : commands) {
			if ((hovered || command.scope() == Scope.SCREEN) && command.matches().test(input)) {
				Optional<IInputInteraction> action = command.prepare().apply(screen, input);
				if (action.isPresent()) {
					if (DebugConfig.isDebugInputsEnabled()) {
						LOGGER.debug("JEI command {} ({})", command.name(), command.scope());
					}
					return action;
				}
			}
		}
		return Optional.empty();
	}

	/**
	 * A shortcut that selects its action when its key or mouse button is pressed.
	 *
	 * @param scope whether this shortcut's input area must be under the mouse pointer
	 * @param name shortcut name used in debug logs
	 * @param matches checks the binding and any conditions needed to recognize the press
	 * @param prepare creates an interaction and remembers any needed data, such as the ingredient
	 *                under the pointer; returning empty lets the next shortcut try
	 */
	private record Command(Scope scope, String name, Predicate<UserInput> matches, BiFunction<Screen, UserInput, Optional<IInputInteraction>> prepare) {
	}
}
