package mezz.jei.common.input;

import net.minecraft.client.gui.screens.Screen;

import java.util.Optional;

/** A control that returns the handler for the rest of an accepted click. */
public interface IInputTarget {
	Optional<IUserInputHandler> beginInput(Screen screen, UserInput input, IInternalKeyMappings keys);

	default void resetInput() {
	}
}
