package mezz.jei.common.network.packets;

import mezz.jei.common.network.IPacketId;
import mezz.jei.common.network.PacketIdServer;
import mezz.jei.common.network.ServerPacketContext;
import mezz.jei.common.network.ServerPacketData;
import mezz.jei.common.config.IServerConfig;
import mezz.jei.common.network.IConnectionToClient;
import mezz.jei.common.util.ServerCommandUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.concurrent.CompletableFuture;

public class PacketRequestCheatPermission extends PacketJei {
	@Override
	public IPacketId getPacketId() {
		return PacketIdServer.CHEAT_PERMISSION_REQUEST;
	}

	@Override
	public void writePacketData(FriendlyByteBuf buf) {
		// the packet itself is the only data needed
	}

	public static CompletableFuture<Void> readPacketData(ServerPacketData data) {
		ServerPacketContext context = data.context();
		ServerPlayer player = context.player();
		IServerConfig serverConfig = context.serverConfig();
		MinecraftServer server = player.server;
		return server.submit(() -> {
			boolean hasPermission = ServerCommandUtil.hasPermissionForCheatMode(player, serverConfig);
			PacketCheatPermission packetCheatPermission = new PacketCheatPermission(hasPermission);

			IConnectionToClient connection = context.connection();
			connection.sendPacketToClient(packetCheatPermission, player);
		});
	}
}
