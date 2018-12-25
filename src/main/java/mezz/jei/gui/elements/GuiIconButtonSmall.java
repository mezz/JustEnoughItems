package mezz.jei.gui.elements;

import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;

import mezz.jei.api.gui.IDrawable;
import mezz.jei.config.Constants;

/**
 * A small gui button that has an {@link IDrawable} instead of a string label.
 */
public class GuiIconButtonSmall extends GuiButton {
	private final IDrawable icon;

	public GuiIconButtonSmall(int buttonId, int x, int y, int widthIn, int heightIn, IDrawable icon) {
		super(buttonId, x, y, widthIn, heightIn, "");
		this.icon = icon;
	}

	@Override
	public void render(int mouseX, int mouseY, float partialTicks) {
		if (this.visible) {
			Minecraft minecraft = Minecraft.getInstance();
			this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
			int k = this.getHoverState(this.hovered);
			GuiUtils.drawContinuousTexturedBox(Constants.RECIPE_BACKGROUND, this.x, this.y, 0, 182 + k * 20, this.width, this.height, 95, 20, 2, 2, 2, 2, this.zLevel);
			this.renderBg(minecraft, mouseX, mouseY);

			int color = 14737632;
			if (!this.enabled) {
				color = 10526880;
			} else if (this.hovered) {
				color = 16777120;
			}

			float red = (float) (color >> 16 & 255) / 255.0F;
			float blue = (float) (color >> 8 & 255) / 255.0F;
			float green = (float) (color & 255) / 255.0F;
			float alpha = (float) (color >> 24 & 255) / 255.0F;
			GlStateManager.color4f(red, blue, green, alpha);

			int xOffset = x + (height - this.icon.getWidth()) / 2;
			int yOffset = y + (width - this.icon.getHeight()) / 2;
			this.icon.draw(xOffset, yOffset);
		}
	}
}
