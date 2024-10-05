package mezz.jei.common.gui.elements;

import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import net.minecraft.client.gui.GuiGraphics;

import java.util.List;

public class DrawableCombined implements IDrawableAnimated {
	private final List<IDrawable> drawables;
	private final int width;
	private final int height;

	public DrawableCombined(IDrawable... drawables) {
		this(List.of(drawables));
	}

	public DrawableCombined(List<IDrawable> drawables) {
		IDrawable first = drawables.getFirst();
		this.width = first.getWidth();
		this.height = first.getHeight();
		for (int i = 1; i < drawables.size(); i++) {
			IDrawable drawable = drawables.get(i);
			if (drawable.getWidth() != width || drawable.getHeight() != height) {
				throw new IllegalArgumentException("Drawables must have the same width and height. Expected " + width + " x " + height + " but got " + drawable.getWidth() + " x " + drawable.getHeight());
			}
		}
		this.drawables = drawables;
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
	public void draw(GuiGraphics guiGraphics) {
		for (IDrawable drawable : drawables) {
			drawable.draw(guiGraphics);
		}
	}

	@Override
	public void draw(GuiGraphics guiGraphics, int xOffset, int yOffset) {
		for (IDrawable drawable : drawables) {
			drawable.draw(guiGraphics, xOffset, yOffset);
		}
	}
}
