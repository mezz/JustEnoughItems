package mezz.jei.common.gui.elements;

import mezz.jei.api.gui.drawable.IDrawable;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * Draws an icon at a higher resolution than normal (determined by the scale parameter).
 */
public class HighResolutionDrawable implements IDrawable {
	private final IDrawable drawable;
	private final int scale;

	public HighResolutionDrawable(IDrawable drawable, int scale) {
		this.drawable = drawable;
		this.scale = scale;
	}

	@Override
	public int getWidth() {
		int width = drawable.getWidth();
		return width / scale;
	}

	@Override
	public int getHeight() {
		int height = drawable.getHeight();
		return height / scale;
	}

	@Override
	public void draw(GuiGraphicsExtractor guiGraphics, int xOffset, int yOffset) {
		var poseStack = guiGraphics.pose();
		poseStack.pushMatrix();
		{
			poseStack.translate(xOffset, yOffset);
			poseStack.scale(1 / (float) scale, 1 / (float) scale);
			this.drawable.draw(guiGraphics);
		}
		poseStack.popMatrix();
	}

	@Override
	public void draw(GuiGraphicsExtractor guiGraphics) {
		var poseStack = guiGraphics.pose();
		poseStack.pushMatrix();
		{
			poseStack.scale(1 / (float) scale, 1 / (float) scale);
			this.drawable.draw(guiGraphics);
		}
		poseStack.popMatrix();
	}
}
