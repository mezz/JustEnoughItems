package mezz.jei.fabric.network;

import mezz.jei.common.Constants;
import mezz.jei.common.network.ServerPacketRouter;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public final class ServerNetworkHandler {
	private ServerNetworkHandler() {}

	public static void registerServerPacketHandler(ServerPacketRouter packetRouter) {
		ServerPlayNetworking.registerGlobalReceiver(
			Constants.NETWORK_CHANNEL_ID,
			(server, player, handler, buf, responseSender) -> {
				packetRouter.onPacket(buf, player);
			}
		);
	}
}
