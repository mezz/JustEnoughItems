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
		textureManager.bind(Constants.LOCATION_JEI_GUI_TEXTURE_ATLAS);

		maskTop += trimTop;
		maskBottom += trimBottom;
		maskLeft += trimLeft;
		maskRight += trimRight;

		int x = xOffset + maskLeft;
		int y = yOffset + maskTop;
		int width = textureWidth - maskRight - maskLeft;
		int height = textureHeight - maskBottom - maskTop;
		float uSize = sprite.getU1() - sprite.getU0();
		float vSize = sprite.getV1() - sprite.getV0();

		float minU = sprite.getU0() + uSize * (maskLeft / (float) textureWidth);
		float minV = sprite.getV0() + vSize * (maskTop / (float) textureHeight);
		float maxU = sprite.getU1() - uSize * (maskRight / (float) textureWidth);
		float maxV = sprite.getV1() - vSize * (maskBottom / (float) textureHeight);

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferBuilder = tessellator.getBuilder();
		bufferBuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
		Matrix4f matrix = matrixStack.last().pose();
		bufferBuilder.vertex(matrix, x, y + height, 0)
			.uv(minU, maxV)
			.endVertex();
		bufferBuilder.vertex(matrix, x + width, y + height, 0)
			.uv(maxU, maxV)
			.endVertex();
		bufferBuilder.vertex(matrix, x + width, y, 0)
			.uv(maxU, minV)
			.endVertex();
		bufferBuilder.vertex(matrix, x, y, 0)
			.uv(minU, minV)
			.endVertex();
		tessellator.end();
	}
}
