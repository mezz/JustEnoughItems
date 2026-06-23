package mezz.jei.common.gui.elements;

import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.common.gui.textures.JeiGuiSpriteManager;
import mezz.jei.common.platform.IPlatformRenderHelper;
import mezz.jei.common.platform.Services;
import mezz.jei.common.util.function.LazySupplier;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

public class DrawableSprite implements IDrawableStatic {
	private final LazySupplier<TextureAtlasSprite> spriteSupplier;

	public DrawableSprite(JeiGuiSpriteManager spriteManager, ResourceLocation spriteId) {
		this(() -> spriteManager.getSprite(spriteId));
	}

	public DrawableSprite(TextureAtlas textureAtlas, ResourceLocation spriteId) {
		this(() -> textureAtlas.getSprite(spriteId));
	}

	private DrawableSprite(Supplier<TextureAtlasSprite> spriteSupplier) {
		this.spriteSupplier = new LazySupplier<>(spriteSupplier);
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
	public void draw(GuiGraphics guiGraphics, int xOffset, int yOffset) {
		draw(guiGraphics, xOffset, yOffset, 0, 0, 0, 0);
	}

	@Override
	public void draw(GuiGraphics guiGraphics, int xOffset, int yOffset, int maskTop, int maskBottom, int maskLeft, int maskRight) {
		TextureAtlasSprite sprite = spriteSupplier.get();
		int width = sprite.contents().width();
		int height = sprite.contents().height();

		int uWidth = width - (maskRight + maskLeft);
		int vHeight = height - (maskBottom + maskTop);

		IPlatformRenderHelper renderHelper = Services.PLATFORM.getRenderHelper();
		renderHelper.blitSprite(
			guiGraphics,
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
