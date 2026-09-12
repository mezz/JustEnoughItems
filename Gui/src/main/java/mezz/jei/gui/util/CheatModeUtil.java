package mezz.jei.gui.util;

import mezz.jei.common.Internal;
import mezz.jei.common.config.IClientToggleState;
import mezz.jei.common.network.IConnectionToServer;
import mezz.jei.common.network.packets.PacketRequestCheatPermission;

public final class CheatModeUtil {
	private CheatModeUtil() {}

	public static void toggleCheatMode(IClientToggleState toggleState) {
		setCheatModeEnabled(toggleState, !toggleState.isCheatItemsEnabled());
	}

	public static void setCheatModeEnabled(IClientToggleState toggleState, boolean enabled) {
		toggleState.setCheatItemsEnabled(enabled);
		if (enabled) {
			IConnectionToServer serverConnection = Internal.getServerConnection();
			serverConnection.sendPacketToServer(PacketRequestCheatPermission.INSTANCE);
		}
	}
}
