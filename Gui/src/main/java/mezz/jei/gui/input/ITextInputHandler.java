package mezz.jei.gui.input;

import mezz.jei.common.input.IInputTarget;
import mezz.jei.common.input.IMouseOverable;
import net.minecraft.client.input.CharacterEvent;

/**
 * A text control that receives keyboard input before other controls while it has focus.
 * For example, Backspace edits the search text before JEI tries a shortcut bound to that key.
 *
 * <p>Use this handler as its input area's {@link InputArea#area() area}. Releasing a mouse
 * button outside that area clears its focus. For example, clicking a search suggestion
 * keeps the search field selected, while clicking an ingredient clears focus on release.</p>
 */
public interface ITextInputHandler extends IInputTarget, IMouseOverable {
	/** Whether this control is available and selected for typing. */
	boolean hasKeyboardFocus();

	/** Stops editing this control, such as when the player clicks elsewhere or closes the screen. */
	void clearFocus();

	/**
	 * Handles typed text while this control has keyboard focus.
	 *
	 * @return whether the text was handled
	 */
	boolean onCharTyped(CharacterEvent event);
}
