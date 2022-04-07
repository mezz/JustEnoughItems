package mezz.jei.forge.network;

import mezz.jei.common.network.ClientPacketRouter;
import mezz.jei.common.network.ServerPacketRouter;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.event.EventNetworkChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NetworkHandler {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final String NETWORK_PROTOCOL_VERSION = "1.0.0";
	private final EventNetworkChannel channel;

	public NetworkHandler() {
		channel = NetworkRegistry.newEventChannel(
			ServerPacketRouter.CHANNEL_ID,
			() -> NETWORK_PROTOCOL_VERSION,
			NetworkHandler::isClientAcceptedVersion,
			NetworkHandler::isServerAcceptedVersion
		);
	}

	private static boolean isClientAcceptedVersion(String version) {
		return true;
	}

	private static boolean isServerAcceptedVersion(String version) {
		return true;
	}

	public void createServerPacketHandler(ServerPacketRouter packetRouter) {
		channel.addListener((NetworkEvent.ClientCustomPayloadEvent event) -> {
			try {
				ServerPlayer player = event.getSource().get().getSender();
				if (player == null) {
					LOGGER.error("Packet error, the sender player is missing for event: {}", event);
					return;
				}
				packetRouter.onPacket(event.getPayload(), player);
			} catch (Throwable e) {
				try {
					LOGGER.error("Packet error for event: {}", event, e);
				} catch (Throwable e2) {
					e2.addSuppressed(e);
					LOGGER.error("Packet error", e2);
				}
			}
			event.getSource().get().setPacketHandled(true);
		});
	}

	@OnlyIn(Dist.CLIENT)
	public void createClientPacketHandler(ClientPacketRouter packetRouter) {
		channel.addListener((NetworkEvent.ServerCustomPayloadEvent event) -> {
			try {
				packetRouter.onPacket(event.getPayload());
			} catch (Throwable e) {
				try {
					LOGGER.error("Packet error for event: {}", event, e);
				} catch (Throwable e2) {
					e2.addSuppressed(e);
					LOGGER.error("Packet error", e2);
				}
			}
			event.getSource().get().setPacketHandled(true);
		});
	}
}
