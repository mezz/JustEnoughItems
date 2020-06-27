package mezz.jei.gui.elements;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;

import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.config.Constants;
import mezz.jei.gui.textures.JeiSpriteUploader;
import net.minecraft.util.math.vector.Matrix4f;

public class DrawableSprite implements IDrawableStatic {
	private final JeiSpriteUploader spriteUploader;
	private final ResourceLocation location;
	private final int width;
	private final int height;
	private int trimLeft;
	private int trimRight;
	private int trimTop;
	private int trimBottom;

	public DrawableSprite(JeiSpriteUploader spriteUploader, ResourceLocation location, int width, int height) {
		this.spriteUploader = spriteUploader;
		this.location = location;
		this.width = width;
		this.height = height;
	}

	public DrawableSprite trim(int left, int right, int top, int bottom) {
		this.trimLeft = left;
		this.trimRight = right;
		this.trimTop = top;
		this.trimBottom = bottom;
		return this;
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
	public void draw(MatrixStack matrixStack, int xOffset, int yOffset) {
		draw(matrixStack, xOffset, yOffset, 0, 0, 0, 0);
	}

	@Override
	public void draw(MatrixStack matrixStack, int xOffset, int yOffset, int maskTop, int maskBottom, int maskLeft, int maskRight) {
		TextureAtlasSprite sprite = spriteUploader.getSprite(location);
		int textureWidth = this.width;
		int textureHeight = this.height;

		Minecraft minecraft = Minecraft.getInstance();
		TextureManager textureManager = minecraft.getTextureManager();
		textureManager.bindTexture(Constants.LOCATION_JEI_GUI_TEXTURE_ATLAS);

		maskTop += trimTop;
		maskBottom += trimBottom;
		maskLeft += trimLeft;
		maskRight += trimRight;

		int x = xOffset + maskLeft;
		int y = yOffset + maskTop;
		int width = textureWidth - maskRight - maskLeft;
		int height = textureHeight - maskBottom - maskTop;
		float uSize = sprite.getMaxU() - sprite.getMinU();
		float vSize = sprite.getMaxV() - sprite.getMinV();

		float minU = sprite.getMinU() + uSize * (maskLeft / (float) textureWidth);
		float minV = sprite.getMinV() + vSize * (maskTop / (float) textureHeight);
		float maxU = sprite.getMaxU() - uSize * (maskRight / (float) textureWidth);
		float maxV = sprite.getMaxV() - vSize * (maskBottom / (float) textureHeight);

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferBuilder = tessellator.getBuffer();
		bufferBuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
		Matrix4f matrix = matrixStack.getLast().getMatrix();
		bufferBuilder.pos(matrix, x, y + height, 0)
			.tex(minU, maxV)
			.endVertex();
		bufferBuilder.pos(matrix, x + width, y + height, 0)
			.tex(maxU, maxV)
			.endVertex();
		bufferBuilder.pos(matrix, x + width, y, 0)
			.tex(maxU, minV)
			.endVertex();
		bufferBuilder.pos(matrix, x, y, 0)
			.tex(minU, minV)
			.endVertex();
		tessellator.draw();
	}
}
