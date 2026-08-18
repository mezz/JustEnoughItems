package mezz.jei.forge.platform;

import mezz.jei.api.constants.ModIds;
import mezz.jei.common.platform.IPlatformServerHelper;
import mezz.jei.forge.events.PermanentEventSubscriptions;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.server.permission.PermissionAPI;
import net.minecraftforge.server.permission.events.PermissionGatherEvent;
import net.minecraftforge.server.permission.nodes.PermissionNode;
import net.minecraftforge.server.permission.nodes.PermissionTypes;

public class ServerHelper implements IPlatformServerHelper {
	private static final PermissionNode<Boolean> CHEAT_PERMISSION = new PermissionNode<>(
		ModIds.JEI_ID,
		"cheat",
		PermissionTypes.BOOLEAN,
		(player, playerUUID, context) -> player != null && IPlatformServerHelper.hasOperatorPermission(player)
	);

	public static void register(PermanentEventSubscriptions subscriptions) {
		subscriptions.register(PermissionGatherEvent.Nodes.class, event -> event.addNodes(CHEAT_PERMISSION));
	}

	@Override
	public boolean hasPermissionForCheatMode(ServerPlayer player) {
		return PermissionAPI.getPermission(player, CHEAT_PERMISSION);
	}
}
