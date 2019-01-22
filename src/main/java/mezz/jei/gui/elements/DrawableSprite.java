package mezz.jei.gui.elements;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;

import mezz.jei.api.gui.IDrawableStatic;
import mezz.jei.gui.textures.TextureInfo;

public class DrawableSprite implements IDrawableStatic {
	private final TextureInfo info;

	public DrawableSprite(TextureInfo info) {
		this.info = info;
	}

	@Override
	public int getWidth() {
		return info.getWidth();
	}

	@Override
	public int getHeight() {
		return info.getHeight();
	}

	@Override
	public void draw(Minecraft minecraft, int xOffset, int yOffset) {
		draw(minecraft, xOffset, yOffset, 0, 0, 0, 0);
	}

	@Override
	public void draw(Minecraft minecraft, int xOffset, int yOffset, int maskTop, int maskBottom, int maskLeft, int maskRight) {
		ResourceLocation location = info.getLocation();
		TextureAtlasSprite sprite = info.getSprite();
		int textureWidth = info.getWidth();
		int textureHeight = info.getHeight();

		TextureManager textureManager = minecraft.getTextureManager();
		textureManager.bindTexture(location);

		maskTop += info.getTrimTop();
		maskBottom += info.getTrimBottom();
		maskLeft += info.getTrimLeft();
		maskRight += info.getTrimRight();

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
		bufferBuilder.pos(x, y + height, 0)
			.tex(minU, maxV)
			.endVertex();
		bufferBuilder.pos(x + width, y + height, 0)
			.tex(maxU, maxV)
			.endVertex();
		bufferBuilder.pos(x + width, y, 0)
			.tex(maxU, minV)
			.endVertex();
		bufferBuilder.pos(x, y, 0)
			.tex(minU, minV)
			.endVertex();
		tessellator.draw();
	}
}
