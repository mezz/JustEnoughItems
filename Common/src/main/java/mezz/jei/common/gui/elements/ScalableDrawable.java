package mezz.jei.common.gui.elements;

import mezz.jei.api.gui.drawable.IScalableDrawable;
import mezz.jei.common.gui.textures.JeiGuiSpriteManager;
import mezz.jei.common.platform.IPlatformRenderHelper;
import mezz.jei.common.platform.Services;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.common.util.function.LazySupplier;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.metadata.gui.GuiMetadataSection;
import net.minecraft.client.resources.metadata.gui.GuiSpriteScaling;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceMetadata;

import java.util.function.Supplier;

public class ScalableDrawable implements IScalableDrawable {
	private final LazySupplier<TextureAtlasSprite> spriteSupplier;

	public ScalableDrawable(JeiGuiSpriteManager spriteManager, ResourceLocation spriteId) {
		this(() -> spriteManager.getSprite(spriteId));
	}

	public ScalableDrawable(TextureAtlas textureAtlas, ResourceLocation spriteId) {
		this(() -> textureAtlas.getSprite(spriteId));
	}

	private ScalableDrawable(Supplier<TextureAtlasSprite> spriteSupplier) {
		this.spriteSupplier = new LazySupplier<>(spriteSupplier);
	}

	public void draw(GuiGraphics guiGraphics, ImmutableRect2i area) {
		draw(guiGraphics, area.getX(), area.getY(), area.getWidth(), area.getHeight());
	}

	@Override
	public void draw(GuiGraphics guiGraphics, int xOffset, int yOffset, int width, int height) {
		TextureAtlasSprite sprite = spriteSupplier.get();
		GuiSpriteScaling scaling = getSpriteScaling(sprite);

		switch (scaling) {
			case GuiSpriteScaling.Tile tileScaling -> {
				IPlatformRenderHelper renderHelper = Services.PLATFORM.getRenderHelper();
				renderHelper.blitTiledSprite(
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
				SpriteContents contents = sprite.contents();
				IPlatformRenderHelper renderHelper = Services.PLATFORM.getRenderHelper();
				renderHelper.blitSprite(
					guiGraphics,
					sprite,
					contents.width(),
					contents.height(),
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

	private static GuiSpriteScaling getSpriteScaling(TextureAtlasSprite sprite) {
		SpriteContents contents = sprite.contents();
		ResourceMetadata metadata = contents.metadata();
		return metadata.getSection(GuiMetadataSection.TYPE)
			.orElse(GuiMetadataSection.DEFAULT)
			.scaling();
	}
}
