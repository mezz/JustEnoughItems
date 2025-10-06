package mezz.jei.gui.elements.config;

import mezz.jei.common.config.file.ConfigValue;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.gui.input.IUserInputHandler;
import mezz.jei.gui.input.UserInput;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.Optional;

final class IntegerEntryWidget extends EntryWidget<Integer> {

	IntegerEntryWidget(ConfigValue<Integer> value) {
		super(value);
	}

	@Override
	public void draw(GuiGraphics guiGraphics, double mouseX, double mouseY) {
		Component localizedName = getLocalizedName();
		guiGraphics.drawString(Minecraft.getInstance().font, localizedName, area.getX(), area.getY(), 0xFFFFFF);
		Component valueText = Component.literal(getValue().toString());
		guiGraphics.drawString(Minecraft.getInstance().font, valueText, area.getX() + 16, area.getY(), 0xFFFFFF);
	}

	@Override
	public IUserInputHandler createInputHandler() {
		return new InputHandler();
	}

	private class InputHandler implements IUserInputHandler {
		@Override
		public Optional<IUserInputHandler> handleUserInput(Screen screen, UserInput input, IInternalKeyMappings keyBindings) {
			return Optional.empty();
		}

		@Override
		public Optional<IUserInputHandler> handleMouseScrolled(double mouseX, double mouseY, double scrollDeltaX, double scrollDeltaY) {
			value.set(value.getValue() + (int) scrollDeltaY);
			return Optional.of(this);
		}
	}
}
