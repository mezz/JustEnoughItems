package mezz.jei.gui.elements;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.drawable.IDrawable;

/**
 * Draws an icon at a higher resolution than normal (determined by the scale parameter).
 */
public class DrawableScaled implements IDrawable {
	private final IDrawable drawable;
	private final int scale;

	public DrawableScaled(IDrawable drawable, int scale) {
		this.drawable = drawable;
		this.scale = scale;
	}

	@Override
	public int getWidth() {
		return drawable.getWidth() / scale;
	}

	@Override
	public int getHeight() {
		return drawable.getHeight() / scale;
	}

	@Override
	public void draw(PoseStack poseStack, int xOffset, int yOffset) {
		poseStack.pushPose();
		{
			poseStack.translate(xOffset, yOffset, 0);
			poseStack.scale(1 / (float) scale, 1 / (float) scale, 1);
			this.drawable.draw(poseStack);
		}
		poseStack.popPose();
	}

	@Override
	public void draw(PoseStack poseStack) {
		poseStack.pushPose();
		{
			poseStack.scale(1 / (float) scale, 1 / (float) scale, 1);
			this.drawable.draw(poseStack);
		}
		poseStack.popPose();
	}
}
