package mezz.jei.gui.input.handlers;

import mezz.jei.common.config.IClientToggleState;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.input.IUserInputHandler;
import mezz.jei.common.input.UserInput;
import mezz.jei.gui.util.CheatModeUtil;
import net.minecraft.client.gui.screens.Screen;

import java.util.Optional;

public class GlobalInputHandler implements IUserInputHandler {
	private final IClientToggleState toggleState;

	public GlobalInputHandler(IClientToggleState toggleState) {
		this.toggleState = toggleState;
	}

	@Override
	public Optional<IUserInputHandler> handleUserInput(Screen screen, UserInput input, IInternalKeyMappings keyBindings) {
		if (input.is(keyBindings.getToggleOverlay())) {
			if (!input.isSimulate()) {
				toggleState.toggleOverlayEnabled();
			}
			return Optional.of(this);
		}

		if (input.is(keyBindings.getToggleBookmarkOverlay())) {
			if (!input.isSimulate()) {
				toggleState.toggleBookmarkEnabled();
			}
			return Optional.of(this);
		}

		if (input.is(keyBindings.getToggleCheatMode())) {
			if (!input.isSimulate()) {
				CheatModeUtil.toggleCheatMode(toggleState);
			}
			return Optional.of(this);
		}

		if (input.is(keyBindings.getToggleEditMode())) {
			if (!input.isSimulate()) {
				toggleState.toggleEditModeEnabled();
			}
			return Optional.of(this);
		}

		return Optional.empty();
	}
}
