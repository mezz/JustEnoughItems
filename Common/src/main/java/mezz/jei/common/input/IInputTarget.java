package mezz.jei.common.input;

import mezz.jei.common.input.interaction.IInputInteraction;
import net.minecraft.client.gui.screens.Screen;

import java.util.Optional;

/**
 * An input handler for a control, or an ordered group of controls, such as buttons or scrollbars.
 * This object decides whether to handle the press; the returned {@link IInputInteraction}
 * handles the rest of that particular click, key press, or drag.
 */
public interface IInputTarget {
	/**
	 * Chooses how to handle a key press or mouse button press.
	 * Keyboard actions run immediately; mouse click actions run on release.
	 *
	 * @param screen the screen receiving the press
	 * @param input the press event, including the pointer position at that time
	 * @param keys key bindings used by this control
	 * @return the accepted interaction, or empty to let another control or shortcut try
	 */
	Optional<IInputInteraction> beginInput(Screen screen, UserInput input, IInternalKeyMappings keys);

	default boolean scroll(double mouseX, double mouseY, double scrollX, double scrollY) {
		return false;
	}

	/** Clears state such as keyboard focus and pressed buttons when the screen closes or JEI restarts. */
	default void resetInput() {
	}
}
