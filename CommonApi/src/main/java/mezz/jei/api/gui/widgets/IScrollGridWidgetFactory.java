package mezz.jei.api.gui.widgets;

import mezz.jei.api.helpers.IGuiHelper;
import net.minecraft.client.gui.navigation.ScreenRectangle;

/**
 * A helper for drawing a grid of recipe ingredients in a scrolling box.
 *
 * Get an instance from {@link IGuiHelper#createScrollGridFactory(int, int)}
 *
 * @since 15.10.0
 * @deprecated use {@link IRecipeExtrasBuilder#addScrollGridWidget} instead, it's much simpler
 */
@SuppressWarnings({"DeprecatedIsStillUsed", "removal"})
@Deprecated(since = "15.20.0", forRemoval = true)
public interface IScrollGridWidgetFactory<R> extends ISlottedWidgetFactory<R> {
	/**
	 * @since 15.10.0
	 */
	void setPosition(int x, int y);
	/**
	 * @since 15.10.0
	 */
	ScreenRectangle getArea();
}
