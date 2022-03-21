package mezz.jei.input.mouse.handlers;

import mezz.jei.config.IWorldConfig;
import mezz.jei.config.KeyBindings;
import mezz.jei.input.UserInput;
import mezz.jei.input.mouse.IUserInputHandler;
import net.minecraft.client.gui.screens.Screen;

import java.util.Optional;

public class GlobalInputHandler implements IUserInputHandler {
	private final IWorldConfig worldConfig;

	public GlobalInputHandler(IWorldConfig worldConfig) {
		this.worldConfig = worldConfig;
	}

	@Override
	public Optional<IUserInputHandler> handleUserInput(Screen screen, UserInput input) {
		if (input.is(KeyBindings.toggleOverlay)) {
			if (!input.isSimulate()) {
				worldConfig.toggleOverlayEnabled();
			}
			return Optional.of(this);
		}

		if (input.is(KeyBindings.toggleBookmarkOverlay)) {
			if (!input.isSimulate()) {
				worldConfig.toggleBookmarkEnabled();
			}
			return Optional.of(this);
		}

		if (input.is(KeyBindings.toggleCheatMode)) {
			if (!input.isSimulate()) {
				worldConfig.toggleCheatItemsEnabled();
			}
			return Optional.of(this);
		}

		if (input.is(KeyBindings.toggleEditMode)) {
			if (!input.isSimulate()) {
				worldConfig.toggleEditModeEnabled();
			}
			return Optional.of(this);
		}

		return Optional.empty();
	}
}
