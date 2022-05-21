package mezz.jei.input;

import mezz.jei.input.click.MouseClickState;
import net.minecraft.client.gui.screen.Screen;

import javax.annotation.Nullable;

public interface IMouseHandler {
	@Nullable
	default IMouseHandler handleClick(Screen screen, double mouseX, double mouseY, int mouseButton, MouseClickState clickState) {
		return null;
	}

	/**
	 * Called when a mouse is clicked but was handled and canceled by some other mouse handler.
	 */
	default void handleMouseClickedOut(int mouseButton) {

	}

	default boolean handleMouseScrolled(double mouseX, double mouseY, double scrollDelta) {
		return false;
	}

}
