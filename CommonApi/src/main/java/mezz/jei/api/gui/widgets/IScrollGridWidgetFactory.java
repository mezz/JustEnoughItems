package mezz.jei.api.gui.widgets;

import mezz.jei.api.helpers.IGuiHelper;
import net.minecraft.client.renderer.Rect2i;
import org.jetbrains.annotations.ApiStatus;

/**
 * A helper for drawing a grid of recipe ingredients in a scrolling box.
 *
 * Get an instance from {@link IGuiHelper#createScrollGridFactory(int, int)}
 *
 * @since 10.32.0
 * @deprecated use {@link IRecipeExtrasBuilder#addScrollGridWidget} instead, it's much simpler
 */
@SuppressWarnings({"DeprecatedIsStillUsed", "removal"})
@Deprecated(since = "10.53.0", forRemoval = true)
@ApiStatus.NonExtendable
public interface IScrollGridWidgetFactory<R> extends ISlottedWidgetFactory<R> {
	/**
	 * @since 10.32.0
	 */
	void setPosition(int x, int y);
	/**
	 * @since 10.32.0
	 */
	Rect2i getArea();
}
