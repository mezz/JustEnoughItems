package mezz.jei.common.input;

import com.mojang.blaze3d.vertex.PoseStack;

/**
 * A foreground layer that owns its rendering, hit testing, and input handling.
 * While the mouse is over this layer, pointer input is captured so that it cannot reach obscured handlers below it.
 */
public interface IGuiInputLayer extends IUserInputHandler, IMouseOverable {
	/** Updates this layer before foreground tooltip hit testing and extraction. */
	default void update(double mouseX, double mouseY) {}

	void draw(PoseStack poseStack, int mouseX, int mouseY);
}
