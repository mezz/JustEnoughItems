package mezz.jei.common.input.interaction;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.common.input.IInputTarget;
import mezz.jei.common.input.UserInput;

/**
 * Handles an accepted key press, mouse click, or drag.
 * Mouse movement and release stay with this interaction even if the pointer leaves
 * the original control or a key binding changes.
 *
 * <p>For keyboard input, the controller calls {@link #complete} immediately.
 * Mouse input completes on release or is cancelled when the screen changes or
 * another press replaces the interaction.</p>
 *
 * <p>For example, a player holds the left mouse button on a scrollbar and moves the
 * mouse sideways, outside the scrollbar. Moving the mouse up or down still scrolls
 * the list until the player lets go of the button.</p>
 *
 * @see IInputTarget#beginInput
 * @see InputAction
 * @see MouseDrag
 */
public interface IInputInteraction {
	/**
	 * Finishes handling the input. A click action may be skipped if its original control
	 * is no longer available or the mouse button was released with the pointer outside it.
	 *
	 * @param input the mouse button release event, or the original key press event
	 */
	void complete(UserInput input);

	/**
	 * Called when the player moves the mouse while holding the button that began this interaction,
	 * including movement outside the control.
	 *
	 * @param mouseX current horizontal pointer position in GUI coordinates
	 * @param mouseY current vertical pointer position in GUI coordinates
	 * @param button the mouse button whose press began the interaction
	 * @param dragX horizontal movement since the previous drag event
	 * @param dragY vertical movement since the previous drag event
	 */
	default void drag(double mouseX, double mouseY, InputConstants.Key button, double dragX, double dragY) {
	}

	/**
	 * Whether this interaction prevents public recipe handlers from also receiving mouse movement.
	 * Scrollbars and resize handles return true. The default, false, lets recipe handlers
	 * under the pointer handle movement as well.
	 */
	default boolean capturesMouseMovement() {
		return false;
	}

	/**
	 * Clears pressed or dragging state without running the click action or dropping an ingredient.
	 * For example, if a player closes the inventory while dragging a bookmark, the bookmark
	 * goes back to its original place.
	 */
	default void cancel() {
	}

	/**
	 * Whether to keep this interaction when the same mouse button is reported pressed again
	 * before release. The default, false, cancels it and starts a new click.
	 *
	 * <p>For example, a player is dragging a bookmark while holding the left mouse button.
	 * If JEI receives a duplicate press event, returning true keeps that bookmark attached
	 * to the mouse until the player lets go.</p>
	 */
	default boolean keepsCaptureOnRepeatedPress() {
		return false;
	}
}
