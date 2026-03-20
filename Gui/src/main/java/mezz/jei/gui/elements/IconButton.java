package mezz.jei.gui.elements;

import mezz.jei.api.gui.handlers.IGuiProperties;
import mezz.jei.api.gui.buttons.IIconButtonController;
import mezz.jei.common.gui.JeiTooltip;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.gui.input.IUserInputHandler;
import mezz.jei.gui.input.UserInput;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;

import java.util.Optional;

public final class IconButton {
	private final InternalIconButton button;
	private final IIconButtonController controller;
	private ImmutableRect2i area;

	public IconButton(IIconButtonController controller) {
		this(controller, ImmutableRect2i.EMPTY);
	}

	public IconButton(IIconButtonController controller, ImmutableRect2i area) {
		this.controller = controller;
		this.button = new InternalIconButton();
		this.area = area;
		this.controller.initState(this.button);
	}

	public void updateBounds(ImmutableRect2i area) {
		this.button.updateBounds(area);
		this.area = area;
	}

	public ImmutableRect2i getArea() {
		return area;
	}

	public void draw(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
		if (this.area.isEmpty()) {
			return;
		}

		this.button.extractRenderState(guiGraphics, mouseX, mouseY, partialTicks);
		this.controller.drawExtras(guiGraphics, area.toMutable(), mouseX, mouseY, partialTicks);
	}

	public boolean isMouseOver(double mouseX, double mouseY) {
		return this.button.visible && this.area.contains(mouseX, mouseY);
	}

	public IUserInputHandler createInputHandler() {
		return new UserInputHandler(button, controller);
	}

	public void tick() {
		this.controller.updateState(this.button);
	}

	public void drawTooltips(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
		if (isMouseOver(mouseX, mouseY)) {
			JeiTooltip tooltip = new JeiTooltip();
			controller.getTooltips(tooltip);
			tooltip.draw(guiGraphics, mouseX, mouseY);
		}
	}

	public boolean isVisible() {
		return button.visible;
	}

	public int getX() {
		return area.getX();
	}

	public int getY() {
		return area.getY();
	}

	public int getWidth() {
		return area.getWidth();
	}

	public int getHeight() {
		return area.getHeight();
	}

	private static class UserInputHandler implements IUserInputHandler {
		private final InternalIconButton button;
		private final IIconButtonController controller;

		public UserInputHandler(InternalIconButton button, IIconButtonController controller) {
			this.button = button;
			this.controller = controller;
		}

		@Override
		public Optional<IUserInputHandler> handleUserInput(Screen screen, IGuiProperties guiProperties, UserInput input, IInternalKeyMappings keyBindings) {
			this.button.setPressed(false);

			boolean handled = input.ifMouseEvent((event, doubleClicked) -> {
				double mouseX = event.x();
				double mouseY = event.y();
				if (!this.button.isActive() || !this.button.isMouseOver(mouseX, mouseY)) {
					return false;
				}
				if (!this.button.isValidClickButton(event.buttonInfo())) {
					return false;
				}
				if (!input.isSimulate()) {
					this.button.playDownSound(Minecraft.getInstance().getSoundManager());
				} else {
					this.button.setPressed(true);
				}
				return true;
			});

			if (handled) {
				if (this.controller.onPress(input) && !input.isSimulate()) {
					this.controller.updateState(this.button);
				}
				return Optional.of(this);
			}
			return Optional.empty();
		}

		@Override
		public void unfocus() {
			this.button.setPressed(false);
		}
	}
}
