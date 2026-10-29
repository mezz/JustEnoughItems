package mezz.jei.common.util;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fc;
import org.joml.Vector2f;

public final class GuiGraphicsUtil {
	private GuiGraphicsUtil() {
	}

	/**
	 * Returns the extractor's screen mouse position in its current pose's local coordinates.
	 */
	public static Vector2f getLocalMousePosition(GuiGraphicsExtractor guiGraphics) {
		return getLocalMousePosition(guiGraphics.pose(), guiGraphics.mouseX(), guiGraphics.mouseY());
	}

	/**
	 * Converts a screen mouse position to local coordinates without modifying the pose.
	 * Returns negative infinity on both axes for an off-screen mouse or a non-invertible pose.
	 */
	public static Vector2f getLocalMousePosition(Matrix3x2fc pose, double mouseX, double mouseY) {
		if (mouseX < 0 || mouseY < 0) {
			return new Vector2f(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY);
		}
		Matrix3x2f inverse = pose.invert(new Matrix3x2f());
		if (!inverse.isFinite()) {
			return new Vector2f(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY);
		}
		return inverse.transformPosition((float) mouseX, (float) mouseY, new Vector2f());
	}
}
