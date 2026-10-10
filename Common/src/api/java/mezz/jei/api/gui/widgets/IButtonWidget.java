package mezz.jei.api.gui.widgets;

import mezz.jei.api.gui.drawable.IDrawable;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.ApiStatus;

/**
 * Configures a recipe button.
 *
 * @param <THIS> the concrete button interface, for chaining configuration methods
 *
 * @since 30.43.0
 */
@ApiStatus.NonExtendable
public interface IButtonWidget<THIS extends IButtonWidget<THIS>> extends IRecipeWidgetBuilder<THIS> {
	/**
	 * Set fixed text displayed on the button, replacing its current contents.
	 * If the button's text is too long and gets truncated, its full text will be shown first in the
	 * tooltip, followed by any content configured with a {@link #setTooltip} overload.
	 *
	 * @param message the text to display
	 *
	 * @since 30.43.0
	 */
	THIS setMessage(Component message);

	/**
	 * Set a fixed icon displayed on the button, replacing its current contents.
	 * The icon is centered without scaling.
	 *
	 * @param icon the drawable to display
	 *
	 * @since 30.43.0
	 */
	THIS setIcon(IDrawable icon);

	/**
	 * Set whether the button is active.
	 * Inactive buttons are drawn disabled and do not receive input.
	 * Defaults to true.
	 *
	 * @param active true to make the button active
	 *
	 * @since 30.43.0
	 */
	THIS setActive(boolean active);

	/**
	 * Set whether the button is visible.
	 * Invisible buttons are not drawn and do not receive input or display tooltips.
	 * Defaults to true.
	 *
	 * @param visible true to make the button visible
	 *
	 * @since 30.43.0
	 */
	THIS setVisible(boolean visible);
}
