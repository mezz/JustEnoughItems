package mezz.jei.common.gui.elements;

import com.google.common.base.Preconditions;
import mezz.jei.api.gui.drawable.IDrawable;
import net.minecraft.client.gui.GuiGraphics;

/**
 * Draws an icon at a higher resolution than normal (determined by the scale parameter).
 */
public class HighResolutionDrawable implements IDrawable {
	private final IDrawable drawable;
	private final int scale;

	public HighResolutionDrawable(IDrawable drawable, int scale) {
		int width = drawable.getWidth();
		int height = drawable.getHeight();
		Preconditions.checkArgument(
			width % scale == 0,
			String.format("drawable width %s must be divisible by the scale %s", width, scale)
		);
		Preconditions.checkArgument(
			height % scale == 0,
			String.format("drawable height %s must be divisible by the scale %s", height, scale)
		);

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
	public void draw(GuiGraphics guiGraphics, int xOffset, int yOffset) {
		var poseStack = guiGraphics.pose();
		poseStack.pushPose();
		{
			poseStack.translate(xOffset, yOffset, 0);
			poseStack.scale(1 / (float) scale, 1 / (float) scale, 1);
			this.drawable.draw(guiGraphics);
		}
		poseStack.popPose();
	}

	@Override
	public void draw(GuiGraphics guiGraphics) {
		var poseStack = guiGraphics.pose();
		poseStack.pushPose();
		{
			poseStack.scale(1 / (float) scale, 1 / (float) scale, 1);
			this.drawable.draw(guiGraphics);
		}
		poseStack.popPose();
	}
}
