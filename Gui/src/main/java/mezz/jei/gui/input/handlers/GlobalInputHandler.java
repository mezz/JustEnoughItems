package mezz.jei.gui.input.handlers;

import mezz.jei.common.Internal;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.network.IConnectionToServer;
import mezz.jei.common.network.packets.PacketRequestCheatPermission;
import mezz.jei.common.config.IWorldConfig;
import mezz.jei.gui.input.UserInput;
import mezz.jei.gui.input.IUserInputHandler;
import net.minecraft.client.gui.screens.Screen;

import java.util.Optional;

public class GlobalInputHandler implements IUserInputHandler {
	private final IWorldConfig worldConfig;

	public GlobalInputHandler(IWorldConfig worldConfig) {
		this.worldConfig = worldConfig;
	}

	@Override
	public Optional<IUserInputHandler> handleUserInput(Screen screen, UserInput input, IInternalKeyMappings keyBindings) {
		if (input.is(keyBindings.getToggleOverlay())) {
			if (!input.isSimulate()) {
				worldConfig.toggleOverlayEnabled();
			}
			return Optional.of(this);
		}

		if (input.is(keyBindings.getToggleBookmarkOverlay())) {
			if (!input.isSimulate()) {
				worldConfig.toggleBookmarkEnabled();
			}
			return Optional.of(this);
		}

		if (input.is(keyBindings.getToggleCheatMode())) {
			if (!input.isSimulate()) {
				worldConfig.toggleCheatItemsEnabled();
				if (worldConfig.isCheatItemsEnabled()) {
					IConnectionToServer serverConnection = Internal.getServerConnection();
					serverConnection.sendPacketToServer(new PacketRequestCheatPermission());
				}
			}
			return Optional.of(this);
		}

		if (input.is(keyBindings.getToggleEditMode())) {
			if (!input.isSimulate()) {
				worldConfig.toggleEditModeEnabled();
			}
			return Optional.of(this);
		}

		return Optional.empty();
	}
}
