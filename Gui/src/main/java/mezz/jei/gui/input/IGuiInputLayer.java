package mezz.jei.gui.input;

import com.mojang.blaze3d.vertex.PoseStack;

/**
 * A foreground layer that owns its rendering, hit testing, and input handling.
 * While the mouse is over this layer, pointer input is captured so that it cannot reach obscured handlers below it.
 */
public interface IGuiInputLayer extends IUserInputHandler, IMouseOverable {
	void draw(PoseStack poseStack, int mouseX, int mouseY);
}
