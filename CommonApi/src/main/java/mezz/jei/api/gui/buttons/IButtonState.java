package mezz.jei.api.gui.buttons;

import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.inputs.IJeiUserInput;

/**
 * Mutable visual and interaction state for a button.
 *
 * <p>
 * Implementations are provided by JEI and are only valid during the
 * {@link IIconButtonController} callback in which they are received.
 * </p>
 *
 * @since 27.2.0
 */
public interface IButtonState {
	/**
	 * Sets the icon used to render the button.
	 *
	 * <p>
	 * By default, buttons have no icon, so this should usually
	 * be set in {@link IIconButtonController#initState(IButtonState)}
	 * </p>
	 *
	 * @param icon the drawable icon
	 *
	 * @since 27.2.0
	 */
	void setIcon(IDrawable icon);

	/**
	 * Sets whether the button is active.
	 *
	 * <p>
	 * Inactive buttons are rendered disabled and do not receive input.
	 * </p>
	 *
	 * <p>
	 * By default, buttons are active.
	 * </p>
	 *
	 * @param value true to make the button active
	 *
	 * @since 27.2.0
	 */
	void setActive(boolean value);

	/**
	 * Sets whether the button is visible.
	 *
	 * <p>
	 * Invisible buttons are not rendered and do not receive input.
	 * </p>
	 *
	 * <p>
	 * By default, buttons are visible.
	 * </p>
	 *
	 * @param value true to make the button visible
	 *
	 * @since 27.2.0
	 */
	void setVisible(boolean value);

	/**
	 * Forces the button to render in a pressed state.
	 *
	 * <p>
	 * This is intended for toggle-style buttons to indicate that the
	 * associated feature is currently enabled.
	 * </p>
	 *
	 * <p>
	 * This does not trigger {@link IIconButtonController#onPress(IJeiUserInput)}.
	 * </p>
	 *
	 * <p>
	 * By default, buttons are not forced pressed.
	 * </p>
	 *
	 * @param value true to force the pressed visual state
	 *
	 * @since 27.2.0
	 */
	void setForcePressed(boolean value);
}
