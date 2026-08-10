package mezz.jei.common.gui.elements;

import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.common.platform.IPlatformRenderHelper;
import mezz.jei.common.platform.Services;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;

import java.util.function.Supplier;

public class DrawableSprite implements IDrawableStatic {
	// Texture atlases replace their sprites on resource reload, so this must not be cached.
	private final Supplier<TextureAtlasSprite> spriteSupplier;
	private final int width;
	private final int height;

	public DrawableSprite(TextureAtlas textureAtlas, Identifier spriteId) {
		this(() -> textureAtlas.getSprite(spriteId));
	}

	public DrawableSprite(TextureAtlas textureAtlas, Identifier spriteId, int width, int height) {
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
	public void draw(GuiGraphicsExtractor guiGraphics, int xOffset, int yOffset) {
		draw(guiGraphics, xOffset, yOffset, 0, 0, 0, 0);
	}

	@Override
	public void draw(GuiGraphicsExtractor guiGraphics, int xOffset, int yOffset, int maskTop, int maskBottom, int maskLeft, int maskRight) {
		TextureAtlasSprite sprite = spriteSupplier.get();
		int width = getWidth(sprite);
		int height = getHeight(sprite);

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
