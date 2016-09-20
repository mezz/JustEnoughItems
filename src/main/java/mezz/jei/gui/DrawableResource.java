package mezz.jei.gui;

import mezz.jei.api.gui.IDrawableStatic;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.config.GuiUtils;

public class DrawableResource implements IDrawableStatic {

	private final ResourceLocation resourceLocation;
	private final int u;
	private final int v;
	private final int width;
	private final int height;
	private final int paddingTop;
	private final int paddingBottom;
	private final int paddingLeft;
	private final int paddingRight;

	public DrawableResource(ResourceLocation resourceLocation, int u, int v, int width, int height) {
		this(resourceLocation, u, v, width, height, 0, 0, 0, 0);
	}

	public DrawableResource(ResourceLocation resourceLocation, int u, int v, int width, int height, int paddingTop, int paddingBottom, int paddingLeft, int paddingRight) {
		this.resourceLocation = resourceLocation;

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
	public void draw(Minecraft minecraft) {
		draw(minecraft, 0, 0);
	}

	@Override
	public void draw(Minecraft minecraft, int xOffset, int yOffset) {
		draw(minecraft, xOffset, yOffset, 0, 0, 0, 0);
	}

	@Override
	public void draw(Minecraft minecraft, int xOffset, int yOffset, int maskTop, int maskBottom, int maskLeft, int maskRight) {
		minecraft.getTextureManager().bindTexture(this.resourceLocation);

		int x = xOffset + this.paddingLeft + maskLeft;
		int y = yOffset + this.paddingTop + maskTop;
		int u = this.u + maskLeft;
		int v = this.v + maskTop;
		int width = this.width - maskRight - maskLeft;
		int height = this.height - maskBottom - maskTop;
		GuiUtils.drawTexturedModalRect(x, y, u, v, width, height, 0);
	}
}
