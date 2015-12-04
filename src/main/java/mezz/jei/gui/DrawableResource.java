package mezz.jei.gui;

import javax.annotation.Nonnull;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.fml.client.config.GuiUtils;

import mezz.jei.api.gui.IDrawable;

public class DrawableResource implements IDrawable {

	@Nonnull
	private final ResourceLocation resourceLocation;
	private final int u;
	private final int v;
	private final int width;
	private final int height;
	private final int paddingTop;
	private final int paddingBottom;
	private final int paddingLeft;
	private final int paddingRight;

	public DrawableResource(@Nonnull ResourceLocation resourceLocation, int u, int v, int width, int height) {
		this(resourceLocation, u, v, width, height, 0, 0, 0, 0);
	}

	public DrawableResource(@Nonnull ResourceLocation resourceLocation, int u, int v, int width, int height, int paddingTop, int paddingBottom, int paddingLeft, int paddingRight) {
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
	public void draw(@Nonnull Minecraft minecraft) {
		minecraft.getTextureManager().bindTexture(resourceLocation);
		GuiUtils.drawTexturedModalRect(paddingLeft, paddingTop, u, v, width, height, 0);
	}

	@Override
	public void draw(@Nonnull Minecraft minecraft, int xOffset, int yOffset) {
		minecraft.getTextureManager().bindTexture(resourceLocation);
		GuiUtils.drawTexturedModalRect(xOffset + paddingLeft, yOffset + paddingTop, u, v, width, height, 0);
	}
}
