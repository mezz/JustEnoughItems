package mezz.jei.common.gui.elements;

import com.mojang.blaze3d.systems.RenderSystem;
import mezz.jei.api.gui.drawable.IDrawable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public class DrawableText implements IDrawable {
	private final String text;
	private final int width;
	private final int height;
	private final int color;

	public DrawableText(String text, int width, int height, int color) {
		this.text = text;
		this.width = width;
		this.height = height;
		this.color = color;
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
		Minecraft minecraft = Minecraft.getInstance();
		Font fontRenderer = minecraft.font;
		int textCenterX = xOffset + (width / 2);
		int textCenterY = yOffset + (height / 2) - 3;
		int stringCenter = fontRenderer.width(text) / 2;
		guiGraphics.drawString(fontRenderer, text, textCenterX - stringCenter, textCenterY, color);
		RenderSystem.setShaderColor(1, 1, 1, 1);
	}
}
