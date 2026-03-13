package mezz.jei.common.gui.elements;

import mezz.jei.api.gui.drawable.IScalableDrawable;
import mezz.jei.common.gui.textures.JeiAtlasManager;
import mezz.jei.common.platform.IPlatformRenderHelper;
import mezz.jei.common.platform.Services;
import mezz.jei.common.util.ImmutableRect2i;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.metadata.gui.GuiMetadataSection;
import net.minecraft.client.resources.metadata.gui.GuiSpriteScaling;
import net.minecraft.resources.Identifier;

public class ScalableDrawable implements IScalableDrawable {
	private final JeiAtlasManager atlasManager;
	private final Identifier spriteId;

	public ScalableDrawable(JeiAtlasManager atlasManager, Identifier spriteId) {
		this.atlasManager = atlasManager;
		this.spriteId = spriteId;
	}

	public void draw(GuiGraphicsExtractor guiGraphics, ImmutableRect2i area) {
		draw(guiGraphics, area.getX(), area.getY(), area.getWidth(), area.getHeight());
	}

	@Override
	public void draw(GuiGraphicsExtractor guiGraphics, int xOffset, int yOffset, int width, int height) {
		TextureAtlasSprite sprite = atlasManager.getAtlas()
			.getSprite(spriteId);
		GuiSpriteScaling scaling = sprite.contents()
			.getAdditionalMetadata(GuiMetadataSection.TYPE)
			.orElse(GuiMetadataSection.DEFAULT)
			.scaling();

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
}
