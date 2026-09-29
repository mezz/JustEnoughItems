package mezz.jei.gui.elements;

import mezz.jei.api.gui.buttons.IIconButtonController;
import mezz.jei.common.gui.JeiTooltip;
import mezz.jei.common.input.IInputTarget;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.input.UserInput;
import mezz.jei.common.input.interaction.IInputInteraction;
import mezz.jei.common.util.ImmutableRect2i;
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

	public void setForcePressed(boolean forcePressed) {
		this.button.setForcePressed(forcePressed);
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

	public IInputTarget createInputHandler() {
		return new ButtonInputHandler(button, controller);
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

	private static class ButtonInputHandler implements IInputTarget {
		private final InternalIconButton button;
		private final IIconButtonController controller;

		public ButtonInputHandler(InternalIconButton button, IIconButtonController controller) {
			this.button = button;
			this.controller = controller;
		}

		@Override
		public Optional<IInputInteraction> beginInput(Screen screen, UserInput input, IInternalKeyMappings keys) {
			boolean hit = input.ifMouseEvent((event, ignoredDoubleClick) -> button.isActive() && button.isMouseOver(event.x(), event.y()) && button.isValidClickButton(event.buttonInfo()));
			if (!hit) {
				return Optional.empty();
			}
			button.setPressed(true);
			controller.onPress(input);
			return Optional.of(new ButtonPress(button, controller));
		}

		@Override
		public void resetInput() {
			this.button.setPressed(false);
		}
	}

	/**
	 * Keeps the button looking pressed until release or cancellation, and runs its action
	 * only if the mouse button is released over it while the control is still enabled.
	 *
	 * @param button the button to check and update on release
	 * @param controller the button's public API handler, called on release to run the action
	 */
	private record ButtonPress(InternalIconButton button, IIconButtonController controller) implements IInputInteraction {
		@Override
		public void complete(UserInput input) {
			button.setPressed(false);
			if (!button.isActive() || !button.isMouseOver(input.getMouseX(), input.getMouseY())) {
				return;
			}
			button.playDownSound(Minecraft.getInstance().getSoundManager());
			if (controller.onPress(input)) {
				controller.updateState(button);
			}
		}

		@Override
		public void cancel() {
			button.setPressed(false);
		}
	}

}
