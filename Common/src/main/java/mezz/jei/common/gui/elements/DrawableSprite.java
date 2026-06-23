package mezz.jei.common.gui.elements;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.common.Constants;
import mezz.jei.common.gui.textures.JeiSpriteUploader;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

public class DrawableSprite implements IDrawableStatic {
	private final Supplier<TextureAtlasSprite> spriteSupplier;
	private final ResourceLocation atlasLocation;
	private final int width;
	private final int height;
	private int trimLeft;
	private int trimRight;
	private int trimTop;
	private int trimBottom;

	public DrawableSprite(JeiSpriteUploader spriteUploader, ResourceLocation location, int width, int height) {
		this(() -> spriteUploader.getSprite(location), Constants.LOCATION_JEI_GUI_TEXTURE_ATLAS, width, height);
	}

	public DrawableSprite(JeiSpriteUploader spriteUploader, ResourceLocation location) {
		this(() -> spriteUploader.getSprite(location), Constants.LOCATION_JEI_GUI_TEXTURE_ATLAS);
	}

	public DrawableSprite(TextureAtlas textureAtlas, ResourceLocation spriteId) {
		this(() -> textureAtlas.getSprite(spriteId), textureAtlas.location());
	}

	DrawableSprite(Supplier<TextureAtlasSprite> spriteSupplier, ResourceLocation atlasLocation) {
		this(spriteSupplier, atlasLocation, 0, 0);
	}

	DrawableSprite(Supplier<TextureAtlasSprite> spriteSupplier, ResourceLocation atlasLocation, int width, int height) {
		if (width < 0 || height < 0 || (width == 0) != (height == 0)) {
			throw new IllegalArgumentException("DrawableSprite size must be positive, or both dimensions must be 0 to use the sprite size");
		}
		this.spriteSupplier = spriteSupplier;
		this.atlasLocation = atlasLocation;
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
		TextureAtlasSprite sprite = spriteSupplier.get();
		return getWidth(sprite);
	}

	@Override
	public int getHeight() {
		TextureAtlasSprite sprite = spriteSupplier.get();
		return getHeight(sprite);
	}

	@Override
	public void draw(PoseStack poseStack, int xOffset, int yOffset) {
		draw(poseStack, xOffset, yOffset, 0, 0, 0, 0);
	}

	@Override
	public void draw(PoseStack poseStack, int xOffset, int yOffset, int maskTop, int maskBottom, int maskLeft, int maskRight) {
		TextureAtlasSprite sprite = spriteSupplier.get();
		int textureWidth = getWidth(sprite);
		int textureHeight = getHeight(sprite);

		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, atlasLocation);

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
		Matrix4f matrix = poseStack.last().pose();
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
		return sprite.getWidth();
	}

	private int getHeight(TextureAtlasSprite sprite) {
		if (height > 0) {
			return height;
		}
		return sprite.getHeight();
	}
}
