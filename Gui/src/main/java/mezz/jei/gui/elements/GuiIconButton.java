package mezz.jei.gui.elements;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.common.Internal;
import mezz.jei.common.gui.elements.DrawableNineSliceTexture;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.gui.input.IUserInputHandler;
import mezz.jei.gui.input.UserInput;
import mezz.jei.common.util.ImmutableRect2i;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.CommonComponents;

import java.util.Optional;

/**
 * A gui button that has an {@link IDrawable} instead of a string label.
 */
public class GuiIconButton extends Button {
	private IDrawable icon;
	private boolean pressed = false;
	private boolean forcePressed = false;

	public GuiIconButton(int x, int y, int width, int height, IDrawable icon, OnPress pressable) {
		super(x, y, width, height, CommonComponents.EMPTY, pressable);
		this.icon = icon;
	}

	public GuiIconButton(IDrawable icon, OnPress pressable) {
		super(0, 0, 0, 0, CommonComponents.EMPTY, pressable);
		this.icon = icon;
	}

	public void updateBounds(Rect2i area) {
		this.x = area.getX();
		this.y = area.getY();
		this.width = area.getWidth();
		this.height = area.getHeight();
	}

	public void updateBounds(ImmutableRect2i area) {
		this.x = area.getX();
		this.y = area.getY();
		this.width = area.getWidth();
		this.height = area.getHeight();
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
		DrawableNineSliceTexture texture = textures.getButtonForState(this.active, hovered);
		texture.draw(poseStack, x, y, this.width, this.height);

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
		poseStack.pushPose();
		{
			poseStack.translate(xOffset, yOffset, 0);
			icon.draw(poseStack);
		}
		poseStack.popPose();
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
	}

	public IUserInputHandler createInputHandler() {
		return new UserInputHandler(this);
	}

	public void setForcePressed(boolean forcePressed) {
		this.forcePressed = forcePressed;
	}

	public ImmutableRect2i getArea() {
		return new ImmutableRect2i(x, y, width, height);
	}

	public void setIcon(IDrawable icon) {
		this.icon = icon;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public void setX(int x) {
		this.x = x;
	}

	public void setY(int y) {
		this.y = y;
	}

	private class UserInputHandler implements IUserInputHandler {
		private final GuiIconButton button;

		public UserInputHandler(GuiIconButton button) {
			this.button = button;
		}

		@Override
		public Optional<IUserInputHandler> handleUserInput(Screen screen, UserInput input, IInternalKeyMappings keyBindings) {
			this.button.pressed = false;

			double mouseX = input.getMouseX();
			double mouseY = input.getMouseY();
			if (!this.button.active || !this.button.visible || !isMouseOver(mouseX, mouseY)) {
				return Optional.empty();
			}
			if (!this.button.isValidClickButton(input.getKey().getValue())) {
				return Optional.empty();
			}
			boolean flag = this.button.clicked(mouseX, mouseY);
			if (!flag) {
				return Optional.empty();
			}
			if (!input.isSimulate()) {
				this.button.playDownSound(Minecraft.getInstance().getSoundManager());
				this.button.onClick(mouseX, mouseY);
			} else {
				this.button.pressed = true;
			}
			return Optional.of(this);
		}

		@Override
		public void unfocus() {
			this.button.pressed = false;
		}
	}
}
