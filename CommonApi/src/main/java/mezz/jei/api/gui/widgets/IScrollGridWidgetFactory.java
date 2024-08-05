package mezz.jei.api.gui.widgets;

import mezz.jei.api.helpers.IGuiHelper;
import net.minecraft.client.gui.navigation.ScreenRectangle;

/**
 * A helper for drawing a grid of recipe ingredients in a scrolling box.
 *
 * Get an instance from {@link IGuiHelper#createScrollGridFactory(int, int)}
 *
 * @since 19.7.0
 */
public interface IScrollGridWidgetFactory<R> extends ISlottedWidgetFactory<R> {
	/**
	 * @since 19.7.0
	 */
	void setPosition(int x, int y);
	/**
	 * @since 19.7.0
	 */
	ScreenRectangle getArea();
}
