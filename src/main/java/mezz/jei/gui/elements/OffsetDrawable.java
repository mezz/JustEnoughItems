package mezz.jei.gui.elements;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.drawable.IDrawable;

/**
 * Draws with a built-in offset.
 */
public class OffsetDrawable implements IDrawable {
	public static IDrawable create(IDrawable drawable, int xOffset, int yOffset) {
		if (xOffset == 0 && yOffset == 0) {
			return drawable;
		}
		return new OffsetDrawable(drawable, xOffset, yOffset);
	}

	private final IDrawable drawable;
	private final int xOffset;
	private final int yOffset;

	private OffsetDrawable(IDrawable drawable, int xOffset, int yOffset) {
		this.drawable = drawable;
		this.xOffset = xOffset;
		this.yOffset = yOffset;
	}

	@Override
	public int getWidth() {
		return drawable.getWidth();
	}

	@Override
	public int getHeight() {
		return drawable.getHeight();
	}

	@Override
	public void draw(PoseStack poseStack, int xOffset, int yOffset) {
		this.drawable.draw(
			poseStack,
			this.xOffset + xOffset,
			this.yOffset + yOffset
		);
	}

	@Override
	public void draw(PoseStack poseStack) {
		this.drawable.draw(poseStack, this.xOffset, this.yOffset);
	}
}
