package mezz.jei.common.input;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.screens.Screen;

import java.util.Optional;

public interface IUserInputHandler {
	default Optional<IUserInputHandler> handleUserInput(Screen screen, UserInput input, IInternalKeyMappings keyBindings) {
		return Optional.empty();
	}

	/**
	 * Called when a mouse is clicked but was handled and canceled by some other mouse handler.
	 */
	default void handleMouseClickedOut(InputConstants.Key key) {

	}

	default boolean handleMouseScrolled(double mouseX, double mouseY, double scrollDelta) {
		return false;
	}

	default Optional<IUserInputHandler> handleDragStart(Screen screen, UserInput input) {
		return Optional.empty();
	}

	default Optional<IUserInputHandler> handleDragComplete(Screen screen, UserInput input) {
		return Optional.empty();
	}

	default void handleDragCanceled() {

	}
}
