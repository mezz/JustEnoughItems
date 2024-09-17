package mezz.jei.api.gui.drawable;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.Rect2i;

/**
 * Similar to {@link IDrawable} but it can scale to any size.
 * Used for backgrounds of UIs and other things that have dynamic size.
 *
 * @since 11.7.0
 */
public interface IScalableDrawable {
	/**
	 * Draw in the given area.
	 *
	 * @since 11.7.0
	 */
	void draw(PoseStack poseStack, int x, int y, int width, int height);

	/**
	 * Draw in the given area.
	 *
	 * @since 11.7.0
	 */
	default void draw(PoseStack poseStack, Rect2i area) {
		draw(poseStack, area.getX(), area.getY(), area.getWidth(), area.getHeight());
	}
}
