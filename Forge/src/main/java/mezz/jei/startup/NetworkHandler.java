package mezz.jei.startup;

import mezz.jei.core.config.IWorldConfig;
import mezz.jei.network.PacketHandler;
import mezz.jei.network.PacketHandlerClient;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.event.EventNetworkChannel;

public class NetworkHandler {
	private static final String NETWORK_PROTOCOL_VERSION = "1.0.0";
	private final EventNetworkChannel channel;

	public NetworkHandler() {
		channel = NetworkRegistry.newEventChannel(
			PacketHandler.CHANNEL_ID,
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

	public void createServerPacketHandler() {
		PacketHandler packetHandler = new PacketHandler();
		channel.addListener(packetHandler::onPacket);
	}

	@OnlyIn(Dist.CLIENT)
	public void createClientPacketHandler(IWorldConfig worldConfig) {
		PacketHandlerClient packetHandler = new PacketHandlerClient(worldConfig);
		channel.addListener(packetHandler::onPacket);
	}
}
