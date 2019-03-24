package mezz.jei.gui.elements;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;

import mezz.jei.Internal;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.gui.GuiHelper;

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
	public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
		if (this.visible) {
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			GlStateManager.enableAlpha();
			this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
			int k = this.getHoverState(this.hovered);
			GuiHelper guiHelper = Internal.getHelpers().getGuiHelper();
			DrawableNineSliceTexture texture = guiHelper.getButtonForState(k);
			texture.draw(mc, this.x, this.y, this.width, this.height);
			this.mouseDragged(mc, mouseX, mouseY);

			int color = 14737632;
			if (!this.enabled) {
				color = 10526880;
			} else if (this.hovered) {
				color = 16777120;
			}
			color |= -16777216;

			float red = (float) (color >> 16 & 255) / 255.0F;
			float blue = (float) (color >> 8 & 255) / 255.0F;
			float green = (float) (color & 255) / 255.0F;
			float alpha = (float) (color >> 24 & 255) / 255.0F;
			GlStateManager.color(red, blue, green, alpha);

			double xOffset = x + (height - this.icon.getWidth()) / 2.0;
			double yOffset = y + (width - this.icon.getHeight()) / 2.0;
			GlStateManager.pushMatrix();
			GlStateManager.translate(xOffset, yOffset, 0);
			this.icon.draw(mc);
			GlStateManager.popMatrix();
		}
	}
}
