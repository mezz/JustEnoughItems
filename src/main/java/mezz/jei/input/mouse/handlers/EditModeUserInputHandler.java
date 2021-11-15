package mezz.jei.input.mouse.handlers;

import mezz.jei.config.IWorldConfig;
import mezz.jei.config.KeyBindings;
import mezz.jei.input.UserInput;
import mezz.jei.input.mouse.IUserInputHandler;
import net.minecraft.client.gui.screens.Screen;

import javax.annotation.Nullable;

public class EditModeUserInputHandler implements IUserInputHandler {
	private final IWorldConfig worldConfig;

	public EditModeUserInputHandler(IWorldConfig worldConfig) {
		this.worldConfig = worldConfig;
	}

	@Nullable
	@Override
	public IUserInputHandler handleUserInput(Screen screen, UserInput input) {
		if (input.is(KeyBindings.toggleEditMode)) {
			if (!input.isSimulate()) {
				worldConfig.toggleEditModeEnabled();
			}
			return this;
		}

		return null;
	}
}
