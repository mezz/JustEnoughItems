package mezz.jei.input.mouse.handlers;

import mezz.jei.config.IWorldConfig;
import mezz.jei.config.KeyBindings;
import mezz.jei.input.UserInput;
import mezz.jei.input.mouse.IUserInputHandler;
import net.minecraft.client.gui.screens.Screen;

public class ClickGlobalHandler implements IUserInputHandler {
	private final IWorldConfig worldConfig;

	public ClickGlobalHandler(IWorldConfig worldConfig) {
		this.worldConfig = worldConfig;
	}

	@Override
	public IUserInputHandler handleUserInput(Screen screen, UserInput input) {
		if (input.is(KeyBindings.toggleOverlay)) {
			if (!input.isSimulate()) {
				worldConfig.toggleOverlayEnabled();
			}
			return this;
		}
		if (input.is(KeyBindings.toggleBookmarkOverlay)) {
			if (!input.isSimulate()) {
				worldConfig.toggleBookmarkEnabled();
			}
			return this;
		}
		if (input.is(KeyBindings.toggleCheatMode)) {
			if (!input.isSimulate()) {
				worldConfig.toggleCheatItemsEnabled();
			}
			return this;
		}
		return null;
	}
}
