package mezz.jei.gui.elements;

import mezz.jei.common.gui.IIconButtonController;
import mezz.jei.common.gui.JeiTooltip;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.gui.input.IUserInputHandler;
import mezz.jei.gui.input.UserInput;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
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

	public void draw(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		if (this.area.isEmpty()) {
			return;
		}

		this.button.render(guiGraphics, mouseX, mouseY, partialTicks);
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

	public void drawTooltips(GuiGraphics guiGraphics, int mouseX, int mouseY) {
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
		public Optional<IUserInputHandler> handleUserInput(Screen screen, UserInput input, IInternalKeyMappings keyBindings) {
			this.button.setPressed(false);

			double mouseX = input.getMouseX();
			double mouseY = input.getMouseY();
			if (!this.button.active || !this.button.visible || !this.button.isMouseOver(mouseX, mouseY)) {
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
			} else {
				this.button.setPressed(true);
			}
			this.controller.onPress(input);
			return Optional.of(this);
		}

		@Override
		public void unfocus() {
			this.button.setPressed(false);
		}
	}
}
