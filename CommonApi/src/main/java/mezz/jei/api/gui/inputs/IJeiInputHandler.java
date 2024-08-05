package mezz.jei.api.gui.inputs;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.navigation.ScreenRectangle;

/**
 * An interface for things that want to receive user inputs like other JEI elements.
 * If you want to do vanilla-like input handling instead, use {@link IJeiGuiEventListener}.
 *
 * @since 15.9.0
 */
public interface IJeiInputHandler {
	/**
	 * Get the area covered by this handler relative to its parent element.
	 *
	 * Mouse coordinates passed to this handler are translated so that when
	 * the mouse is at this area's position, it is passed to this handler as if it were (0, 0).
	 *
	 * @since 15.9.0
	 */
	ScreenRectangle getArea();

	/**
	 * Called when a player clicks or presses a button while the mouse is over this element.
	 * Useful for implementing buttons, hyperlinks, and other interactions to your recipe.
	 *
	 * In short, to handle inputs correctly,
	 *  * when {@link IJeiUserInput#isSimulate()} is true, do not execute any action.
	 *  	Only return true or false for whether you can handle the input or not.
	 *  * when {@link IJeiUserInput#isSimulate()} is false, execute the action if possible.
	 *
	 * Other than immediacy of the execution, keys and mouse clicks are generally interchangeable in JEI.
	 * Something that bound to a key press by default could be remapped to a mouse button and the input handler works the same.
	 *
	 *
	 * Detailed info about how mouse clicks are handled:
	 *  JEI treats mouse-down as a {@link IJeiUserInput#isSimulate()} step,
	 *  so input handlers should return whether they can handle a click when {@link IJeiUserInput#isSimulate()} is true.
	 *  JEI input handlers should then execute the action on mouse-up, when {@link IJeiUserInput#isSimulate()} is false.
	 *
	 *  Done this way, a mouse must be clicked and unclicked on the same element in order to execute its action.
	 *  This allows players to accidentally click something, move the mouse away, and release the mouse elsewhere without
	 *  executing a click, which is similar to how most operating systems and programs handle mouse clicks.
	 *
	 *
	 * Detailed info about how key presses are handled:
	 *  JEI treats key-down as executing immediately with no simulate step.
	 *  JEI input handlers should execute the action when {@link IJeiUserInput#isSimulate()} is false.
	 *  Key up is ignored.
	 *
	 *  Done this way, a key press is treated as more intentional and immediate than a mouse click, so it doesn't
	 *  need a simulate step. The immediate handling of the key press will feel fast and responsive.
	 *
	 *
	 * @param mouseX    the X position of the mouse, relative to the parent element.
	 * @param mouseY    the Y position of the mouse, relative to the parent element.
	 * @param input the current input, it may be a click or a keyboard key
	 * @return true if the input was handled (or could be handled), false otherwise
	 *
	 * @since 15.9.0
	 */
	default boolean handleInput(double mouseX, double mouseY, IJeiUserInput input) {
		return false;
	}

	/**
	 * Called when a player scrolls the mouse on the widget.
	 * Useful for implementing scroll boxes, and other interactions.
	 *
	 * @param mouseX   the X position of the mouse, relative to the parent element.
	 * @param mouseY   the Y position of the mouse, relative to the parent element.
	 * @param scrollDelta the amount of vertical scrolling.
	 * @return true if the scrolling was handled, false otherwise.
	 *
	 * @since 15.9.0
	 */
	default boolean handleMouseScrolled(double mouseX, double mouseY, double scrollDelta) {
		return false;
	}

	/**
	 * Called when a player drags the mouse on the target.
	 * Useful for implementing scroll bars and other interactions.
	 *
	 * @param mouseX the X position of the mouse, relative to the parent element.
	 * @param mouseY the Y position of the mouse, relative to the parent element.
	 * @param mouseKey the currently pressed mouse key
	 * @param dragX the amount of horizontal dragging.
	 * @param dragY the amount of vertical dragging.
	 * @return true if the scrolling was handled, false otherwise.
	 *
	 * @since 15.9.0
	 */
	default boolean handleMouseDragged(double mouseX, double mouseY, InputConstants.Key mouseKey, double dragX, double dragY) {
		return false;
	}

	/**
	 * Called when the mouse is moved within the GUI element.
	 *
	 * @param mouseX the X position of the mouse, relative to the parent element.
	 * @param mouseY the Y position of the mouse, relative to the parent element.
	 *
	 * @since 15.9.0
	 */
	default void handleMouseMoved(double mouseX, double mouseY) {

	}
}
