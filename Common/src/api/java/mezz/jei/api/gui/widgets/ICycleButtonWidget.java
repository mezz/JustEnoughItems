package mezz.jei.api.gui.widgets;

import mezz.jei.api.gui.drawable.IDrawable;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Function;

/**
 * A recipe button that cycles through a list of values.
 *
 * Create one with {@link IRecipeExtrasBuilder#addCycleButton}.
 * Clicking the button cycles through the values in their supplied order, wrapping at either end.
 * Holding Shift cycles backward.
 *
 * @param <T> the type of values the button cycles through
 *
 * @since 19.65.0
 */
@ApiStatus.NonExtendable
public interface ICycleButtonWidget<T> extends IButtonWidget<ICycleButtonWidget<T>> {
	/**
	 * Set text for the selected value, replacing the button's current contents.
	 * Truncated text is shown in full before the configured tooltip.
	 *
	 * @param valueText supplies the selected value's text when rendering or building its tooltip
	 *
	 * @since 19.65.0
	 */
	ICycleButtonWidget<T> setMessage(Function<T, Component> valueText);

	/**
	 * Set an icon for the selected value, replacing the button's current contents.
	 * The icon is centered without scaling.
	 *
	 * @param valueIcon supplies the selected value's drawable when rendering
	 *
	 * @since 19.65.0
	 */
	ICycleButtonWidget<T> setIcon(Function<T, IDrawable> valueIcon);

	/**
	 * Get the selected value.
	 *
	 * @since 19.65.0
	 */
	T getValue();

	/**
	 * Set the selected value without calling the value-change callback.
	 *
	 * @param value the value to select
	 * @throws IllegalArgumentException if the value is not equal to one of the supplied values
	 *
	 * @since 19.65.0
	 */
	ICycleButtonWidget<T> setValue(T value);
}
