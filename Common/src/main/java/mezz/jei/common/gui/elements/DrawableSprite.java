package mezz.jei.common.gui.elements;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.Tesselator;
import mezz.jei.common.gui.textures.JeiSpriteUploader;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.resources.ResourceLocation;

import mezz.jei.api.gui.drawable.IDrawableStatic;

import org.joml.Matrix4f;

import java.util.function.Supplier;

public class DrawableSprite implements IDrawableStatic {
	private final Supplier<TextureAtlasSprite> spriteSupplier;
	private final int width;
	private final int height;
	private int trimLeft;
	private int trimRight;
	private int trimTop;
	private int trimBottom;

	public DrawableSprite(JeiSpriteUploader spriteUploader, ResourceLocation location, int width, int height) {
		this(() -> spriteUploader.getSprite(location), width, height);
	}

	public DrawableSprite(JeiSpriteUploader spriteUploader, ResourceLocation location) {
		this(() -> spriteUploader.getSprite(location));
	}

	public DrawableSprite(TextureAtlas textureAtlas, ResourceLocation spriteId) {
		this(() -> textureAtlas.getSprite(spriteId));
	}

	public DrawableSprite(TextureAtlas textureAtlas, ResourceLocation spriteId, int width, int height) {
		this(() -> textureAtlas.getSprite(spriteId), width, height);
	}

	DrawableSprite(Supplier<TextureAtlasSprite> spriteSupplier) {
		this(spriteSupplier, 0, 0);
	}

	DrawableSprite(Supplier<TextureAtlasSprite> spriteSupplier, int width, int height) {
		if (width < 0 || height < 0 || (width == 0) != (height == 0)) {
			throw new IllegalArgumentException("DrawableSprite size must be positive, or both dimensions must be 0 to use the sprite size");
		}
		this.spriteSupplier = spriteSupplier;
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
		if (width > 0) {
			return width;
		}
		TextureAtlasSprite sprite = spriteSupplier.get();
		return getWidth(sprite);
	}

	@Override
	public int getHeight() {
		if (height > 0) {
			return height;
		}
		TextureAtlasSprite sprite = spriteSupplier.get();
		return getHeight(sprite);
	}

	@Override
	public void draw(GuiGraphics guiGraphics, int xOffset, int yOffset) {
		draw(guiGraphics, xOffset, yOffset, 0, 0, 0, 0);
	}

	@Override
	public void draw(GuiGraphics guiGraphics, int xOffset, int yOffset, int maskTop, int maskBottom, int maskLeft, int maskRight) {
		TextureAtlasSprite sprite = spriteSupplier.get();
		int textureWidth = getWidth(sprite);
		int textureHeight = getHeight(sprite);

		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, sprite.atlasLocation());

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

		Tesselator tessellator = Tesselator.getInstance();
		BufferBuilder bufferBuilder = tessellator.getBuilder();
		bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
		Matrix4f matrix = guiGraphics.pose().last().pose();
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

	private int getWidth(TextureAtlasSprite sprite) {
		if (width > 0) {
			return width;
		}
		return sprite.contents().width();
	}

	private int getHeight(TextureAtlasSprite sprite) {
		if (height > 0) {
			return height;
		}
		return sprite.contents().height();
	}
}
