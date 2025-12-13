package mezz.jei.gui.elements;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.buttons.IButtonState;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.common.Internal;
import mezz.jei.common.gui.elements.DrawableBlank;
import mezz.jei.common.gui.elements.DrawableNineSliceTexture;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.common.util.ImmutableRect2i;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.CommonComponents;

/**
 * A button that has an {@link IDrawable} instead of a string label.
 * This internal class is used for re-using vanilla render code and to override behavior.
 * See {@link IconButton} for the class that uses this.
 */
class InternalIconButton extends Button implements IButtonState {
	private IDrawable icon = DrawableBlank.EMPTY;
	private boolean pressed = false;
	private boolean forcePressed = false;

	public InternalIconButton() {
		super(0, 0, 0, 0, CommonComponents.EMPTY, b -> {});
	}

	public void updateBounds(ImmutableRect2i area) {
		this.x = area.getX();
		this.y = area.getY();
		this.width = area.getWidth();
		this.height = area.getHeight();
	}

	public void setHeight(int value) {
		this.height = value;
	}

	@Override
	public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		boolean hovered =
			mouseX >= this.x &&
				mouseY >= this.y &&
				mouseX < this.x + this.width &&
				mouseY < this.y + this.height;
		RenderSystem.enableBlend();
		RenderSystem.blendFuncSeparate(
			GlStateManager.SourceFactor.SRC_ALPHA,
			GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
			GlStateManager.SourceFactor.ONE,
			GlStateManager.DestFactor.ZERO
		);
		RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		Textures textures = Internal.getTextures();
		boolean isPressed = this.pressed || this.forcePressed;
		DrawableNineSliceTexture texture = textures.getButtonForState(isPressed, this.active, hovered);
		texture.draw(poseStack, this.x, this.y, this.width, this.height);

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

		double xOffset = x + (width - icon.getWidth()) / 2.0;
		double yOffset = y + (height - icon.getHeight()) / 2.0;
		if (isPressed) {
			xOffset += 0.5;
			yOffset += 0.5;
		}
		poseStack.pushPose();
		{
			poseStack.translate(xOffset, yOffset, 0);
			icon.draw(poseStack);
		}
		poseStack.popPose();
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
	}

	public void setPressed(boolean pressed) {
		this.pressed = pressed;
	}

	@Override
	public void setForcePressed(boolean forcePressed) {
		this.forcePressed = forcePressed;
	}

	@Override
	public boolean clicked(double x, double y) {
		return super.clicked(x, y);
	}

	@Override
	public boolean isValidClickButton(int mouseButton) {
		return super.isValidClickButton(mouseButton);
	}

	@Override
	public void setIcon(IDrawable icon) {
		this.icon = icon;
	}

	@Override
	public void setActive(boolean value) {
		this.active = value;
	}

	@Override
	public void setVisible(boolean value) {
		this.visible = value;
	}
}
