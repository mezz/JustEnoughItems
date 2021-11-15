package mezz.jei.input.mouse;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.input.UserInput;
import net.minecraft.client.gui.screens.Screen;

import javax.annotation.Nullable;

public interface IUserInputHandler {
	@Nullable
	default IUserInputHandler handleUserInput(Screen screen, UserInput input) {
		return null;
	}

	/**
	 * Called when a mouse is clicked but was handled and canceled by some other mouse handler.
	 */
	default void handleMouseClickedOut(InputConstants.Key key) {

	}

	default boolean handleMouseScrolled(double mouseX, double mouseY, double scrollDelta) {
		return false;
	}

	@Nullable
	default IUserInputHandler handleDragStart(Screen screen, UserInput input) {
		return null;
	}

	@Nullable
	default IUserInputHandler handleDragComplete(Screen screen, UserInput input) {
		return null;
	}

	default void handleDragCanceled() {

	}
}
