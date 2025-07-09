package mezz.jei.common.gui.elements;

import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.common.gui.textures.JeiGuiSpriteManager;
import mezz.jei.common.platform.IPlatformRenderHelper;
import mezz.jei.common.platform.Services;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;

public class DrawableSprite implements IDrawableStatic {
	private final JeiGuiSpriteManager spriteManager;
	private final ResourceLocation location;
	private final int width;
	private final int height;
	private int trimLeft;
	private int trimRight;
	private int trimTop;
	private int trimBottom;

	public DrawableSprite(JeiGuiSpriteManager spriteManager, ResourceLocation location, int width, int height) {
		this.spriteManager = spriteManager;
		this.location = location;
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
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public void draw(GuiGraphics guiGraphics, int xOffset, int yOffset) {
		draw(guiGraphics, xOffset, yOffset, 0, 0, 0, 0);
	}

	@Override
	public void draw(GuiGraphics guiGraphics, int xOffset, int yOffset, int maskTop, int maskBottom, int maskLeft, int maskRight) {
		TextureAtlasSprite sprite = spriteManager.getSprite(location);

		maskTop += trimTop;
		maskBottom += trimBottom;
		maskLeft += trimLeft;
		maskRight += trimRight;

		int uWidth = this.width - (maskRight + maskLeft);
		int vHeight = this.height - (maskBottom + maskTop);

		IPlatformRenderHelper renderHelper = Services.PLATFORM.getRenderHelper();
		renderHelper.blitSprite(
			guiGraphics,
			RenderType::guiTextured,
			sprite,
			this.width,
			this.height,
			maskLeft,
			maskTop,
			xOffset + maskLeft,
			yOffset + maskTop,
			uWidth,
			vHeight
		);
	}
}
