package mezz.jei.gui.elements;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.Rectangle2d;

import mezz.jei.Internal;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.gui.textures.Textures;
import net.minecraft.util.text.StringTextComponent;

/**
 * A gui button that has an {@link IDrawable} instead of a string label.
 */
public class GuiIconButton extends Button {
	private final IDrawable icon;

	public GuiIconButton(IDrawable icon, IPressable pressable) {
		super(0, 0, 0, 0, StringTextComponent.field_240750_d_, pressable);
		this.icon = icon;
	}

	public void updateBounds(Rectangle2d area) {
		this.field_230690_l_ = area.getX();
		this.field_230691_m_ = area.getY();
		this.field_230688_j_ = area.getWidth();
		this.field_230689_k_ = area.getHeight();
	}

	@Override
	public void func_230430_a_(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		if (this.field_230694_p_) {
			RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
			boolean hovered = mouseX >= this.field_230690_l_ && mouseY >= this.field_230691_m_ && mouseX < this.field_230690_l_ + this.field_230688_j_ && mouseY < this.field_230691_m_ + this.field_230689_k_;
			RenderSystem.enableBlend();
			RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
			RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
			Textures textures = Internal.getTextures();
			Minecraft minecraft = Minecraft.getInstance();
			DrawableNineSliceTexture texture = textures.getButtonForState(this.field_230693_o_, hovered);
			texture.draw(matrixStack, this.field_230690_l_, this.field_230691_m_, this.field_230688_j_, this.field_230689_k_);
			this.func_230441_a_(matrixStack, minecraft, mouseX, mouseY);

			int color = 14737632;
			if (packedFGColor != 0) {
				color = packedFGColor;
			} else if (!this.field_230693_o_) {
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

			double xOffset = field_230690_l_ + (field_230688_j_ - icon.getWidth()) / 2.0;
			double yOffset = field_230691_m_ + (field_230689_k_ - icon.getHeight()) / 2.0;
			matrixStack.push();
			matrixStack.translate(xOffset, yOffset, 0);
			icon.draw(matrixStack);
			matrixStack.pop();
			RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		}
	}

}
