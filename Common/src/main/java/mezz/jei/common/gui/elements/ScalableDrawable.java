package mezz.jei.common.gui.elements;

import mezz.jei.api.gui.drawable.IScalableDrawable;
import mezz.jei.common.gui.textures.JeiAtlasManager;
import mezz.jei.common.platform.IPlatformRenderHelper;
import mezz.jei.common.platform.Services;
import mezz.jei.common.util.ImmutableRect2i;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.metadata.gui.GuiMetadataSection;
import net.minecraft.client.resources.metadata.gui.GuiSpriteScaling;
import net.minecraft.resources.Identifier;

import java.util.function.Function;

public class ScalableDrawable implements IScalableDrawable {
	private final TextureAtlas textureAtlas;
	private final Identifier spriteId;
	private final Function<TextureAtlasSprite, GuiSpriteScaling> scalingSupplier;

	public ScalableDrawable(JeiAtlasManager atlasManager, Identifier spriteId) {
		this(atlasManager.getAtlas(), spriteId, atlasManager::getSpriteScaling);
	}

	public ScalableDrawable(TextureAtlas textureAtlas, Identifier spriteId) {
		this(textureAtlas, spriteId, ScalableDrawable::getSpriteScaling);
	}

	private ScalableDrawable(
		TextureAtlas textureAtlas,
		Identifier spriteId,
		Function<TextureAtlasSprite, GuiSpriteScaling> scalingSupplier
	) {
		this.textureAtlas = textureAtlas;
		this.spriteId = spriteId;
		this.scalingSupplier = scalingSupplier;
	}

	public void draw(GuiGraphics guiGraphics, ImmutableRect2i area) {
		draw(guiGraphics, area.getX(), area.getY(), area.getWidth(), area.getHeight());
	}

	@Override
	public void draw(GuiGraphics guiGraphics, int xOffset, int yOffset, int width, int height) {
		TextureAtlasSprite sprite = textureAtlas.getSprite(spriteId);
		GuiSpriteScaling scaling = scalingSupplier.apply(sprite);

		switch (scaling) {
			case GuiSpriteScaling.Tile tileScaling -> {
				IPlatformRenderHelper renderHelper = Services.PLATFORM.getRenderHelper();
				renderHelper.blitTiledSprite(
					guiGraphics,
					RenderPipelines.GUI_TEXTURED,
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
					RenderPipelines.GUI_TEXTURED,
					sprite,
					nineSliceScaling,
					xOffset,
					yOffset,
					width,
					height
				);
			}
			default -> {
				guiGraphics.blitSprite(
					RenderPipelines.GUI_TEXTURED,
					sprite,
					xOffset,
					yOffset,
					width,
					height
				);
			}
		}
	}

	private static GuiSpriteScaling getSpriteScaling(TextureAtlasSprite sprite) {
		return sprite.contents()
			.getAdditionalMetadata(GuiMetadataSection.TYPE)
			.orElse(GuiMetadataSection.DEFAULT)
			.scaling();
	}
}
