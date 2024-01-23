package mezz.jei.common.network.packets;

import mezz.jei.common.network.*;
import mezz.jei.common.config.IServerConfig;
import mezz.jei.common.util.ServerCommandUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public class PacketRequestCheatPermission extends PacketJeiToServer {
	@Override
	public PacketIdServer getPacketId() {
		return PacketIdServer.CHEAT_PERMISSION_REQUEST;
	}

	@Override
	public void writePacketData(FriendlyByteBuf buf) {
		// the packet itself is the only data needed
	}

	public static PacketRequestCheatPermission readPacketData(FriendlyByteBuf buf) {
		return new PacketRequestCheatPermission();
	}

	@Override
	public void processOnServerThread(ServerPacketContext context) {
		ServerPlayer player = context.player();
		IServerConfig serverConfig = context.serverConfig();
		boolean hasPermission = ServerCommandUtil.hasPermissionForCheatMode(player, serverConfig);
		PacketCheatPermission packetCheatPermission = new PacketCheatPermission(hasPermission);

		IConnectionToClient connection = context.connection();
		connection.sendPacketToClient(packetCheatPermission, player);
	}
}
