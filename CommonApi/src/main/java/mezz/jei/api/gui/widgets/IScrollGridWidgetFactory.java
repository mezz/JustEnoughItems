package mezz.jei.api.gui.widgets;

import mezz.jei.api.helpers.IGuiHelper;
import net.minecraft.client.renderer.Rect2i;

/**
 * A helper for drawing a grid of recipe ingredients in a scrolling box.
 *
 * Get an instance from {@link IGuiHelper#createScrollGridFactory(int, int)}
 *
 * @since 11.32.0
 * @deprecated use {@link IRecipeExtrasBuilder#addScrollGridWidget} instead, it's much simpler
 */
@SuppressWarnings({"DeprecatedIsStillUsed", "removal"})
@Deprecated(since = "11.38.3", forRemoval = true)
public interface IScrollGridWidgetFactory<R> extends ISlottedWidgetFactory<R> {
	/**
	 * @since 11.32.0
	 */
	void setPosition(int x, int y);
	/**
	 * @since 11.32.0
	 */
	Rect2i getArea();
}
