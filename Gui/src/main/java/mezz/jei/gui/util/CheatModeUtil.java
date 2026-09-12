package mezz.jei.gui.util;

import mezz.jei.common.Internal;
import mezz.jei.common.config.IWorldConfig;
import mezz.jei.common.network.IConnectionToServer;
import mezz.jei.common.network.packets.PacketRequestCheatPermission;

public final class CheatModeUtil {
	private CheatModeUtil() {}

	public static void toggleCheatMode(IWorldConfig worldConfig) {
		setCheatModeEnabled(worldConfig, !worldConfig.isCheatItemsEnabled());
	}

	public static void setCheatModeEnabled(IWorldConfig worldConfig, boolean enabled) {
		worldConfig.setCheatItemsEnabled(enabled);
		if (enabled) {
			IConnectionToServer serverConnection = Internal.getServerConnection();
			serverConnection.sendPacketToServer(new PacketRequestCheatPermission());
		}
	}
}
