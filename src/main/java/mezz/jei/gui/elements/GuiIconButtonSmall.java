package mezz.jei.gui.elements;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;

import mezz.jei.Internal;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.gui.textures.Textures;
import net.minecraft.util.text.StringTextComponent;

/**
 * A small gui button that has an {@link IDrawable} instead of a string label.
 */
public class GuiIconButtonSmall extends Button {
	private final IDrawable icon;

	public GuiIconButtonSmall(int x, int y, int widthIn, int heightIn, IDrawable icon, Button.IPressable pressable) {
		super(x, y, widthIn, heightIn, StringTextComponent.EMPTY, pressable);
		this.icon = icon;
	}

	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		if (this.visible) {
			Minecraft minecraft = Minecraft.getInstance();
			RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
			RenderSystem.enableAlphaTest();
			boolean hovered = isMouseOver(mouseX, mouseY);
			Textures textures = Internal.getTextures();
			DrawableNineSliceTexture texture = textures.getButtonForState(this.active, hovered);
			texture.draw(matrixStack, this.x, this.y, this.width, this.height);
			this.renderBg(matrixStack, minecraft, mouseX, mouseY);

			int color = 14737632;
			if (packedFGColor != 0) {
				color = packedFGColor;
			} else if (!this.active) {
				color = 10526880;
			} else if (hovered) {
				color = 16777120;
			}
			if ((color & -67108864) == 0) {
				color |= -16777216;
			}

			float red = (float) (color >> 16 & 255) / 255.0F;
			float blue = (float) (color >> 8 & 255) / 255.0F;
			float green = (float) (color & 255) / 255.0F;
			float alpha = (float) (color >> 24 & 255) / 255.0F;
			RenderSystem.color4f(red, blue, green, alpha);

			double xOffset = x + (width - this.icon.getWidth()) / 2.0;
			double yOffset = y + (height - this.icon.getHeight()) / 2.0;
			matrixStack.push();
			matrixStack.translate(xOffset, yOffset, 0);
			this.icon.draw(matrixStack);
			matrixStack.pop();
		}
	}
}
