package mezz.jei.gui.elements;

import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.gui.input.IUserInputHandler;
import mezz.jei.gui.input.UserInput;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;

import java.util.Optional;

/**
 * A gui button that has an {@link IDrawable} instead of a string label.
 */
public class GuiIconButton extends Button {
	private static final WidgetSprites SPRITES = new WidgetSprites(ResourceLocation.withDefaultNamespace("widget/button"), ResourceLocation.withDefaultNamespace("widget/button_disabled"), ResourceLocation.withDefaultNamespace("widget/button_highlighted"));

	private IDrawable icon;
	private boolean pressed = false;
	private boolean forcePressed = false;

	public GuiIconButton(int x, int y, int width, int height, IDrawable icon, OnPress pressable) {
		super(x, y, width, height, CommonComponents.EMPTY, pressable, Button.DEFAULT_NARRATION);
		this.icon = icon;
	}

	public GuiIconButton(IDrawable icon, OnPress pressable) {
		super(0, 0, 0, 0, CommonComponents.EMPTY, pressable, Button.DEFAULT_NARRATION);
		this.icon = icon;
	}

	public void updateBounds(Rect2i area) {
		setX(area.getX());
		setY(area.getY());
		this.width = area.getWidth();
		this.height = area.getHeight();
	}

	public void updateBounds(ImmutableRect2i area) {
		setX(area.getX());
		setY(area.getY());
		this.width = area.getWidth();
		this.height = area.getHeight();
	}

	@Override
	public void setHeight(int value) {
		this.height = value;
	}

	@Override
	public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		guiGraphics.blitSprite(RenderType::guiTextured, SPRITES.get(this.active, this.isHoveredOrFocused()), this.getX(), this.getY(), this.getWidth(), this.getHeight(), ARGB.white(this.alpha));

		double xOffset = getX() + (width - icon.getWidth()) / 2.0;
		double yOffset = getY() + (height - icon.getHeight()) / 2.0;
		if (this.pressed || this.forcePressed) {
			xOffset += 0.5;
			yOffset += 0.5;
		}
		var poseStack = guiGraphics.pose();
		poseStack.pushPose();
		{
			poseStack.translate(xOffset, yOffset, 0);
			icon.draw(guiGraphics);
		}
		poseStack.popPose();
	}

	public IUserInputHandler createInputHandler() {
		return new UserInputHandler(this);
	}

	public void setForcePressed(boolean forcePressed) {
		this.forcePressed = forcePressed;
	}

	public ImmutableRect2i getArea() {
		return new ImmutableRect2i(getX(), getY(), width, height);
	}

	public void setIcon(IDrawable icon) {
		this.icon = icon;
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
