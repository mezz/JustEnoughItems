package mezz.jei.api.gui.drawable;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;

/**
 * Similar to {@link IDrawable} but it can scale to any size.
 * Used for backgrounds of UIs and other things that have dynamic size.
 *
 * @since 19.4.0
 */
public interface IScalableDrawable {
	/**
	 * Draw in the given area.
	 *
	 * @since 19.4.0
	 */
	void draw(GuiGraphics guiGraphics, int x, int y, int width, int height);

	/**
	 * Draw in the given area.
	 *
	 * @since 19.4.0
	 */
	default void draw(GuiGraphics guiGraphics, Rect2i area) {
		draw(guiGraphics, area.getX(), area.getY(), area.getWidth(), area.getHeight());
	}
}
