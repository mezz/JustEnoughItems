package mezz.jei.gui.elements.config;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.common.config.file.ConfigValue;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.gui.input.IUserInputHandler;
import mezz.jei.gui.input.UserInput;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.Optional;

final class BooleanEntryWidget extends EntryWidget<Boolean> {

	BooleanEntryWidget(ConfigValue<Boolean> value) {
		super(value);
	}

	@Override
	public void draw(GuiGraphics guiGraphics, double mouseX, double mouseY) {
		Component localizedName = getLocalizedName();
		guiGraphics.drawString(Minecraft.getInstance().font, localizedName, area.getX(), area.getY(), 0xFFFFFF);
		//draw checkbox
		boolean checked = value.getValue();
		Component checkbox = Component.literal(checked ? "X" : " ");
		guiGraphics.drawString(Minecraft.getInstance().font, checkbox, area.getX() + 16, area.getY(), 0xFFFFFF);
	}

	@Override
	public IUserInputHandler createInputHandler() {
		return new InputHandler();
	}

	void changeValue() {
		value.set(!value.getValue());
	}

	private class InputHandler implements IUserInputHandler {
		@Override
		public Optional<IUserInputHandler> handleUserInput(Screen screen, UserInput input, IInternalKeyMappings keyBindings) {
			if (input.getKey().getType() == InputConstants.Type.MOUSE) {
				changeValue();
				return Optional.of(this);
			}
			return Optional.empty();
		}
	}

}
