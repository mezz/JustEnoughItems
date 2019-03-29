package mezz.jei.gui.elements;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;

import mezz.jei.Internal;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.gui.textures.Textures;

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
			GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
			GlStateManager.enableAlphaTest();
			this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
			int k = this.getHoverState(this.hovered);
			Textures textures = Internal.getTextures();
			DrawableNineSliceTexture texture = textures.getButtonForState(k);
			texture.draw(this.x, this.y, this.width, this.height);
			this.renderBg(minecraft, mouseX, mouseY);

			int color = 14737632;
			if (packedFGColor != 0) {
				color = packedFGColor;
			} else if (!this.enabled) {
				color = 10526880;
			} else if (this.hovered) {
				color = 16777120;
			}
			if ((color & -67108864) == 0) {
				color |= -16777216;
			}

			float red = (float) (color >> 16 & 255) / 255.0F;
			float blue = (float) (color >> 8 & 255) / 255.0F;
			float green = (float) (color & 255) / 255.0F;
			float alpha = (float) (color >> 24 & 255) / 255.0F;
			GlStateManager.color4f(red, blue, green, alpha);

			double xOffset = x + (height - this.icon.getWidth()) / 2.0;
			double yOffset = y + (width - this.icon.getHeight()) / 2.0;
			GlStateManager.pushMatrix();
			GlStateManager.translated(xOffset, yOffset, 0);
			this.icon.draw();
			GlStateManager.popMatrix();
		}
	}
}
