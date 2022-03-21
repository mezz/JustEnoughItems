package mezz.jei.gui.elements;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import mezz.jei.util.ImmutableRect2i;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;

import mezz.jei.Internal;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.gui.textures.Textures;
import net.minecraft.network.chat.TextComponent;

/**
 * A small gui button that has an {@link IDrawable} instead of a string label.
 */
public class GuiIconButtonSmall extends Button {
	private final IDrawable icon;

	public GuiIconButtonSmall(int x, int y, int widthIn, int heightIn, IDrawable icon, Button.OnPress pressable) {
		super(x, y, widthIn, heightIn, TextComponent.EMPTY, pressable);
		this.icon = icon;
	}

	public ImmutableRect2i getArea() {
		return new ImmutableRect2i(x, y, width, height);
	}

	@Override
	public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
		if (this.visible) {
			Minecraft minecraft = Minecraft.getInstance();
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			boolean hovered = isMouseOver(mouseX, mouseY);
			Textures textures = Internal.getTextures();
			DrawableNineSliceTexture texture = textures.getButtonForState(this.active, hovered);
			texture.draw(poseStack, this.x, this.y, this.width, this.height);
			this.renderBg(poseStack, minecraft, mouseX, mouseY);

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

			float red = (color >> 16 & 255) / 255.0F;
			float blue = (color >> 8 & 255) / 255.0F;
			float green = (color & 255) / 255.0F;
			float alpha = (color >> 24 & 255) / 255.0F;
			RenderSystem.setShaderColor(red, blue, green, alpha);

			double xOffset = x + (width - this.icon.getWidth()) / 2.0;
			double yOffset = y + (height - this.icon.getHeight()) / 2.0;
			poseStack.pushPose();
			{
				poseStack.translate(xOffset, yOffset, 0);
				this.icon.draw(poseStack);
			}
			poseStack.popPose();
		}
	}
}
