package mezz.jei.fabric.network;

import mezz.jei.common.Constants;
import mezz.jei.common.network.ServerPacketRouter;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resources.ResourceLocation;

public final class ServerNetworkHandler {
	private ServerNetworkHandler() {}

	public static void registerServerPacketHandler(ServerPacketRouter packetRouter) {
		registerServerPacketHandler(Constants.NETWORK_CHANNEL_ID, packetRouter);
		registerServerPacketHandler(Constants.RECIPE_TRANSFER_RESULT_CHANNEL_ID, packetRouter);
	}

	private static void registerServerPacketHandler(ResourceLocation channelId, ServerPacketRouter packetRouter) {
		ServerPlayNetworking.registerGlobalReceiver(
			channelId,
			(server, player, handler, buf, responseSender) ->
				packetRouter.onPacket(buf, player)
		);
	}
}
