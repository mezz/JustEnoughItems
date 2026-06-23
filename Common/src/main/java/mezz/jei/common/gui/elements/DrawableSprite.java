package mezz.jei.common.gui.elements;

import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.common.platform.IPlatformRenderHelper;
import mezz.jei.common.platform.Services;
import mezz.jei.common.util.function.LazySupplier;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;

public class DrawableSprite implements IDrawableStatic {
	private final LazySupplier<TextureAtlasSprite> spriteSupplier;

	public DrawableSprite(TextureAtlas textureAtlas, Identifier spriteId) {
		this.spriteSupplier = new LazySupplier<>(() -> textureAtlas.getSprite(spriteId));
	}

	@Override
	public int getWidth() {
		TextureAtlasSprite sprite = spriteSupplier.get();
		return sprite.contents().width();
	}

	@Override
	public int getHeight() {
		TextureAtlasSprite sprite = spriteSupplier.get();
		return sprite.contents().height();
	}

	@Override
	public void draw(GuiGraphicsExtractor guiGraphics, int xOffset, int yOffset) {
		draw(guiGraphics, xOffset, yOffset, 0, 0, 0, 0);
	}

	@Override
	public void draw(GuiGraphicsExtractor guiGraphics, int xOffset, int yOffset, int maskTop, int maskBottom, int maskLeft, int maskRight) {
		TextureAtlasSprite sprite = spriteSupplier.get();
		int width = sprite.contents().width();
		int height = sprite.contents().height();

		int uWidth = width - (maskRight + maskLeft);
		int vHeight = height - (maskBottom + maskTop);

		IPlatformRenderHelper renderHelper = Services.PLATFORM.getRenderHelper();
		renderHelper.blitSprite(
			guiGraphics,
			RenderPipelines.GUI_TEXTURED,
			sprite,
			width,
			height,
			maskLeft,
			maskTop,
			xOffset + maskLeft,
			yOffset + maskTop,
			uWidth,
			vHeight
		);
	}
}
