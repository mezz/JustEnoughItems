package mezz.jei.gui.elements;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;

/**
 * Breaks a texture into 9 pieces so that it can be scaled to any size.
 * Draws the corners and then repeats any middle textures to fill the remaining area.
 */
public class DrawableNineSliceTexture {
	private final ResourceLocation resourceLocation;
	private final int u;
	private final int v;
	private final int width;
	private final int height;
	private final int scale;
	private final int leftWidth;
	private final int rightWidth;
	private final int topHeight;
	private final int bottomHeight;
	private final int textureWidth;
	private final int textureHeight;

	public DrawableNineSliceTexture(ResourceLocation resourceLocation, int u, int v, int width, int height, int scale, int leftWidth, int rightWidth, int topHeight, int bottomHeight, int textureWidth, int textureHeight) {
		this.resourceLocation = resourceLocation;
		this.u = u / scale;
		this.v = v / scale;
		this.width = width / scale;
		this.height = height / scale;
		this.scale = scale;
		this.leftWidth = leftWidth;
		this.rightWidth = rightWidth;
		this.topHeight = topHeight;
		this.bottomHeight = bottomHeight;
		this.textureWidth = textureWidth;
		this.textureHeight = textureHeight;
	}

	public void draw(Minecraft minecraft, int xOffset, int yOffset, int width, int height) {
		TextureManager textureManager = minecraft.getTextureManager();
		textureManager.bindTexture(resourceLocation);

		final int uMiddle = u + leftWidth;
		final int uRight = u + this.width - rightWidth;
		final int vMiddle = v + topHeight;
		final int vBottom = v + this.height - bottomHeight;
		final int middleWidth = uRight - uMiddle;
		final int middleHeight = vBottom - vMiddle;

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferBuilder = tessellator.getBuffer();
		bufferBuilder.begin(7, DefaultVertexFormats.POSITION_TEX);

		// left top
		draw(bufferBuilder, u, v, leftWidth, topHeight, xOffset, yOffset);
		// left bottom
		draw(bufferBuilder, u, vBottom, leftWidth, bottomHeight, xOffset, yOffset + height - bottomHeight);
		// right top
		draw(bufferBuilder, uRight, v, rightWidth, topHeight, xOffset + width - rightWidth, yOffset);
		// right bottom
		draw(bufferBuilder, uRight, vBottom, rightWidth, bottomHeight, xOffset + width - rightWidth, yOffset + height - bottomHeight);

		final int tiledMiddleWidth = width - leftWidth - rightWidth;
		final int tiledMiddleHeight = height - topHeight - bottomHeight;
		if (tiledMiddleWidth > 0) {
			// top edge
			drawTiled(bufferBuilder, uMiddle, v, middleWidth, topHeight, xOffset + leftWidth, yOffset, tiledMiddleWidth, topHeight);
			// bottom edge
			drawTiled(bufferBuilder, uMiddle, vBottom, middleWidth, bottomHeight, xOffset + leftWidth, yOffset + height - bottomHeight, tiledMiddleWidth, bottomHeight);
		}
		if (tiledMiddleHeight > 0) {
			// left side
			drawTiled(bufferBuilder, u, vMiddle, leftWidth, middleHeight, xOffset, yOffset + topHeight, leftWidth, tiledMiddleHeight);
			// right side
			drawTiled(bufferBuilder, uRight, vMiddle, rightWidth, middleHeight, xOffset + width - rightWidth, yOffset + topHeight, rightWidth, tiledMiddleHeight);
		}
		if (tiledMiddleHeight > 0 && tiledMiddleWidth > 0) {
			// middle area
			drawTiled(bufferBuilder, uMiddle, vMiddle, middleWidth, middleHeight, xOffset + leftWidth, yOffset + topHeight, tiledMiddleWidth, tiledMiddleHeight);
		}

		tessellator.draw();
	}

	private void draw(BufferBuilder bufferBuilder, int u, int v, int width, int height, int xOffset, int yOffset) {
		double widthScale = scale / (double) (textureWidth);
		double heightScale = scale / (double) (textureHeight);
		double u1 = u * widthScale;
		double v1 = (v + height) * heightScale;
		double u2 = (u + width) * widthScale;
		double v2 = v * heightScale;

		bufferBuilder.pos(xOffset, yOffset + height, 0)
			.tex(u1, v1)
			.endVertex();
		bufferBuilder.pos(xOffset + width, yOffset + height, 0)
			.tex(u2, v1)
			.endVertex();
		bufferBuilder.pos(xOffset + width, yOffset, 0)
			.tex(u2, v2)
			.endVertex();
		bufferBuilder.pos(xOffset, yOffset, 0)
			.tex(u1, v2)
			.endVertex();
	}

	private void drawTiled(BufferBuilder bufferBuilder, int u, int v, int width, int height, int xOffset, int yOffset, int tiledWidth, int tiledHeight) {
		final int xTileCount = tiledWidth / width;
		final int xRemainder = tiledWidth - (xTileCount * width);
		final int yTileCount = tiledHeight / height;
		final int yRemainder = tiledHeight - (yTileCount * height);

		final int yStart = yOffset + tiledHeight;

		for (int xTile = 0; xTile <= xTileCount; xTile++) {
			for (int yTile = 0; yTile <= yTileCount; yTile++) {
				int tileWidth = (xTile == xTileCount) ? xRemainder : width;
				int tileHeight = (yTile == yTileCount) ? yRemainder : height;
				int x = xOffset + (xTile * width);
				int y = yStart - ((yTile + 1) * height);
				if (tileWidth > 0 && tileHeight > 0) {
					int maskTop = height - tileHeight;
					int maskRight = width - tileWidth;
					draw(bufferBuilder, u, v + maskTop, width - maskRight, height - maskTop, x, y + maskTop);
				}
			}
		}
	}
}
