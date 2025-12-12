package mezz.jei.gui.elements;

import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.handlers.IGuiProperties;
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
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;

import java.util.Optional;

/**
 * A gui button that has an {@link IDrawable} instead of a string label.
 */
public class GuiIconButton extends Button {
	// TODO re-implement the "clicked" state when something is held down
	private static final WidgetSprites SPRITES = new WidgetSprites(
		Identifier.withDefaultNamespace("widget/button"),
		Identifier.withDefaultNamespace("widget/button_disabled"),
		Identifier.withDefaultNamespace("widget/button_highlighted")
	);

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
	protected void renderContents(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		Identifier spriteLocation = SPRITES.get(this.active, this.isHoveredOrFocused());
		guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, spriteLocation, this.getX(), this.getY(), this.getWidth(), this.getHeight(), ARGB.white(this.alpha));

		float xOffset = getX() + (width - icon.getWidth()) / 2.0f;
		float yOffset = getY() + (height - icon.getHeight()) / 2.0f;
		if (this.pressed || this.forcePressed) {
			xOffset += 0.5f;
			yOffset += 0.5f;
		}
		var poseStack = guiGraphics.pose();
		poseStack.pushMatrix();
		{
			poseStack.translate(xOffset, yOffset);
			icon.draw(guiGraphics);
		}
		poseStack.popMatrix();
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
		public Optional<IUserInputHandler> handleUserInput(Screen screen, IGuiProperties guiProperties, UserInput input, IInternalKeyMappings keyBindings) {
			this.button.pressed = false;

			boolean handled = input.ifMouseEvent((event, doubleClicked) -> {
				double mouseX = event.x();
				double mouseY = event.y();
				if (!this.button.active || !this.button.visible || !isMouseOver(mouseX, mouseY)) {
					return false;
				}
				if (!this.button.isValidClickButton(event.buttonInfo())) {
					return false;
				}
				if (!input.isSimulate()) {
					this.button.playDownSound(Minecraft.getInstance().getSoundManager());
					this.button.onClick(event, doubleClicked);
				} else {
					this.button.pressed = true;
				}
				return true;
			});

			if (handled) {
				return Optional.of(this);
			}
			return Optional.empty();
		}

		@Override
		public void unfocus() {
			this.button.pressed = false;
		}
	}
}
