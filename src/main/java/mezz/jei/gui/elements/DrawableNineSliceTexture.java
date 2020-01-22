package mezz.jei.gui.elements;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;

import mezz.jei.config.Constants;
import mezz.jei.gui.textures.JeiSpriteUploader;

/**
 * Breaks a texture into 9 pieces so that it can be scaled to any size.
 * Draws the corners and then repeats any middle textures to fill the remaining area.
 */
public class DrawableNineSliceTexture {
	private final JeiSpriteUploader spriteUploader;
	private final ResourceLocation location;
	private final int width;
	private final int height;
	private final int sliceLeft;
	private final int sliceRight;
	private final int sliceTop;
	private final int sliceBottom;

	public DrawableNineSliceTexture(JeiSpriteUploader spriteUploader, ResourceLocation location, int width, int height, int left, int right, int top, int bottom) {
		this.spriteUploader = spriteUploader;
		this.location = location;

		this.width = width;
		this.height = height;
		this.sliceLeft = left;
		this.sliceRight = right;
		this.sliceTop = top;
		this.sliceBottom = bottom;
	}

	public void draw(int xOffset, int yOffset, int width, int height) {
		TextureAtlasSprite sprite = spriteUploader.getSprite(location);
		int leftWidth = sliceLeft;
		int rightWidth = sliceRight;
		int topHeight = sliceTop;
		int bottomHeight = sliceBottom;
		int textureWidth = this.width;
		int textureHeight = this.height;

		Minecraft minecraft = Minecraft.getInstance();
		TextureManager textureManager = minecraft.getTextureManager();
		textureManager.bindTexture(Constants.LOCATION_JEI_GUI_TEXTURE_ATLAS);

		float uMin = sprite.getMinU();
		float uMax = sprite.getMaxU();
		float vMin = sprite.getMinV();
		float vMax = sprite.getMaxV();
		float uSize = uMax - uMin;
		float vSize = vMax - vMin;

		float uLeft = uMin + uSize * (leftWidth / (float) textureWidth);
		float uRight = uMax - uSize * (rightWidth / (float) textureWidth);
		float vTop = vMin + vSize * (topHeight / (float) textureHeight);
		float vBottom = vMax - vSize * (bottomHeight / (float) textureHeight);

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferBuilder = tessellator.getBuffer();
		bufferBuilder.begin(7, DefaultVertexFormats.POSITION_TEX);

		// left top
		draw(bufferBuilder, uMin, vMin, uLeft, vTop, xOffset, yOffset, leftWidth, topHeight);
		// left bottom
		draw(bufferBuilder, uMin, vBottom, uLeft, vMax, xOffset, yOffset + height - bottomHeight, leftWidth, bottomHeight);
		// right top
		draw(bufferBuilder, uRight, vMin, uMax, vTop, xOffset + width - rightWidth, yOffset, rightWidth, topHeight);
		// right bottom
		draw(bufferBuilder, uRight, vBottom, uMax, vMax, xOffset + width - rightWidth, yOffset + height - bottomHeight, rightWidth, bottomHeight);

		int middleWidth = textureWidth - leftWidth - rightWidth;
		int middleHeight = textureWidth - topHeight - bottomHeight;
		int tiledMiddleWidth = width - leftWidth - rightWidth;
		int tiledMiddleHeight = height - topHeight - bottomHeight;
		if (tiledMiddleWidth > 0) {
			// top edge
			drawTiled(bufferBuilder, uLeft, vMin, uRight, vTop, xOffset + leftWidth, yOffset, tiledMiddleWidth, topHeight, middleWidth, topHeight);
			// bottom edge
			drawTiled(bufferBuilder, uLeft, vBottom, uRight, vMax, xOffset + leftWidth, yOffset + height - bottomHeight, tiledMiddleWidth, bottomHeight, middleWidth, bottomHeight);
		}
		if (tiledMiddleHeight > 0) {
			// left side
			drawTiled(bufferBuilder, uMin, vTop, uLeft, vBottom, xOffset, yOffset + topHeight, leftWidth, tiledMiddleHeight, leftWidth, middleHeight);
			// right side
			drawTiled(bufferBuilder, uRight, vTop, uMax, vBottom, xOffset + width - rightWidth, yOffset + topHeight, rightWidth, tiledMiddleHeight, rightWidth, middleHeight);
		}
		if (tiledMiddleHeight > 0 && tiledMiddleWidth > 0) {
			// middle area
			drawTiled(bufferBuilder, uLeft, vTop, uRight, vBottom, xOffset + leftWidth, yOffset + topHeight, tiledMiddleWidth, tiledMiddleHeight, middleWidth, middleHeight);
		}

		tessellator.draw();
	}

	private void drawTiled(BufferBuilder bufferBuilder, float uMin, float vMin, float uMax, float vMax, int xOffset, int yOffset, int tiledWidth, int tiledHeight, int width, int height) {
		int xTileCount = tiledWidth / width;
		int xRemainder = tiledWidth - (xTileCount * width);
		int yTileCount = tiledHeight / height;
		int yRemainder = tiledHeight - (yTileCount * height);

		int yStart = yOffset + tiledHeight;

		float uSize = uMax - uMin;
		float vSize = vMax - vMin;

		for (int xTile = 0; xTile <= xTileCount; xTile++) {
			for (int yTile = 0; yTile <= yTileCount; yTile++) {
				int tileWidth = (xTile == xTileCount) ? xRemainder : width;
				int tileHeight = (yTile == yTileCount) ? yRemainder : height;
				int x = xOffset + (xTile * width);
				int y = yStart - ((yTile + 1) * height);
				if (tileWidth > 0 && tileHeight > 0) {
					int maskRight = width - tileWidth;
					int maskTop = height - tileHeight;
					float uOffset = (maskRight / (float) width) * uSize;
					float vOffset = (maskTop / (float) height) * vSize;

					draw(bufferBuilder, uMin, vMin + vOffset, uMax - uOffset, vMax, x, y + maskTop, tileWidth, tileHeight);
				}
			}
		}
	}

	private static void draw(BufferBuilder bufferBuilder, float minU, double minV, float maxU, float maxV, int xOffset, int yOffset, int width, int height) {
		bufferBuilder.pos(xOffset, yOffset + height, 0)
			.tex(minU, maxV)
			.endVertex();
		bufferBuilder.pos(xOffset + width, yOffset + height, 0)
			.tex(maxU, maxV)
			.endVertex();
		bufferBuilder.pos(xOffset + width, yOffset, 0)
			.tex(maxU, (float) minV)
			.endVertex();
		bufferBuilder.pos(xOffset, yOffset, 0)
			.tex(minU, (float) minV)
			.endVertex();
	}
}
