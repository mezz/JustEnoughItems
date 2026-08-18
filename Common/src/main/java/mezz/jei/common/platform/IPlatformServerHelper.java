package mezz.jei.common.platform;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public interface IPlatformServerHelper {
	boolean hasPermissionForCheatMode(ServerPlayer player);

	static boolean hasOperatorPermission(ServerPlayer player) {
		MinecraftServer server = player.getServer();
		if (server == null) {
			return false;
		}

		int opPermissionLevel = server.getOperatorUserPermissionLevel();
		return player.createCommandSourceStack()
			.hasPermission(opPermissionLevel);
	}
}
