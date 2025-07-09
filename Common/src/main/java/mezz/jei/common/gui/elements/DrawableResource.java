package mezz.jei.common.gui.elements;

import mezz.jei.api.gui.drawable.IDrawableStatic;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.ResourceLocation;

public class DrawableResource implements IDrawableStatic {

	private final ResourceLocation resourceLocation;
	private final int textureWidth;
	private final int textureHeight;

	private final int u;
	private final int v;
	private final int width;
	private final int height;
	private final int paddingTop;
	private final int paddingBottom;
	private final int paddingLeft;
	private final int paddingRight;

	public DrawableResource(ResourceLocation resourceLocation, int u, int v, int width, int height, int paddingTop, int paddingBottom, int paddingLeft, int paddingRight, int textureWidth, int textureHeight) {
		this.resourceLocation = resourceLocation;
		this.textureWidth = textureWidth;
		this.textureHeight = textureHeight;

		this.u = u;
		this.v = v;
		this.width = width;
		this.height = height;

		this.paddingTop = paddingTop;
		this.paddingBottom = paddingBottom;
		this.paddingLeft = paddingLeft;
		this.paddingRight = paddingRight;
	}

	@Override
	public int getWidth() {
		return width + paddingLeft + paddingRight;
	}

	@Override
	public int getHeight() {
		return height + paddingTop + paddingBottom;
	}

	@Override
	public void draw(GuiGraphics guiGraphics, int xOffset, int yOffset) {
		draw(guiGraphics, xOffset, yOffset, 0, 0, 0, 0);
	}

	@Override
	public void draw(GuiGraphics guiGraphics, int xOffset, int yOffset, int maskTop, int maskBottom, int maskLeft, int maskRight) {
		int uWidth = this.width - (maskRight + maskLeft);
		int vHeight = this.height - (maskBottom + maskTop);

		guiGraphics.blit(
			RenderPipelines.GUI_TEXTURED,
			this.resourceLocation,
			xOffset + maskLeft,
			yOffset + maskTop,
			u + maskLeft,
			v + maskTop,
			uWidth,
			vHeight,
			this.textureWidth,
			this.textureHeight
		);
	}
}
