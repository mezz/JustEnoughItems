package mezz.jei.gui;

import mezz.jei.api.gui.IDrawableAnimated;
import mezz.jei.api.gui.IDrawableStatic;
import net.minecraft.client.Minecraft;

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
	public void draw(Minecraft minecraft, int xOffset, int yOffset, int maskTop, int maskBottom, int maskLeft, int maskRight) {
		// draws nothing
	}

	@Override
	public void draw(Minecraft minecraft) {
		// draws nothing
	}

	@Override
	public void draw(Minecraft minecraft, int xOffset, int yOffset) {
		// draws nothing
	}
}
