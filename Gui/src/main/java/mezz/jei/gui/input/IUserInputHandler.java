package mezz.jei.gui.input;

import mezz.jei.api.gui.handlers.IGuiProperties;
import mezz.jei.common.input.IInternalKeyMappings;
import net.minecraft.client.gui.screens.Screen;

import java.util.Optional;

public interface IUserInputHandler {
	Optional<IUserInputHandler> handleUserInput(Screen screen, IGuiProperties guiProperties, UserInput input, IInternalKeyMappings keyBindings);

	/**
	 * Called when a mouse is clicked but was handled and canceled by some other mouse handler.
	 */
	default void unfocus() {

	}

	default Optional<IUserInputHandler> handleMouseScrolled(double mouseX, double mouseY, double scrollDeltaX, double scrollDeltaY) {
		return Optional.empty();
	}
}
