package mezz.jei.fabric.network;

import mezz.jei.common.Constants;
import mezz.jei.common.network.ClientPacketRouter;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.player.LocalPlayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class ClientNetworkHandler {
	private static final Logger LOGGER = LogManager.getLogger();

	private ClientNetworkHandler() {}

	public static void registerClientPacketHandler(ClientPacketRouter packetRouter) {
		ClientPlayNetworking.registerGlobalReceiver(
			Constants.NETWORK_CHANNEL_ID,
			(client, handler, buf, responseSender) -> {
				LocalPlayer player = client.player;
				if (player == null) {
					LOGGER.error("Packet error, the local player is missing.");
					return;
				}
				packetRouter.onPacket(buf, player);
			}
		);
	}
}
