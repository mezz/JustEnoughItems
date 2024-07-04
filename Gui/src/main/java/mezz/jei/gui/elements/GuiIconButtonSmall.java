package mezz.jei.gui.elements;

import com.mojang.blaze3d.systems.RenderSystem;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.common.gui.elements.DrawableNineSliceTexture;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.common.util.ImmutableRect2i;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.CommonComponents;

/**
 * A small gui button that has an {@link IDrawable} instead of a string label.
 */
public class GuiIconButtonSmall extends Button {
	private final IDrawable icon;
	private final Textures textures;

	public GuiIconButtonSmall(int x, int y, int widthIn, int heightIn, IDrawable icon, Button.OnPress pressable, Textures textures) {
		super(x, y, widthIn, heightIn, CommonComponents.EMPTY, pressable, Button.DEFAULT_NARRATION);
		this.icon = icon;
		this.textures = textures;
	}

	public ImmutableRect2i getArea() {
		return new ImmutableRect2i(getX(), getY(), getWidth(), getHeight());
	}

	@Override
	public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		boolean hovered = isMouseOver(mouseX, mouseY);
		DrawableNineSliceTexture texture = textures.getButtonForState(this.active, hovered);
		texture.draw(guiGraphics, this.getX(), this.getY(), this.getWidth(), this.getHeight());

		int color = 0xFFE0E0E0;
		if (!this.active) {
			color = 0xFFA0A0A0;
		} else if (hovered) {
			color = 0xFFFFFFFF;
		}

		float red = (color >> 16 & 255) / 255.0F;
		float blue = (color >> 8 & 255) / 255.0F;
		float green = (color & 255) / 255.0F;
		float alpha = (color >> 24 & 255) / 255.0F;
		RenderSystem.setShaderColor(red, blue, green, alpha);

		double xOffset = getX() + (getWidth() - this.icon.getWidth()) / 2.0;
		double yOffset = getY() + (getHeight() - this.icon.getHeight()) / 2.0;
		var poseStack = guiGraphics.pose();
		poseStack.pushPose();
		{
			poseStack.translate(xOffset, yOffset, 0);
			this.icon.draw(guiGraphics);
		}
		poseStack.popPose();
	}

	public void setArea(Rect2i buttonArea) {
		setX(buttonArea.getX());
		setY(buttonArea.getY());
		setWidth(buttonArea.getWidth());
		setHeight(buttonArea.getHeight());
	}
}
