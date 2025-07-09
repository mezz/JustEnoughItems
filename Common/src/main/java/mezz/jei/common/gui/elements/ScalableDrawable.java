package mezz.jei.common.gui.elements;

import mezz.jei.api.gui.drawable.IScalableDrawable;
import mezz.jei.common.gui.textures.JeiGuiSpriteManager;
import mezz.jei.common.platform.IPlatformRenderHelper;
import mezz.jei.common.platform.Services;
import mezz.jei.common.util.ImmutableRect2i;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.metadata.gui.GuiSpriteScaling;
import net.minecraft.resources.ResourceLocation;

public class ScalableDrawable implements IScalableDrawable {
	private final JeiGuiSpriteManager spriteUploader;
	private final ResourceLocation location;

	public ScalableDrawable(JeiGuiSpriteManager spriteUploader, ResourceLocation location) {
		this.spriteUploader = spriteUploader;
		this.location = location;
	}

	public void draw(GuiGraphics guiGraphics, ImmutableRect2i area) {
		draw(guiGraphics, area.getX(), area.getY(), area.getWidth(), area.getHeight());
	}

	@Override
	public void draw(GuiGraphics guiGraphics, int xOffset, int yOffset, int width, int height) {
		TextureAtlasSprite sprite = spriteUploader.getSprite(location);
		GuiSpriteScaling scaling = spriteUploader.getSpriteScaling(sprite);

		switch (scaling) {
			case GuiSpriteScaling.Tile tileScaling -> {
				IPlatformRenderHelper renderHelper = Services.PLATFORM.getRenderHelper();
				renderHelper.blitTiledSprite(
					guiGraphics,
					RenderType::guiTextured,
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
					RenderType::guiTextured,
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
					RenderType::guiTextured,
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
