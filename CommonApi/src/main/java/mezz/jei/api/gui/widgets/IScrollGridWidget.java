package mezz.jei.api.gui.widgets;

import mezz.jei.api.gui.placement.IPlaceable;
import net.minecraft.client.gui.navigation.ScreenRectangle;

/**
 * A scrolling area for ingredients with a scrollbar.
 * Modeled after the vanilla creative menu.
 *
 * Create one with {@link IRecipeExtrasBuilder#addScrollGridWidget}.
 * @since 15.20.3
 */
public interface IScrollGridWidget extends ISlottedRecipeWidget, IPlaceable<IScrollGridWidget> {
	/**
	 * Get the position and size of this widget, relative to its parent element.
	 *
	 * @since 15.20.3
	 */
	ScreenRectangle getScreenRectangle();
}
