package mezz.jei.common.gui.elements;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import mezz.jei.api.gui.drawable.IScalableDrawable;
import mezz.jei.common.gui.textures.JeiGuiSpriteManager;
import mezz.jei.common.platform.IPlatformRenderHelper;
import mezz.jei.common.platform.Services;
import mezz.jei.common.util.ImmutableRect2i;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.metadata.gui.GuiMetadataSection;
import net.minecraft.client.resources.metadata.gui.GuiSpriteScaling;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceMetadata;
import org.joml.Matrix4f;

import java.util.function.Function;
import java.util.function.Supplier;

public class ScalableDrawable implements IScalableDrawable {
	// Texture atlases replace their sprites on resource reload, so this must not be cached.
	private final Supplier<TextureAtlasSprite> spriteSupplier;
	private final Function<TextureAtlasSprite, GuiSpriteScaling> scalingSupplier;

	public ScalableDrawable(JeiGuiSpriteManager spriteManager, ResourceLocation spriteId) {
		this(() -> spriteManager.getSprite(spriteId), spriteManager::getSpriteScaling);
	}

	public ScalableDrawable(TextureAtlas textureAtlas, ResourceLocation spriteId) {
		this(() -> textureAtlas.getSprite(spriteId), ScalableDrawable::getSpriteScaling);
	}

	private ScalableDrawable(Supplier<TextureAtlasSprite> spriteSupplier, Function<TextureAtlasSprite, GuiSpriteScaling> scalingSupplier) {
		this.spriteSupplier = spriteSupplier;
		this.scalingSupplier = scalingSupplier;
	}

	public void draw(GuiGraphics guiGraphics, ImmutableRect2i area) {
		draw(guiGraphics, area.getX(), area.getY(), area.getWidth(), area.getHeight());
	}

	@Override
	public void draw(GuiGraphics guiGraphics, int xOffset, int yOffset, int width, int height) {
		TextureAtlasSprite sprite = spriteSupplier.get();
		GuiSpriteScaling scaling = scalingSupplier.apply(sprite);

		switch (scaling) {
			case GuiSpriteScaling.Tile tileScaling -> {
				blitTiledSpriteWithColor(
					guiGraphics,
					sprite,
					tileScaling,
					xOffset,
					yOffset,
					width,
					height,
					-1
				);
			}
			case GuiSpriteScaling.NineSlice nineSliceScaling -> {
				IPlatformRenderHelper renderHelper = Services.PLATFORM.getRenderHelper();
				renderHelper.blitNineSlicedSprite(
					guiGraphics,
					sprite,
					nineSliceScaling,
					xOffset,
					yOffset,
					width,
					height
				);
			}
			default -> {
				IPlatformRenderHelper renderHelper = Services.PLATFORM.getRenderHelper();
				// Using the sprite's pixel size here would make larger draws sample past its edges.
				// Using the draw size instead stretches the full sprite to the requested area.
				renderHelper.blitSprite(
					guiGraphics,
					sprite,
					width,
					height,
					0,
					0,
					xOffset,
					yOffset,
					width,
					height
				);
			}
		}
	}

	/**
	 * Draws a tiled {@link TextureAtlasSprite} with per-vertex color.
	 * Minecraft 1.21.1's tiled sprite overload has no color argument, so color-aware callers otherwise have to rely on global shader color.
	 */
	public static void blitTiledSpriteWithColor(GuiGraphics guiGraphics, TextureAtlasSprite sprite, GuiSpriteScaling.Tile scaling, int xOffset, int yOffset, int width, int height, int color) {
		int tileWidth = scaling.width();
		int tileHeight = scaling.height();

		if (width <= 0 || height <= 0) {
			return;
		}

		if (tileWidth <= 0 || tileHeight <= 0) {
			throw new IllegalArgumentException("Tile size must be positive, got " + tileWidth + "x" + tileHeight);
		}

		for (int xTile = 0; xTile < width; xTile += tileWidth) {
			int uWidth = Math.min(tileWidth, width - xTile);
			for (int yTile = 0; yTile < height; yTile += tileHeight) {
				int vHeight = Math.min(tileHeight, height - yTile);
				blitSprite(
					guiGraphics,
					sprite,
					tileWidth,
					tileHeight,
					0,
					0,
					xOffset + xTile,
					yOffset + yTile,
					uWidth,
					vHeight,
					color
				);
			}
		}
	}

	private static void blitSprite(
		GuiGraphics guiGraphics,
		TextureAtlasSprite sprite,
		int textureWidth,
		int textureHeight,
		int uPosition,
		int vPosition,
		int x,
		int y,
		int uWidth,
		int vHeight,
		int color
	) {
		if (uWidth <= 0 || vHeight <= 0) {
			return;
		}

		float u0 = sprite.getU((float) uPosition / textureWidth);
		float u1 = sprite.getU((float) (uPosition + uWidth) / textureWidth);
		float v0 = sprite.getV((float) vPosition / textureHeight);
		float v1 = sprite.getV((float) (vPosition + vHeight) / textureHeight);

		float alpha = (color >> 24 & 0xFF) / 255F;
		float red = (color >> 16 & 0xFF) / 255F;
		float green = (color >> 8 & 0xFF) / 255F;
		float blue = (color & 0xFF) / 255F;

		RenderSystem.setShaderTexture(0, sprite.atlasLocation());
		RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
		RenderSystem.enableBlend();

		Matrix4f matrix = guiGraphics.pose().last().pose();
		BufferBuilder bufferBuilder = Tesselator.getInstance()
			.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
		bufferBuilder.addVertex(matrix, x, y, 0)
			.setUv(u0, v0)
			.setColor(red, green, blue, alpha);
		bufferBuilder.addVertex(matrix, x, y + vHeight, 0)
			.setUv(u0, v1)
			.setColor(red, green, blue, alpha);
		bufferBuilder.addVertex(matrix, x + uWidth, y + vHeight, 0)
			.setUv(u1, v1)
			.setColor(red, green, blue, alpha);
		bufferBuilder.addVertex(matrix, x + uWidth, y, 0)
			.setUv(u1, v0)
			.setColor(red, green, blue, alpha);
		BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());

		RenderSystem.disableBlend();
	}

	private static GuiSpriteScaling getSpriteScaling(TextureAtlasSprite sprite) {
		SpriteContents contents = sprite.contents();
		ResourceMetadata metadata = contents.metadata();
		return metadata.getSection(GuiMetadataSection.TYPE)
			.orElse(GuiMetadataSection.DEFAULT)
			.scaling();
	}
}
