package mezz.jei.gui.elements;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableStatic;

public class DrawableBlank implements IDrawableStatic, IDrawableAnimated {
	private final int width;
	private final int height;

	public DrawableBlank(int width, int height) {
		this.width = width;
		this.height = height;
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public void draw(PoseStack poseStack, int xOffset, int yOffset, int maskTop, int maskBottom, int maskLeft, int maskRight) {
		// draws nothing
	}

	@Override
	public void draw(PoseStack poseStack, int xOffset, int yOffset) {
		// draws nothing
	}
}
