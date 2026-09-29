package mezz.jei.common.input;

import com.mojang.blaze3d.vertex.PoseStack;

/**
 * Input handling and drawing for an area in front of other controls, such as a recipe tooltip.
 */
public interface IGuiInputLayer extends IInputTarget, IMouseOverable {
	/**
	 * Updates the layer for the current mouse position.
	 * Call before checking whether the pointer is over the layer or drawing it.
	 */
	default void update(double mouseX, double mouseY) {

	}

	void draw(PoseStack poseStack, int mouseX, int mouseY);
}
