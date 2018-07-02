package mezz.jei.gui.elements;

import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IDrawableStatic;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

/**
 * Breaks a texture into 9 pieces so that it can be scaled to any size.
 * Draws the corners and then repeats any middle textures to fill the remaining area.
 */
public class DrawableNineSliceTexture implements IDrawable {
	private final IDrawableStatic leftTop;
	private final IDrawableStatic leftMiddle;
	private final IDrawableStatic leftBottom;
	private final IDrawableStatic middleTop;
	private final IDrawableStatic middleMiddle;
	private final IDrawableStatic middleBottom;
	private final IDrawableStatic rightTop;
	private final IDrawableStatic rightMiddle;
	private final IDrawableStatic rightBottom;
	private int width;
	private int height;

	public DrawableNineSliceTexture(ResourceLocation resourceLocation, int u, int v, int width, int height, int leftWidth, int rightWidth, int topHeight, int bottomHeight) {
		final int uMiddle = u + leftWidth;
		final int uRight = u + width - rightWidth;
		final int vMiddle = v + topHeight;
		final int vBottom = v + height - bottomHeight;

		final int middleWidth = uRight - uMiddle;
		final int middleHeight = vBottom - vMiddle;

		this.leftTop = new DrawableResource(resourceLocation, u, v, leftWidth, topHeight);
		this.leftMiddle = new DrawableResource(resourceLocation, u, vMiddle, leftWidth, middleHeight);
		this.leftBottom = new DrawableResource(resourceLocation, u, vBottom, leftWidth, bottomHeight);
		this.middleTop = new DrawableResource(resourceLocation, uMiddle, v, middleWidth, topHeight);
		this.middleMiddle = new DrawableResource(resourceLocation, uMiddle, vMiddle, middleWidth, middleHeight);
		this.middleBottom = new DrawableResource(resourceLocation, uMiddle, vBottom, middleWidth, bottomHeight);
		this.rightTop = new DrawableResource(resourceLocation, uRight, v, rightWidth, topHeight);
		this.rightMiddle = new DrawableResource(resourceLocation, uRight, vMiddle, rightWidth, middleHeight);
		this.rightBottom = new DrawableResource(resourceLocation, uRight, vBottom, rightWidth, bottomHeight);

		this.width = width;
		this.height = height;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public void setHeight(int height) {
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
	public void draw(Minecraft minecraft, int xOffset, int yOffset) {
		// corners first
		this.leftTop.draw(minecraft, xOffset,  yOffset);
		this.leftBottom.draw(minecraft, xOffset, yOffset + height - this.leftBottom.getHeight());
		this.rightTop.draw(minecraft, xOffset + width - this.rightTop.getWidth(), yOffset);
		this.rightBottom.draw(minecraft, xOffset + width - this.rightBottom.getWidth(), yOffset + height - this.rightBottom.getHeight());

		// fill in the remaining areas
		final int leftWidth = this.leftTop.getWidth();
		final int rightWidth = this.rightTop.getWidth();
		final int middleWidth = width - leftWidth - rightWidth;
		final int topHeight = this.leftTop.getHeight();
		final int bottomHeight = this.leftBottom.getHeight();
		final int middleHeight = height - topHeight - bottomHeight;
		if (middleWidth > 0) {
			drawTiled(minecraft, xOffset + leftWidth, yOffset, middleWidth, topHeight, this.middleTop);
			drawTiled(minecraft, xOffset + leftWidth, yOffset + height - this.leftBottom.getHeight(), middleWidth, bottomHeight, this.middleBottom);
		}
		if (middleHeight > 0) {
			drawTiled(minecraft, xOffset, yOffset + topHeight, leftWidth, middleHeight, this.leftMiddle);
			drawTiled(minecraft, xOffset + width - this.rightTop.getWidth(), yOffset + topHeight, rightWidth, middleHeight, this.rightMiddle);
		}
		if (middleHeight > 0 && middleWidth > 0) {
			drawTiled(minecraft, xOffset + leftWidth, yOffset + topHeight, middleWidth, middleHeight, this.middleMiddle);
		}
	}

	private void drawTiled(Minecraft minecraft, final int xOffset, final int yOffset, final int tiledWidth, final int tiledHeight, IDrawableStatic drawable) {
		final int xTileCount = tiledWidth / drawable.getWidth();
		final int xRemainder = tiledWidth - (xTileCount * drawable.getWidth());
		final int yTileCount = tiledHeight / drawable.getHeight();
		final int yRemainder = tiledHeight - (yTileCount * drawable.getHeight());

		final int yStart = yOffset + tiledHeight;

		for (int xTile = 0; xTile <= xTileCount; xTile++) {
			for (int yTile = 0; yTile <= yTileCount; yTile++) {
				int width = (xTile == xTileCount) ? xRemainder : drawable.getWidth();
				int height = (yTile == yTileCount) ? yRemainder : drawable.getHeight();
				int x = xOffset + (xTile * drawable.getWidth());
				int y = yStart - ((yTile + 1) * drawable.getHeight());
				if (width > 0 && height > 0) {
					int maskTop = drawable.getHeight() - height;
					int maskRight = drawable.getWidth() - width;

					drawable.draw(minecraft, x, y, maskTop, 0, 0, maskRight);
				}
			}
		}
	}
}
