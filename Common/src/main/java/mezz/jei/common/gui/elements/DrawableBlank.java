package mezz.jei.common.gui.elements;

import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.drawable.IScalableDrawable;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public record DrawableBlank(int width, int height) implements IDrawableStatic, IDrawableAnimated, IScalableDrawable {
	public static final DrawableBlank EMPTY = new DrawableBlank(0, 0);

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public void draw(GuiGraphicsExtractor guiGraphics, int xOffset, int yOffset, int maskTop, int maskBottom, int maskLeft, int maskRight) {
		// draws nothing
	}

	@Override
	public void draw(GuiGraphicsExtractor guiGraphics, int xOffset, int yOffset) {
		// draws nothing
	}

	@Override
	public void draw(GuiGraphicsExtractor guiGraphics, int x, int y, int width, int height) {
		// draws nothing
	}
}
