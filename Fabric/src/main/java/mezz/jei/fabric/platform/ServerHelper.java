package mezz.jei.fabric.platform;

import mezz.jei.common.platform.IPlatformServerHelper;
import net.minecraft.server.level.ServerPlayer;

public class ServerHelper implements IPlatformServerHelper {
	@Override
	public boolean hasPermissionForCheatMode(ServerPlayer player) {
		return IPlatformServerHelper.hasOperatorPermission(player);
	}
}
