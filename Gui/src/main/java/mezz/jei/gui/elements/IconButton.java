package mezz.jei.gui.elements;

import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.inputs.IJeiUserInput;
import mezz.jei.common.gui.JeiTooltip;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.gui.input.IUserInputHandler;
import mezz.jei.gui.input.UserInput;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;

import java.util.Optional;
import java.util.function.Function;

public final class IconButton {
	private final InternalIconButton button;
	private final IIconButtonDescriptor descriptor;
	private ImmutableRect2i area;

	public IconButton(IIconButtonDescriptor descriptor) {
		this(descriptor, ImmutableRect2i.EMPTY);
	}

	public IconButton(IDrawable icon, Function<IJeiUserInput, Boolean> onPress) {
		this(new BasicButtonDescriptor(icon, onPress));
	}

	public IconButton(IIconButtonDescriptor descriptor, ImmutableRect2i area) {
		this.descriptor = descriptor;
		this.button = new InternalIconButton();
		this.area = area;
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

		this.button.active = descriptor.isActive();
		this.button.visible = descriptor.isVisible();
		this.button.setForcePressed(descriptor.isForcePressed());
		this.button.setIcon(descriptor.getIcon());
		this.button.render(guiGraphics, mouseX, mouseY, partialTicks);
		this.descriptor.drawExtras(guiGraphics, area.toMutable(), mouseX, mouseY, partialTicks);
	}

	public boolean isMouseOver(double mouseX, double mouseY) {
		return this.button.visible && this.area.contains(mouseX, mouseY);
	}

	public IUserInputHandler createInputHandler() {
		return new UserInputHandler(button, descriptor);
	}

	public void tick() {
		descriptor.tick();
		this.button.active = descriptor.isActive();
		this.button.visible = descriptor.isVisible();
		this.button.setForcePressed(descriptor.isForcePressed());
		this.button.setIcon(descriptor.getIcon());
	}

	public void drawTooltips(GuiGraphics guiGraphics, int mouseX, int mouseY) {
		if (isMouseOver(mouseX, mouseY)) {
			JeiTooltip tooltip = new JeiTooltip();
			descriptor.getTooltips(tooltip);
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
		private final IIconButtonDescriptor descriptor;

		public UserInputHandler(InternalIconButton button, IIconButtonDescriptor descriptor) {
			this.button = button;
			this.descriptor = descriptor;
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
			this.descriptor.onPress(input);
			return Optional.of(this);
		}

		@Override
		public void unfocus() {
			this.button.setPressed(false);
		}
	}
}
