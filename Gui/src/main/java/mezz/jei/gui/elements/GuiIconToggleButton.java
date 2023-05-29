package mezz.jei.gui.elements;

import net.minecraft.client.gui.GuiGraphics;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.common.gui.TooltipRenderer;
import mezz.jei.common.gui.elements.DrawableBlank;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.gui.input.IUserInputHandler;
import mezz.jei.gui.input.UserInput;
import mezz.jei.common.util.ImmutableRect2i;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class GuiIconToggleButton {
	private final IDrawable offIcon;
	private final IDrawable onIcon;
	private final GuiIconButton button;
	private ImmutableRect2i area;

	public GuiIconToggleButton(IDrawable offIcon, IDrawable onIcon, Textures textures) {
		this.offIcon = offIcon;
		this.onIcon = onIcon;
		this.button = new GuiIconButton(new DrawableBlank(0, 0), b -> {}, textures);
		this.area = ImmutableRect2i.EMPTY;
	}

	public void updateBounds(ImmutableRect2i area) {
		this.button.setWidth(area.getWidth());
		this.button.setHeight(area.getHeight());
		this.button.setX(area.getX());
		this.button.setY(area.getY());
		this.area = area;
	}

	public void draw(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		if (this.area.isEmpty()) {
			return;
		}
		this.button.render(guiGraphics, mouseX, mouseY, partialTicks);
		IDrawable icon = isIconToggledOn() ? this.onIcon : this.offIcon;
		icon.draw(guiGraphics, this.button.getX() + 2, this.button.getY() + 2);
	}

	public final boolean isMouseOver(double mouseX, double mouseY) {
		return this.area.contains(mouseX, mouseY);
	}

	public IUserInputHandler createInputHandler() {
		return new UserInputHandler();
	}

	public final void drawTooltips(GuiGraphics guiGraphics, int mouseX, int mouseY) {
		if (isMouseOver(mouseX, mouseY)) {
			List<Component> tooltip = new ArrayList<>();
			getTooltips(tooltip);
			TooltipRenderer.drawHoveringText(guiGraphics, tooltip, mouseX, mouseY);
		}
	}

	protected abstract void getTooltips(List<Component> tooltip);

	protected abstract boolean isIconToggledOn();

	protected abstract boolean onMouseClicked(UserInput input);

	private class UserInputHandler implements IUserInputHandler {
		@Override
		public final Optional<IUserInputHandler> handleUserInput(Screen screen, UserInput input, IInternalKeyMappings keyBindings) {
			if (isMouseOver(input.getMouseX(), input.getMouseY())) {
				IUserInputHandler handler = button.createInputHandler();
				return handler.handleUserInput(screen, input, keyBindings)
					.flatMap(handled -> {
						if (onMouseClicked(input)) {
							return Optional.of(this);
						}
						return Optional.empty();
					});
			}
			return Optional.empty();
		}
	}
}
